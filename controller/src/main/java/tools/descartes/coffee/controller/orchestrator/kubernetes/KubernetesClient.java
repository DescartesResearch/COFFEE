package tools.descartes.coffee.controller.orchestrator.kubernetes;

import tools.descartes.coffee.controller.config.ClusterProperties;
import tools.descartes.coffee.controller.config.ControllerProperties;
import tools.descartes.coffee.controller.orchestrator.kubernetes.configuration.KubernetesProperties;
import tools.descartes.coffee.controller.orchestrator.BaseClusterClient;
import tools.descartes.coffee.controller.orchestrator.kubernetes.util.KubeConfigConverter;
import tools.descartes.coffee.controller.orchestrator.kubernetes.util.KubeUtils;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.KubeConfig;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.function.FailableSupplier;

public class KubernetesClient extends BaseClusterClient {
    private static final Logger logger = Logger.getLogger(KubernetesClient.class.getName());
    private final KubeConfig kubeConfiguration;
    private final KubernetesProperties kubernetesProperties;
    private final KubeComponents kubeComponents;

    private boolean connected = false;

    public KubernetesClient(ControllerProperties controllerProperties, ClusterProperties clusterConfig, KubernetesProperties kubernetesConfig, KubeComponents kubeComponents) {
        super(controllerProperties, clusterConfig);
        kubernetesProperties = kubernetesConfig;
        kubeConfiguration = KubeConfigConverter.convert(clusterConfig, kubernetesConfig);
        this.kubeComponents = kubeComponents;
    }

    @Override
    public void connect() {
        ApiClient client;

        try {
            client = Config.fromConfig(kubeConfiguration);
            // ApiClient client = new KubeConfigurator().configure(clusterConfig);
        } catch (IOException e) {
            throw new IllegalStateException("Error while loading the kubernetes configuration: " + e.getMessage(), e);
        }

        client.setDebugging(false);

        Configuration.setDefaultApiClient(client);
        connected = true;
    }

    public void reconnect() {
        // https://github.com/fabric8io/kubernetes-client/issues/2112
        connect();
    }

    @Override
    public void init() {

        boolean namespacePresent = this.isNamespaceCreated();
        boolean servicePresent = this.isServiceCreated();
        boolean deploymentPresent = this.isDeploymentCreated();

        if (!namespacePresent) {
            this.createNamespace();
        }

        if (!deploymentPresent) {
            this.createDeployment(false);
        } else {
            this.scaleDownDeployedPods();
        }

        if (!servicePresent) {
            this.createService(false);
        }

        if (!this.isProxyDeploymentCreated()) {
            this.createDeployment(true);
        }

        if (!this.isProxyServiceCreated()) {
            this.createService(true);
        }
    }

    @Override
    public void clear() {
        boolean namespacePresent = this.isNamespaceCreated();
        boolean servicePresent = this.isServiceCreated();
        boolean deploymentPresent = this.isDeploymentCreated();

        if (namespacePresent) {

            if (servicePresent) {
                this.deleteNamespacedService();
            }

            if (deploymentPresent) {
                this.scaleDownDeployedPods();
                this.deleteNamespacedDeployment();
            }

            deleteNamespace();
        }
    }

    @Override
    public void disconnect() {
        connected = false;
    }

    public CoreV1Api getAPI() {
        if (connected) {
            return new CoreV1Api();
        } else {
            throw new IllegalStateException("Trying to access the kubernetes api while disconnected");
        }
    }

    public AppsV1Api getAppsAPI() {
        if (connected) {
            return new AppsV1Api();
        } else {
            throw new IllegalStateException("Trying to access the kubernetes apps api while disconnected");
        }
    }

    private boolean isNamespaceCreated() {

        V1NamespaceList namespaces = callAPI("listNamespace",
                () -> getAPI().listNamespace(null, null, null, null, null, null, null, null, null,
                        null));

        List<String> namespaceNames = namespaces.getItems().stream()
                .map(namespace -> namespace.getMetadata().getName())
                .collect(Collectors.toList());

        return namespaceNames.contains(kubernetesProperties.getNaming().getNamespace());
    }

    private boolean isProxyServiceCreated() {
        return this.isServiceCreated(kubernetesProperties.getNaming().getProxyService());
    }

    private boolean isServiceCreated() {
        return this.isServiceCreated(kubernetesProperties.getNaming().getService());
    }

    private boolean isServiceCreated(String serviceName) {

        V1ServiceList services = callAPI("listNamespacedService",
                () -> getAPI().listNamespacedService(kubernetesProperties.getNaming().getNamespace(), null, null, null,
                        null, null, null, null, null, null, null));

        List<String> serviceNames = services.getItems().stream()
                .map(service -> service.getMetadata().getName())
                .collect(Collectors.toList());

        return serviceNames.contains(serviceName);
    }

