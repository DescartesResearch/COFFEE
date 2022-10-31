package tools.descartes.coffee.controller.orchestrator;

import java.util.HashMap;
import java.util.Map;

import tools.descartes.coffee.controller.config.ClusterProperties;
import tools.descartes.coffee.controller.orchestrator.kubernetes.KubeClassMap;
import tools.descartes.coffee.controller.orchestrator.kubernetes.KubernetesClient;
import tools.descartes.coffee.controller.orchestrator.kubernetes.accessor.KubePodAccessor;
import tools.descartes.coffee.controller.orchestrator.kubernetes.configuration.KubernetesProperties;
import tools.descartes.coffee.controller.orchestrator.nomad.NomadClassMap;
import tools.descartes.coffee.controller.orchestrator.nomad.NomadClient;
import tools.descartes.coffee.controller.orchestrator.nomad.accessor.NomadContainerAccessor;
import tools.descartes.coffee.controller.orchestrator.nomad.configuration.NomadProperties;
import tools.descartes.coffee.controller.procedure.BaseProcedure;
import tools.descartes.coffee.controller.procedure.collection.Command;
import tools.descartes.coffee.controller.utils.EnumUtils;
import org.springframework.stereotype.Component;

@Component
public class OrchestratorMap {

    private final ClusterProperties clusterProperties;
    private final HashMap<Orchestrators, Map<Command, Class<? extends BaseProcedure>>> map;
    private final HashMap<Orchestrators, ContainerAccessor> containerAccessorMap;

    public OrchestratorMap(ClusterProperties clusterProperties, ClusterClientWrapper clusterClientWrapper,
                           KubernetesProperties kubernetesProperties, NomadProperties nomadProperties) {
        this.clusterProperties = clusterProperties;
        map = new HashMap<>() {
            {
                put(Orchestrators.KUBERNETES, KubeClassMap.MAP);
                put(Orchestrators.NOMAD, NomadClassMap.MAP);
            }
        };
        containerAccessorMap = new HashMap<>();
        if (clusterClientWrapper.getClient() instanceof KubernetesClient) {
            containerAccessorMap.put(Orchestrators.KUBERNETES, new KubePodAccessor(clusterClientWrapper, kubernetesProperties, clusterProperties));
        } else if (clusterClientWrapper.getClient() instanceof NomadClient) {
            containerAccessorMap.put(Orchestrators.NOMAD, new NomadContainerAccessor((NomadClient) clusterClientWrapper.getClient(), nomadProperties));
        }
    }

    public ContainerAccessor getContainerAccessor() {
        try {
            return containerAccessorMap.get(EnumUtils.searchEnum(Orchestrators.class, clusterProperties.getOrchestrator()));
        } catch (IllegalArgumentException | SecurityException e) {
            throw new IllegalStateException(
                    "Error occurred instantiating the container accessor for " + clusterProperties.getOrchestrator(), e);
        }
    }

    public HashMap<Orchestrators, Map<Command, Class<? extends BaseProcedure>>> getMap() {
        return map;
    }
}
