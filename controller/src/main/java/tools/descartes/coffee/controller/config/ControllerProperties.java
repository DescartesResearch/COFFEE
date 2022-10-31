package tools.descartes.coffee.controller.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("controller")
public class ControllerProperties {
    private boolean exportResults;
    private String testScript;
    private int initialReplicas;
    private String reportDirectory;
    private int proxyReplicas;
    private int networkingTimeoutSeconds;
    private DatabaseProperties database;

    public boolean isExportResults() {
        return exportResults;
    }

    public void setExportResults(boolean exportResults) {
        this.exportResults = exportResults;
    }

    public String getTestScript() {
        return testScript;
    }

    public void setTestScript(String testScript) {
        this.testScript = testScript;
    }

    public int getInitialReplicas() {
        return initialReplicas;
    }

    public void setInitialReplicas(int initialReplicas) {
        this.initialReplicas = initialReplicas;
    }

    public String getReportDirectory() {
        return reportDirectory;
    }

    public void setReportDirectory(String reportDirectory) {
        this.reportDirectory = reportDirectory;
    }

    public int getProxyReplicas() {
        return proxyReplicas;
    }

    public void setProxyReplicas(int proxyReplicas) {
        this.proxyReplicas = proxyReplicas;
    }

    public int getNetworkingTimeoutSeconds() {
        return networkingTimeoutSeconds;
    }

    public void setNetworkingTimeoutSeconds(int networkingTimeoutSeconds) {
        this.networkingTimeoutSeconds = networkingTimeoutSeconds;
    }

    public DatabaseProperties getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseProperties database) {
        this.database = database;
    }

    public static class DatabaseProperties {
        private String address;
        private int port;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}
