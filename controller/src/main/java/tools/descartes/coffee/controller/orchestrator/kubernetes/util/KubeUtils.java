package tools.descartes.coffee.controller.orchestrator.kubernetes.util;

import tools.descartes.coffee.controller.config.ClusterProperties;
import tools.descartes.coffee.controller.config.ControllerProperties;
import tools.descartes.coffee.controller.orchestrator.kubernetes.KubernetesClient;
import tools.descartes.coffee.controller.orchestrator.kubernetes.configuration.KubernetesProperties;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to provide deployment specific functions.
 * 
 * TBD: use Kubernetes PatchUtils
 * Patch implementation example:
 * 
 * @see <a href="https://github.com/kubernetes-client/java/blob/master/examples/examples-release-14/src/main/java/io/kubernetes/client/examples/PatchExample.java">...</a>
 */
public final class KubeUtils {
    private KubeUtils() {

    }

    public static V1PersistentVolumeClaim createPvc(KubernetesProperties kubernetesProperties) {
        Map<String, Quantity> storageRequests = new HashMap<>();
        storageRequests.put("storage", new Quantity("100Mi"));
        return new V1PersistentVolumeClaim()
                .metadata(new V1ObjectMeta().name("storage-pvc").namespace(kubernetesProperties.getNaming().getNamespace()))
                .spec(new V1PersistentVolumeClaimSpec().accessModes(List.of("ReadWriteOnce")).resources(new V1ResourceRequirements().requests(storageRequests)));
    }

    public static V1Deployment createDeployment(ControllerProperties controllerProperties, ClusterProperties clusterProperties, KubernetesProperties kubernetesProperties, boolean isProxyContext, boolean persistentStorageNeeded) {
        String name;
        Map<String, String> label;

        if (isProxyContext) {
            name = kubernetesProperties.getNaming().getProxyDeployment();
            label = kubernetesProperties.getNaming().getProxyAppLabel();
        } else {
            name = kubernetesProperties.getNaming().getDeployment();
            label = kubernetesProperties.getNaming().getAppLabel();
        }

        return new V1Deployment()
                .metadata(new V1ObjectMeta().name(name)
                        .namespace(kubernetesProperties.getNaming().getNamespace())
                        .labels(label))
                .spec(createDeploymentSpec(controllerProperties, clusterProperties, kubernetesProperties, isProxyContext, persistentStorageNeeded));
    }

    private static V1DeploymentSpec createDeploymentSpec(ControllerProperties controllerProperties, ClusterProperties clusterProperties, KubernetesProperties kubernetesProperties, boolean isProxyContext, boolean persistentStorageNeeded) {
        Map<String, String> label = isProxyContext
                ? kubernetesProperties.getNaming().getProxyAppLabel()
                : kubernetesProperties.getNaming().getAppLabel();

        V1PodSpec podSpec = createPodSpec(clusterProperties, kubernetesProperties, isProxyContext, persistentStorageNeeded);
        V1ObjectMeta metadata = new V1ObjectMeta().labels(label);

        V1PodTemplateSpec template = new V1PodTemplateSpec().metadata(metadata).spec(podSpec);
        V1LabelSelector selector = new V1LabelSelector().matchLabels(label);

        V1RollingUpdateDeployment rollingUpdate = new V1RollingUpdateDeployment()
                .maxSurge(new IntOrString(kubernetesProperties.getUpdate().getMaxSurge()))
                .maxUnavailable(new IntOrString(kubernetesProperties.getUpdate().getMaxUnavailable()));
        V1DeploymentStrategy strategy = new V1DeploymentStrategy().type(kubernetesProperties.getUpdate().getStrategy())
                .rollingUpdate(rollingUpdate);

        return new V1DeploymentSpec()
                .template(template)
                .replicas(isProxyContext
                        ? controllerProperties.getProxyReplicas()
                        : controllerProperties.getInitialReplicas())
                .strategy(strategy)
                .selector(selector);
    }

