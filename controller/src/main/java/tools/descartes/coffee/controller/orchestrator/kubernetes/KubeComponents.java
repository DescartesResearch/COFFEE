package tools.descartes.coffee.controller.orchestrator.kubernetes;

import tools.descartes.coffee.controller.config.ClusterProperties;
import tools.descartes.coffee.controller.config.ControllerProperties;
import tools.descartes.coffee.controller.orchestrator.kubernetes.configuration.KubernetesProperties;
import tools.descartes.coffee.controller.orchestrator.kubernetes.util.KubeUtils;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Service;
import org.springframework.stereotype.Component;

@Component
public class KubeComponents {

        private final V1Namespace applicationNamespace;
        private final V1Service proxyService;
        private final V1Service applicationService;

        public KubeComponents(ControllerProperties controllerProperties, ClusterProperties clusterProperties, KubernetesProperties kubernetesProperties) {
                applicationNamespace = new V1Namespace().metadata(new V1ObjectMeta().name(kubernetesProperties.getNaming().getNamespace()));
                proxyService = KubeUtils.createService(clusterProperties, kubernetesProperties, true);
                applicationService = KubeUtils.createService(clusterProperties, kubernetesProperties, false);
        }

        public V1Namespace getApplicationNamespace() {
                return applicationNamespace;
        }

        public V1Service getProxyService() {
                return proxyService;
        }

        public V1Service getApplicationService() {
                return applicationService;
        }
}
