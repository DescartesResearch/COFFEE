package tools.descartes.coffee.controller.orchestrator;

import tools.descartes.coffee.controller.config.ClusterProperties;
import tools.descartes.coffee.controller.config.ControllerProperties;
import tools.descartes.coffee.controller.orchestrator.kubernetes.KubeComponents;
import tools.descartes.coffee.controller.orchestrator.kubernetes.KubernetesClient;
import tools.descartes.coffee.controller.orchestrator.kubernetes.configuration.KubernetesProperties;
import tools.descartes.coffee.controller.orchestrator.nomad.NomadClient;
import tools.descartes.coffee.controller.orchestrator.nomad.NomadComponents;
import tools.descartes.coffee.controller.orchestrator.nomad.balancer.HAProxy;
import tools.descartes.coffee.controller.orchestrator.nomad.configuration.NomadProperties;
import org.springframework.stereotype.Component;

@Component
public class ClusterClientWrapper {
    private final BaseClusterClient client;

    public ClusterClientWrapper(ControllerProperties controllerProperties, ClusterProperties clusterProperties,
                                KubernetesProperties kubernetesProperties, KubeComponents kubeComponents,
                                NomadProperties nomadProperties, NomadComponents nomadComponents, HAProxy haProxy) {
        switch (clusterProperties.getOrchestrator()) {
            case "kubernetes":
                client = new KubernetesClient(controllerProperties, clusterProperties, kubernetesProperties, kubeComponents);
                break;
            case "nomad":
                client = new NomadClient(nomadComponents, controllerProperties, clusterProperties, nomadProperties, haProxy);
                break;
            default:
                throw new IllegalArgumentException("Unknown orchestrator: " + clusterProperties.getOrchestrator());
        }
    }

    public BaseClusterClient getClient() {
        return client;
    }
}