    private static V1PodSpec createPodSpec(ClusterProperties clusterProperties, KubernetesProperties kubernetesProperties, boolean isProxyPod, boolean persistentStorageNeeded) {
        String image;
        if (isProxyPod) {
            image = clusterProperties.getProxyImage();
        } else {
            image = clusterProperties.getAppImage();
        }

        V1ContainerPort port = new V1ContainerPort().containerPort(clusterProperties.getAppContainerPort());

        V1Container container = new V1Container().name(kubernetesProperties.getNaming().getContainer())
                .image(image)
                .ports(List.of(port));

        if (!isProxyPod && persistentStorageNeeded) {
            V1VolumeMount volumeMount = new V1VolumeMount().mountPath("/var/log").name("storage-volume");
            List<V1VolumeMount> mounts = List.of(volumeMount);
            container.volumeMounts(mounts);
        }

        if (!isProxyPod && clusterProperties.isAppHealthCheck()) {
            V1HTTPGetAction healthCheck = new V1HTTPGetAction().path("/health/check")
                    .port(new IntOrString(clusterProperties.getAppContainerPort()));
            V1Probe livenessProbe = new V1Probe().httpGet(healthCheck).initialDelaySeconds(20)
                    .periodSeconds(10)
                    .timeoutSeconds(3).failureThreshold(1);

            container.livenessProbe(livenessProbe);
        }

        List<V1Container> replicas = List.of(container);

        /*
         * hostNetwork:
         *
         * Flannel has full control of the networking on every node in the cluster
         * allowing it to manage the overlay network to which the pods with
         * hostNetwork: false are connected to.
         */

        V1PodSpec podSpec = new V1PodSpec().hostNetwork(false).containers(replicas);

        if (isProxyPod && clusterProperties.getProxyNodeName() != null) {
            podSpec.nodeName(clusterProperties.getProxyNodeName());
        }

        if (!isProxyPod && persistentStorageNeeded) {
            V1Volume volume = new V1Volume()
                    .name("storage-volume")
                    .persistentVolumeClaim(new V1PersistentVolumeClaimVolumeSource().claimName("storage-pvc"));
            List<V1Volume> volumes = List.of(volume);
            podSpec.volumes(volumes);
        }

        return podSpec;
    }

    public static V1Service createService(ClusterProperties clusterProperties, KubernetesProperties kubernetesProperties, boolean isProxyContext) {
        String name;
        Map<String, String> label;
        V1ServiceSpec serviceSpec;
        V1ServicePort port;

        if (isProxyContext) {
            name = kubernetesProperties.getNaming().getProxyService();
            label = kubernetesProperties.getNaming().getProxyAppLabel();

            port = new V1ServicePort()
                    .name(kubernetesProperties.getNaming().getPort())
                    .port(clusterProperties.getAppContainerPort())
                    .protocol("TCP")
                    .nodePort(kubernetesProperties.getProxyNodePort())
                    .targetPort(new IntOrString(clusterProperties.getAppContainerPort()));

        } else {
            name = kubernetesProperties.getNaming().getService();
            label = kubernetesProperties.getNaming().getAppLabel();

            port = new V1ServicePort()
                    .name(kubernetesProperties.getNaming().getPort())
                    .port(clusterProperties.getAppContainerPort())
                    .protocol("TCP")
                    .nodePort(kubernetesProperties.getApplicationNodePort())
                    .targetPort(new IntOrString(clusterProperties.getAppContainerPort()));
        }

        serviceSpec = new V1ServiceSpec().type("NodePort")
                .ports(List.of(port))
                .selector(label);

        V1ObjectMeta metaData = new V1ObjectMeta()
                .name(name)
                .namespace(kubernetesProperties.getNaming().getNamespace())
                .labels(label);

        return new V1Service().metadata(metaData).spec(serviceSpec);
    }

