package tools.descartes.coffee.controller.orchestrator.nomad.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("nomad")
public class NomadProperties {
    private String datacenter;
    private String driver;
    private HAProxyProperties haproxy;
    private NamingProperties naming;
    private UpdateProperties update;
    private RestartProperties restart;
    private HealthProperties health;

    public HAProxyProperties getHaproxy() {
        return haproxy;
    }

    public void setHaproxy(HAProxyProperties haproxy) {
        this.haproxy = haproxy;
    }

    public String getDatacenter() {
        return datacenter;
    }

    public void setDatacenter(String datacenter) {
        this.datacenter = datacenter;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
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

    public RestartProperties getRestart() {
        return restart;
    }

    public void setRestart(RestartProperties restart) {
        this.restart = restart;
    }

    public HealthProperties getHealth() {
        return health;
    }

    public void setHealth(HealthProperties health) {
        this.health = health;
    }

    public static class HAProxyProperties {
        private String jobId;
        private String name;
        private int port;
        private int uiPort;
        private int checkIntervalSeconds;
        private int checkTimeoutSeconds;
        private String version;
        private int cpu;
        private int memory;

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getUiPort() {
            return uiPort;
        }

        public void setUiPort(int uiPort) {
            this.uiPort = uiPort;
        }

        public int getCheckIntervalSeconds() {
            return checkIntervalSeconds;
        }

        public void setCheckIntervalSeconds(int checkIntervalSeconds) {
            this.checkIntervalSeconds = checkIntervalSeconds;
        }

        public int getCheckTimeoutSeconds() {
            return checkTimeoutSeconds;
        }

        public void setCheckTimeoutSeconds(int checkTimeoutSeconds) {
            this.checkTimeoutSeconds = checkTimeoutSeconds;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public int getCpu() {
            return cpu;
        }

        public void setCpu(int cpu) {
            this.cpu = cpu;
        }

        public int getMemory() {
            return memory;
        }

        public void setMemory(int memory) {
            this.memory = memory;
        }
    }

    public static class NamingProperties {
        private String prefix;
        private String namespace;
        private String job;
        private String taskGroup;
        private String task;
        private String portLabel;
        private String service;
        private String proxy;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getNamespace() {
            return prefix + namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getJob() {
            return prefix + job;
        }

        public String getJobId() {
            return getJob() + "-id";
        }

        public void setJob(String job) {
            this.job = job;
        }

        public String getTaskGroup() {
            return prefix + taskGroup;
        }

        public void setTaskGroup(String taskGroup) {
            this.taskGroup = taskGroup;
        }

        public String getTaskGroupPortLabel() {
            return getTaskGroup() + "-port-label";
        }

        public String getTask() {
            return prefix + task;
        }

        public void setTask(String task) {
            this.task = task;
        }

        public String getPortLabel() {
            return prefix + portLabel;
        }

        public void setPortLabel(String portLabel) {
            this.portLabel = portLabel;
        }

        public String getService() {
            return prefix + service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getServiceCheck() {
            return getService() + "-check";
        }

        public String getProxy() {
            return prefix + proxy;
        }

        public void setProxy(String proxy) {
            this.proxy = proxy;
        }

        public String getProxyTaskGroup() {
            return getProxy() + taskGroup;
        }

        public String getProxyTask() {
            return getProxy() + task;
        }

        public String getProxyPortLabel() {
            return getProxy() + portLabel;
        }

        public String getProxyService() {
            return getProxy() + service;
        }

        /*
         * ENVIRONMENT
         * https://www.nomadproject.io/docs/runtime/environment
         */

        /* Host IP:Port pair for the given port label */
        public String getEnvVarAddr() {
            return "NOMAD_ADDR_" + portLabel;
        }

        /*
         * Host IP:Port for the given service when defined as a Consul Connect upstream.
         */
        public String getEnvVarServiceAddr() {
            return "NOMAD_UPSTREAM_ADDR_" + service;
        }
    }

    public static class UpdateProperties {
        private int staggerMilliSeconds;
        private int minHealthyTimeMilliSeconds;
        private String healthStatus;
        private int maxParallel;

        public int getStaggerMilliSeconds() {
            return staggerMilliSeconds;
        }

        public void setStaggerMilliSeconds(int staggerMilliSeconds) {
            this.staggerMilliSeconds = staggerMilliSeconds;
        }

        public int getMinHealthyTimeMilliSeconds() {
            return minHealthyTimeMilliSeconds;
        }

        public void setMinHealthyTimeMilliSeconds(int minHealthyTimeMilliSeconds) {
            this.minHealthyTimeMilliSeconds = minHealthyTimeMilliSeconds;
        }

        public String getHealthStatus() {
            return healthStatus;
        }

        public void setHealthStatus(String healthStatus) {
            this.healthStatus = healthStatus;
        }

        public int getMaxParallel() {
            return maxParallel;
        }

        public void setMaxParallel(int maxParallel) {
            this.maxParallel = maxParallel;
        }
    }

    public static class RestartProperties {
        private int delaySeconds;
        private int intervalSeconds;
        private int limit;
        private int graceSeconds;
        private boolean ignoreWarnings;

        public int getDelaySeconds() {
            return delaySeconds;
        }

        public void setDelaySeconds(int delaySeconds) {
            this.delaySeconds = delaySeconds;
        }

        public int getIntervalSeconds() {
            return intervalSeconds;
        }

        public void setIntervalSeconds(int intervalSeconds) {
            this.intervalSeconds = intervalSeconds;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getGraceSeconds() {
            return graceSeconds;
        }

        public void setGraceSeconds(int graceSeconds) {
            this.graceSeconds = graceSeconds;
        }

        public boolean isIgnoreWarnings() {
            return ignoreWarnings;
        }

        public void setIgnoreWarnings(boolean ignoreWarnings) {
            this.ignoreWarnings = ignoreWarnings;
        }
    }

    public static class HealthProperties {
        private int intervalSeconds;
        private int timeoutSeconds;

        public int getIntervalSeconds() {
            return intervalSeconds;
        }

        public void setIntervalSeconds(int intervalSeconds) {
            this.intervalSeconds = intervalSeconds;
        }

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }
    }
}
