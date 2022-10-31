package tools.descartes.coffee.controller.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("cluster")
public class ClusterProperties {
    private String orchestrator;
    private String ip;
    private int port;
    private String controllerIp;
    private String appImage;
    private String proxyImage;
    private String updateImage;
    private String proxyNodeName;
    private int appContainerPort;
    private boolean appHealthCheck;

    public String getOrchestrator() {
        return orchestrator;
    }

    public void setOrchestrator(String orchestrator) {
        this.orchestrator = orchestrator;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getControllerIp() {
        return controllerIp;
    }

    public void setControllerIp(String controllerIp) {
        this.controllerIp = controllerIp;
    }

    public String getAppImage() {
        return appImage;
    }

    public void setAppImage(String appImage) {
        this.appImage = appImage;
    }

    public String getProxyImage() {
        return proxyImage;
    }

    public void setProxyImage(String proxyImage) {
        this.proxyImage = proxyImage;
    }

    public String getUpdateImage() {
        return updateImage;
    }

    public void setUpdateImage(String updateImage) {
        this.updateImage = updateImage;
    }

    public String getProxyNodeName() {
        if (proxyNodeName.equals("NULL")) {
            return null;
        }
        return proxyNodeName;
    }

    public void setProxyNodeName(String proxyNodeName) {
        this.proxyNodeName = proxyNodeName;
    }

    public int getAppContainerPort() {
        return appContainerPort;
    }

    public void setAppContainerPort(int appContainerPort) {
        this.appContainerPort = appContainerPort;
    }

    public boolean isAppHealthCheck() {
        return appHealthCheck;
    }

    public void setAppHealthCheck(boolean appHealthCheck) {
        this.appHealthCheck = appHealthCheck;
    }
}