    public static int getCurrentScale(KubernetesClient client, KubernetesProperties kubernetesProperties) throws ApiException {
        for (int i = 1; i < 3; i++) {
            try {
                V1Scale currentScale = client.getAppsAPI()
                        .readNamespacedDeploymentScale(kubernetesProperties.getNaming().getDeployment(), kubernetesProperties.getNaming().getNamespace(), null);

                return currentScale.getStatus().getReplicas();
            } catch (ApiException apie) {
                if (i < 2) {
                    client.reconnect();
                } else {
                    throw apie;
                }
            }
        }
        throw new IllegalStateException();
    }

    /** SCALE DEPLOYMENT */

    public static void scale(KubernetesClient client, KubernetesProperties kubernetesProperties, V1Patch patch) throws ApiException {
        // alternative:
        // KubernetesClient.getAppsAPI().patchNamespacedReplicaSetScale(...)
        for (int i = 1; i < 3; i++) {
            try {
                client.getAppsAPI().patchNamespacedDeploymentScale(kubernetesProperties.getNaming().getDeployment(),
                        kubernetesProperties.getNaming().getNamespace(), patch, null, null, null, null);
                return;
            } catch (ApiException apie) {
                if (i < 2) {
                    client.reconnect();
                } else {
                    throw apie;
                }
            }
        }
    }

    public static V1Patch createScalePatch(int replicas) {
        return new V1Patch(createScaleJson(replicas));
    }

    /** UPDATE DEPLOYMENT BY PATCH */

    public static void patchDeployment(KubernetesClient client, KubernetesProperties kubernetesProperties, V1Patch patch) throws ApiException {
        for (int i = 1; i < 3; i++) {
            try {
                client.getAppsAPI().patchNamespacedDeployment(kubernetesProperties.getNaming().getDeployment(),
                        kubernetesProperties.getNaming().getNamespace(), patch, null, null, null, null);
                return;
            } catch (ApiException apie) {
                if (i < 2) {
                    client.reconnect();
                } else {
                    throw apie;
                }
            }
        }
    }

    public static V1Patch createUpdatePatch(String updateImage) {
        return new V1Patch(createPatchImageJson(updateImage));
    }

    /** UPDATE DEPLOYMENT BY REPLACE */

    public static void replaceDeployment(KubernetesClient client, KubernetesProperties kubernetesProperties, V1Deployment deployment) throws ApiException {
        for (int i = 1; i < 3; i++) {
            try {
                client.getAppsAPI().replaceNamespacedDeployment(kubernetesProperties.getNaming().getDeployment(),
                        kubernetesProperties.getNaming().getNamespace(), deployment, null, null, null);
                return;
            } catch (ApiException apie) {
                if (i < 2) {
                    client.reconnect();
                } else {
                    throw apie;
                }
            }
        }
    }

    public static V1Deployment createUpdateReplaceDeployment(KubernetesClient client, KubernetesProperties kubernetesProperties, String updateImage) throws ApiException {
        for (int i = 1; i < 3; i++) {
            try {
                V1Deployment deployment = client.getAppsAPI().readNamespacedDeployment(kubernetesProperties.getNaming().getDeployment(),
                        kubernetesProperties.getNaming().getNamespace(), null);

                deployment.getSpec().getTemplate().getSpec().getContainers().get(0).setImage(updateImage);

                return deployment;
            } catch (ApiException apie) {
                if (i < 2) {
                    client.reconnect();
                } else {
                    throw apie;
                }
            }
        }
        throw new IllegalStateException();
    }

    /** HELPER FUNCTIONS */

    private static String createScaleJson(int replicas) {
        return "[{\"op\":\"replace\",\"path\":\"/spec/replicas\",\"value\":" + replicas
                + "}]";
    }

    /**
     * Warning: currently not working - api exception: bad request
     *
     * @param updateImage
     * @return
     */
    private static String createPatchImageJson(String updateImage) {
        return "[{\"op\":\"replace\",\"path\":\"/spec/template/spec/containers/0/image\",\"value\":" + updateImage
                + "}]";
    }
}
