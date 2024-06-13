package tools.descartes.coffee.controller.orchestrator.nomad;

import java.util.Map;

import com.hashicorp.nomad.apimodel.Job;
import com.hashicorp.nomad.apimodel.Namespace;
import com.hashicorp.nomad.apimodel.NetworkResource;
import com.hashicorp.nomad.apimodel.RestartPolicy;
import com.hashicorp.nomad.apimodel.Service;
import com.hashicorp.nomad.apimodel.Task;
import com.hashicorp.nomad.apimodel.TaskGroup;
import com.hashicorp.nomad.apimodel.UpdateStrategy;

import tools.descartes.coffee.controller.config.ClusterProperties;
import tools.descartes.coffee.controller.config.ControllerProperties;
import tools.descartes.coffee.controller.orchestrator.nomad.configuration.NomadProperties;
import tools.descartes.coffee.controller.orchestrator.nomad.util.NomadUtils;
import org.springframework.stereotype.Component;

@Component
public class NomadComponents {

        private ControllerProperties controllerProperties;
        private ClusterProperties clusterProperties;
        private NomadProperties nomadProperties;

        private UpdateStrategy rollingUpdateStrategy;
        private RestartPolicy restartPolicy;

        /* TEST APP */
        private Service taskGroupService;
        private NetworkResource taskGroupNetwork;
        private Map<String, Object> taskConfig;
        private Task task;
        private TaskGroup taskGroup;

        /* TEST APP - UPDATE */
        private Map<String, Object> taskUpdateConfig;
        private Task taskUpdate;
        private TaskGroup taskUpdateGroup;

        /* PROXY */
        private Service proxyTaskGroupService;
        private NetworkResource proxyTaskGroupNetwork;
        private Map<String, Object> proxyTaskConfig;
        private Task proxyTask;
        private TaskGroup proxyTaskGroup;

        private Namespace namespace;
        private Job job;

        public NomadComponents(ControllerProperties controllerProperties, ClusterProperties clusterProperties,
                               NomadProperties nomadProperties) {
                this.controllerProperties = controllerProperties;
                this.clusterProperties = clusterProperties;
                this.nomadProperties = nomadProperties;
        }

        public void init(boolean persistentStorageNeeded) {
                rollingUpdateStrategy = NomadUtils.getUpdateStrategy(nomadProperties);
                restartPolicy = NomadUtils.getRestartPolicy(nomadProperties);
                taskGroupService = NomadUtils.createGroupService(clusterProperties, nomadProperties, false);
                taskGroupNetwork = NomadUtils.createGroupNetwork(clusterProperties, nomadProperties, false);
                taskConfig = NomadUtils.createTaskConfig(clusterProperties, nomadProperties, false, false);
                taskUpdateConfig = NomadUtils.createTaskConfig(clusterProperties, nomadProperties, false, true);
                proxyTaskGroupService = NomadUtils.createGroupService(clusterProperties, nomadProperties, true);
                proxyTaskGroupNetwork = NomadUtils.createGroupNetwork(clusterProperties, nomadProperties, true);
                proxyTaskConfig = NomadUtils.createTaskConfig(clusterProperties, nomadProperties, true, false);
                namespace = NomadUtils.createNamespace(nomadProperties);
                task = NomadUtils.createTask(this, nomadProperties, false, false, persistentStorageNeeded);
                taskGroup = NomadUtils.createTaskGroup(this, controllerProperties, nomadProperties, false, false, persistentStorageNeeded);
                taskUpdate = NomadUtils.createTask(this, nomadProperties, false, true, persistentStorageNeeded);
                taskUpdateGroup = NomadUtils.createTaskGroup(this, controllerProperties, nomadProperties, false, true, persistentStorageNeeded);
                proxyTask = NomadUtils.createTask(this, nomadProperties, true, false, persistentStorageNeeded);
                proxyTaskGroup = NomadUtils.createTaskGroup(this, controllerProperties, nomadProperties, true, false, persistentStorageNeeded);
                job = NomadUtils.createDefaultJob(this, nomadProperties);
        }

        public UpdateStrategy getRollingUpdateStrategy() {
                return rollingUpdateStrategy;
        }

        public RestartPolicy getRestartPolicy() {
                return restartPolicy;
        }

        public Service getTaskGroupService() {
                return taskGroupService;
        }

        public NetworkResource getTaskGroupNetwork() {
                return taskGroupNetwork;
        }

        public Map<String, Object> getTaskConfig() {
                return taskConfig;
        }

        public Task getTask() {
                return task;
        }

        public TaskGroup getTaskGroup() {
                return taskGroup;
        }

        public Map<String, Object> getTaskUpdateConfig() {
                return taskUpdateConfig;
        }

        public Task getTaskUpdate() {
                return taskUpdate;
        }

        public TaskGroup getTaskUpdateGroup() {
                return taskUpdateGroup;
        }

        public Service getProxyTaskGroupService() {
                return proxyTaskGroupService;
        }

        public NetworkResource getProxyTaskGroupNetwork() {
                return proxyTaskGroupNetwork;
        }

        public Map<String, Object> getProxyTaskConfig() {
                return proxyTaskConfig;
        }

        public Task getProxyTask() {
                return proxyTask;
        }

        public TaskGroup getProxyTaskGroup() {
                return proxyTaskGroup;
        }

        public Namespace getNamespace() {
                return namespace;
        }

        public Job getJob() {
                return job;
        }
}
