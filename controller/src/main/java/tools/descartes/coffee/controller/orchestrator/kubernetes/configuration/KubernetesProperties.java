package tools.descartes.coffee.controller.orchestrator.kubernetes.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties("kubernetes")
public class KubernetesProperties {
    private String kubeConfigFile;
    private String userName;
    private String clusterName;
    private String clientCertificateData;
    private String clientKeyData;
    private String certificateAuthorityData;
    private int applicationNodePort;
    private int proxyNodePort;
    private String ipForLoadAndProxy;

    private String storageClassName;
    private NamingProperties naming;
    private UpdateProperties update;

    public String getKubeConfigFile() {
        if (kubeConfigFile.equals("NULL")) {
            return null;
        }
        return kubeConfigFile;
    }

    public void setKubeConfigFile(String kubeConfigFile) {
        this.kubeConfigFile = kubeConfigFile;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getContext() {
        return userName + "@" + clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClientCertificateData() {
        return clientCertificateData;
    }

    public void setClientCertificateData(String clientCertificateData) {
        this.clientCertificateData = clientCertificateData;
    }

    public String getClientKeyData() {
        return clientKeyData;
    }

    public void setClientKeyData(String clientKeyData) {
        this.clientKeyData = clientKeyData;
    }

    public String getCertificateAuthorityData() {
        return certificateAuthorityData;
    }

    public void setCertificateAuthorityData(String certificateAuthorityData) {
        this.certificateAuthorityData = certificateAuthorityData;
    }

    public int getApplicationNodePort() {
        return applicationNodePort;
    }

    public void setApplicationNodePort(int applicationNodePort) {
        this.applicationNodePort = applicationNodePort;
    }

    public int getProxyNodePort() {
        return proxyNodePort;
    }

    public void setProxyNodePort(int proxyNodePort) {
        this.proxyNodePort = proxyNodePort;
    }

    public String getIpForLoadAndProxy() {
        if (ipForLoadAndProxy.equals("NULL")) {
            return null;
        }
        return ipForLoadAndProxy;
    }

    public void setIpForLoadAndProxy(String ipForLoadAndProxy) {
        this.ipForLoadAndProxy = ipForLoadAndProxy;
    }

    public String getStorageClassName() {
        if (storageClassName.equals("NULL")) {
            return null;
        }
        return storageClassName;
    }

    public void setStorageClassName(String storageClassName) {
        this.storageClassName = storageClassName;
    }

    public NamingProperties getNaming() {
        return naming;
    }

    public void setNaming(NamingProperties naming) {
        this.naming = naming;
    }

    public UpdateProperties getUpdate() {
        return update;
    }

    public void setUpdate(UpdateProperties update) {
        this.update = update;
    }

    public static class NamingProperties {
        private String prefix;
        private String proxyDeployment;
        private String proxyService;
        private String proxyLabel;
        private String namespace;
        private String label;
        private String deployment;
        private String service;
        private String container;
        private String port;

        public Map<String, String> getProxyAppLabel() {
            return new HashMap<>() {
                {
                    put("app", prefix + proxyLabel);
                }
            };
        }

        public Map<String, String> getAppLabel() {
            return new HashMap<>() {
                {
                    put("app", prefix + label);
                }
            };
        }

        public String getPrefix() {
            return prefix;
        }

        public String getProxyDeployment() {
            return prefix + proxyDeployment;
        }

        public String getProxyService() {
            return prefix + proxyService;
        }

        public String getProxyLabel() {
            return prefix + proxyLabel;
        }

        public String getNamespace() {
            return prefix + namespace;
        }

        public String getLabel() {
            return prefix + label;
        }

        public String getDeployment() {
            return prefix + deployment;
        }

        public String getService() {
            return prefix + service;
        }

        public String getContainer() {
            return prefix + container;
        }

        public String getPort() {
            return prefix + port;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public void setProxyDeployment(String proxyDeployment) {
            this.proxyDeployment = proxyDeployment;
        }

        public void setProxyService(String proxyService) {
            this.proxyService = proxyService;
        }

        public void setProxyLabel(String proxyLabel) {
            this.proxyLabel = proxyLabel;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public void setDeployment(String deployment) {
            this.deployment = deployment;
        }

        public void setService(String service) {
            this.service = service;
        }

        public void setContainer(String container) {
            this.container = container;
        }

        public void setPort(String port) {
            this.port = port;
        }
    }


    public static class UpdateProperties {
        private String strategy;
        private String maxSurge;
        private String maxUnavailable;

        public String getStrategy() {
            return strategy;
        }

        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }

        public String getMaxSurge() {
            return maxSurge;
        }

        public void setMaxSurge(String maxSurge) {
            this.maxSurge = maxSurge;
        }

        public String getMaxUnavailable() {
            return maxUnavailable;
        }

        public void setMaxUnavailable(String maxUnavailable) {
            this.maxUnavailable = maxUnavailable;
        }
    }


}