    private boolean isDeploymentCreated() {
        return this.isDeploymentCreated(kubernetesProperties.getNaming().getDeployment());
    }

    private boolean isProxyDeploymentCreated() {
        return this.isDeploymentCreated(kubernetesProperties.getNaming().getProxyDeployment());
    }

    private boolean isDeploymentCreated(String deploymentName) {

        V1DeploymentList deployments = callAPI("listNamespacedDeployment",
                () -> getAppsAPI().listNamespacedDeployment(kubernetesProperties.getNaming().getNamespace(), null, null,
                        null, null, null, null, null, null, null, null));

        List<String> deploymentNames = deployments.getItems().stream()
                .map(deployment -> deployment.getMetadata().getName())
                .collect(Collectors.toList());

        return deploymentNames.contains(deploymentName);
    }

    private void createNamespace() {
        callAPI("createNamespace",
                (newNamespace) -> getAPI().createNamespace(newNamespace, null, null, null),
                kubeComponents.getApplicationNamespace());
    }

    private void createDeployment(boolean isProxyContext) {
        callAPI("createNamespacedDeployment",
                (newDeployment) -> getAppsAPI().createNamespacedDeployment(kubernetesProperties.getNaming().getNamespace(),
                        newDeployment, null, null, null),
                KubeUtils.createDeployment(controllerProperties, clusterProperties, kubernetesProperties, isProxyContext));
    }

    private void createService(Boolean isProxyContext) {
        callAPI("createNamespacedService", (newService) -> getAPI()
                .createNamespacedService(kubernetesProperties.getNaming().getNamespace(), newService, null, null, null),
                isProxyContext ? kubeComponents.getProxyService() : kubeComponents.getApplicationService());

    }

    /**
     * Scales the default deployment to 0 replicas.
     * 
     * Resc
     */
    private void scaleDownDeployedPods() {
        V1Patch scaleDownPatch = KubeUtils.createScalePatch(0);

        callAPI("patchNamespacedDeploymentScale",
                () -> getAppsAPI().patchNamespacedDeploymentScale(kubernetesProperties.getNaming().getDeployment(),
                        kubernetesProperties.getNaming().getNamespace(), scaleDownPatch, null, null, null, null));

        callAPI("patchNamespacedDeploymentScale",
                () -> getAppsAPI().patchNamespacedDeploymentScale(kubernetesProperties.getNaming().getProxyDeployment(),
                        kubernetesProperties.getNaming().getNamespace(), scaleDownPatch, null, null, null, null));
    }

    private void deleteNamespacedService() {
        callAPI("deleteNamespacedService",
                () -> getAPI().deleteNamespacedService(kubernetesProperties.getNaming().getService(),
                        kubernetesProperties.getNaming().getNamespace(), null, null, null, null, null, null));
    }

    private void deleteNamespacedDeployment() {
        callAPI("deleteCollectionNamespacedDeployment",
                () -> getAppsAPI().deleteCollectionNamespacedDeployment(kubernetesProperties.getNaming().getNamespace(),
                        null, null, null, null, null, null, null, null, null, null, null, null, null));
    }

    private void deleteNamespace() {
        callAPI("deleteNamespace",
                () -> getAPI().deleteNamespace(kubernetesProperties.getNaming().getNamespace(), null, null, null, null,
                        null, null));
    }

    private <T> T callAPI(String context, FailableSupplier<T, ApiException> call) {
        for (int i = 0; i < 2; i++) {
            try {
                return call.get();
            } catch (ApiException e) {
                if (i < 1) {
                    logger.warning("ApiException occured, try reconnect");
                    reconnect();
                } else {
                    this.throwApiException(context, e.getMessage(), e);
                    return null;
                }
            }
        }
        throw new IllegalStateException();
    }

    private <T, R> T callAPI(String context, FailableFunction<R, T, ApiException> call, R arg) {
        for (int i = 0; i < 2; i++) {
            try {
                // logger.info("Argument: " + arg);
                return call.apply(arg);
            } catch (ApiException e) {
                if (i < 1) {
                    logger.warning("ApiException occured, try reconnect");
                    reconnect();
                } else {
                    this.throwApiException(context, e.getMessage(), e);
                    return null;
                }
            }
        }
        throw new IllegalStateException();
    }

    private void throwApiException(String context, String message, Throwable cause) {
        throw new IllegalStateException(
                "API Exception while accessing " + context + " Kubernetes client API:\n" + message, cause);
    }
}
