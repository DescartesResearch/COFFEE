package tools.descartes.coffee.controller.orchestrator.kubernetes.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.logging.Logger;

import tools.descartes.coffee.controller.config.ClusterProperties;
import tools.descartes.coffee.controller.orchestrator.kubernetes.configuration.KubernetesProperties;
import io.kubernetes.client.util.KubeConfig;

public class KubeConfigConverter {
    private static final Logger logger = Logger.getLogger(KubeConfigConverter.class.getName());

    private KubeConfigConverter() {

    }

    public static KubeConfig convert(ClusterProperties cConfig, KubernetesProperties kConfig) {
        if (kConfig.getKubeConfigFile() != null) {
            try {
                return KubeConfig.loadKubeConfig(new FileReader(kConfig.getKubeConfigFile()));
            } catch (FileNotFoundException fnfe) {
                logger.warning("KubeConfig file could not be found using other properties");
            }
        }
        String currentContext = kConfig.getContext();
        Object preferences = new Object();

        ArrayList<Object> users = new ArrayList<>(List.of(buildUserConfig(kConfig)));
        ArrayList<Object> contexts = new ArrayList<>(List.of(buildContextConfig(kConfig)));
        ArrayList<Object> clusters = new ArrayList<>(List.of(buildClusterConfig(cConfig, kConfig)));

        KubeConfig kubeConfig = new KubeConfig(contexts, clusters, users);

        kubeConfig.setContext(currentContext);
        kubeConfig.setPreferences(preferences);

        return kubeConfig;
    }

    private static Map<String, Object> buildUserConfig(KubernetesProperties config) {
        Map<String, Object> userDetails = new HashMap<>() {
            {
                put("client-certificate-data", config.getClientCertificateData());
                put("client-key-data", config.getClientKeyData());
            }
        };

        return new HashMap<>() {
            {
                put("name", config.getUserName());
                put("user", userDetails);
            }
        };
    }

    private static Map<String, Object> buildClusterConfig(ClusterProperties cConfig, KubernetesProperties kConfig) {
        Map<String, Object> clusterDetails = new HashMap<>() {
            {
                put("certificate-authority-data", kConfig.getCertificateAuthorityData());
                put("server", "https://" + (cConfig.getPort() != -1 ? cConfig.getIp() + ":" + cConfig.getPort() : cConfig.getIp()));
            }
        };

        return new HashMap<>() {
            {
                put("name", kConfig.getClusterName());
                put("cluster", clusterDetails);
            }
        };
    }

    private static Map<String, Object> buildContextConfig(KubernetesProperties config) {
        Map<String, Object> contextDetails = new HashMap<>() {
            {
                put("cluster", config.getClusterName());
                put("namespace", config.getNaming().getNamespace());
                put("user", config.getUserName());
            }
        };

        return new HashMap<>() {
            {
                put("name", config.getContext());
                put("context", contextDetails);
            }
        };
    }
}
