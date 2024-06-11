package tools.descartes.coffee.controller.orchestrator.nomad.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.hashicorp.nomad.apimodel.*;
import com.hashicorp.nomad.javasdk.NomadException;
import com.hashicorp.nomad.javasdk.QueryOptions;
import com.hashicorp.nomad.javasdk.WriteOptions;

import tools.descartes.coffee.controller.config.ClusterProperties;
import tools.descartes.coffee.controller.config.ControllerProperties;
import tools.descartes.coffee.controller.orchestrator.nomad.NomadClient;
import tools.descartes.coffee.controller.orchestrator.nomad.NomadComponents;
import tools.descartes.coffee.controller.orchestrator.nomad.configuration.NomadProperties;
import tools.descartes.coffee.controller.procedure.collection.deployment.UpdateContainer;
import tools.descartes.coffee.shared.AppVersion;

/*
 * Helper class to provide deployment specific functions.
 */
public final class NomadUtils {
    public static <T> QueryOptions<T> getNamespacedQueryOptions(NomadProperties nomadProperties) {
        QueryOptions<T> options = new QueryOptions<>();
        options.setNamespace(nomadProperties.getNaming().getNamespace());
        return options;
    }

    public static WriteOptions getNamespacedWriteOptions(NomadProperties nomadProperties) {
        WriteOptions options = new WriteOptions("global");
        options.setNamespace(nomadProperties.getNaming().getNamespace());
        return options;
    }

    public static List<AllocationListStub> getCurrentAllocs(NomadClient nomadClient, NomadProperties nomadProperties) throws IOException, NomadException {
        // get all running allocations from task group

        return nomadClient.getAllocationsAPI()
                .list(getNamespacedQueryOptions(nomadProperties))
                .getValue().stream()
                .filter(alloc -> alloc.getTaskGroup().equals(nomadProperties.getNaming().getTaskGroup()))
                .filter(alloc -> alloc.getTaskStates().values().stream()
                        .allMatch(state -> state.getState().equals("running")))
                .collect(Collectors.toList());
    }

    public static int getCurrentScale(NomadClient client, NomadProperties nomadProperties) throws NomadException, IOException {
        return (int) client.getAllocationsAPI()
                .list(getNamespacedQueryOptions(nomadProperties))
                .getValue().stream()
                .filter(alloc -> alloc.getTaskGroup().equals(nomadProperties.getNaming().getTaskGroup()))
                .filter(alloc -> alloc.getTaskStates().values().stream()
                        .allMatch(state -> state.getState().equals("running")))
                .count();
    }

    /* SCALE DEPLOYMENT */

    /**
     * jobs:scaleGroup results in error: job scaling blocked due to active
     * deployment
     * 
     * workaround via job:register as update of the count
     * 
     * @param count
     * @throws NomadException
     * @throws IOException
     */
    public static void scale(NomadClient client, NomadComponents nomadComponents, NomadProperties nomadProperties, int count) throws NomadException, IOException {
        boolean useVersion2 = UpdateContainer.getCurrentAppVersion().equals(AppVersion.V2);
        Job update = createJob(nomadComponents, nomadProperties, useVersion2, count);
        client.getJobsAPI().register(update);

        /*
         * note - Nomad CLI: nomad job scale <job-id> <group-name> count
         * works fine
         * 
         * NomadClient.getJobsAPI().scaleGroup(NomadDefaultConfig.JOB_ID,
         * NomadDefaultConfig.TASK_GROUP, count,
         * "Scaling deployment to " + count + " containers.", null,
         * getNamespacedWriteOptions());
         */
    }

    /* UPDATE DEPLOYMENT BY REGISTER CALL */

    public static Job prepareUpdateJob(NomadComponents nomadComponents, NomadProperties nomadProperties, int scale) throws NomadException, IOException {
        boolean isV2Update = UpdateContainer.getCurrentAppVersion().equals(AppVersion.V1);
        return createJob(nomadComponents, nomadProperties, isV2Update, scale);
    }

    /**
     * 
     * @param updatedJob job with modified count
     * @throws NomadException
     * @throws IOException
     */
    public static void updateDeployment(NomadClient client, Job updatedJob) throws NomadException, IOException {
        client.getJobsAPI().register(updatedJob);
    }

    /* ### JOB ### */

    /**
     * https://www.nomadproject.io/api-docs/jobs
     *
     * @return
     */
    public static Job createJob(NomadComponents nomadComponents, NomadProperties nomadProperties, boolean isV2Update, int replicas) {
        Job job = new Job();

        job.setName(nomadProperties.getNaming().getJob());
        job.setId(nomadProperties.getNaming().getJobId());
        job.setType("service");
        job.setNamespace(nomadProperties.getNaming().getNamespace());
        job.setTaskGroups(List.of(createAppTaskGroupByCount(nomadComponents, nomadProperties, isV2Update, replicas), nomadComponents.getProxyTaskGroup()));
        job.setDatacenters(List.of("dc1"));

        return job;
    }

    /**
     * https://www.nomadproject.io/api-docs/jobs
     *
     * @return
     */
    public static Job createDefaultJob(NomadComponents nomadComponents, NomadProperties nomadProperties) {
        Job job = new Job();

        job.setName(nomadProperties.getNaming().getJob());
        job.setId(nomadProperties.getNaming().getJobId());
        job.setType("service");
        job.setNamespace(nomadProperties.getNaming().getNamespace());
        job.setTaskGroups(List.of(nomadComponents.getTaskGroup(), nomadComponents.getProxyTaskGroup()));
        job.setDatacenters(List.of("dc1"));

        // TBD
        // job.setUpdate(update)

        return job;
    }

    /* ### NAMESPACE ### */

    public static Namespace createNamespace(NomadProperties nomadProperties) {
        Namespace namespace = new Namespace();
        namespace.setName(nomadProperties.getNaming().getNamespace());
        namespace.setDescription("Container Orchestration Framework Testing Namespace");

        return namespace;
    }

    /* ### TASK GROUP ### */

    public static TaskGroup createTaskGroup(NomadComponents nomadComponents, ControllerProperties controllerProperties, NomadProperties nomadProperties, boolean isProxy, boolean isUpdate, boolean persistentStorageNeeded) {
        if (isProxy) {
            return createProxyTaskGroup(nomadComponents, controllerProperties, nomadProperties);
        } else {
            return createAppTaskGroupByCount(nomadComponents, nomadProperties, isUpdate, controllerProperties.getInitialReplicas(), persistentStorageNeeded);
        }
    }

    public static TaskGroup createProxyTaskGroup(NomadComponents nomadComponents, ControllerProperties controllerProperties, NomadProperties nomadProperties) {
        TaskGroup taskGroup = new TaskGroup();

        taskGroup.setName(nomadProperties.getNaming().getProxyTaskGroup());
        taskGroup.setCount(controllerProperties.getProxyReplicas());
        taskGroup.setTasks(List.of(nomadComponents.getProxyTask()));
        taskGroup.setServices(List.of(nomadComponents.getProxyTaskGroupService()));
        taskGroup.setNetworks(List.of(nomadComponents.getProxyTaskGroupNetwork()));

        return taskGroup;

    }

    public static TaskGroup createAppTaskGroupByCount(NomadComponents nomadComponents, NomadProperties nomadProperties, boolean isUpdate, int count, boolean persistentStorageNeeded) {
        TaskGroup taskGroup = new TaskGroup();

        taskGroup.setName(nomadProperties.getNaming().getTaskGroup());
        taskGroup.setUpdate(nomadComponents.getRollingUpdateStrategy());
        taskGroup.setTasks(List.of(isUpdate ? nomadComponents.getTaskUpdate() : nomadComponents.getTask()));
        taskGroup.setServices(List.of(nomadComponents.getTaskGroupService()));
        taskGroup.setNetworks(List.of(nomadComponents.getTaskGroupNetwork()));
        taskGroup.setCount(count);
        taskGroup.setRestartPolicy(nomadComponents.getRestartPolicy());

        if (persistentStorageNeeded) {
            Map<String, VolumeRequest> vols = new HashMap<>();
            vols.put(nomadProperties.getStorage().getVolumeSource(), new VolumeRequest()
                    .setType(nomadProperties.getStorage().getVolumeType())
                    .setSource(nomadProperties.getStorage().getVolumeSource())
                    .setReadOnly(false))
            taskGroup.setVolumes(vols);
        }

        return taskGroup;
    }

    /**
     * https://www.nomadproject.io/docs/job-specification/update
     *
     * @return
     */
    public static UpdateStrategy getUpdateStrategy(NomadProperties nomadProperties) {
        UpdateStrategy strategy = new UpdateStrategy();

        strategy.setMaxParallel(nomadProperties.getUpdate().getMaxParallel());
        strategy.setMinHealthyTime(TimeUnit.NANOSECONDS.convert(nomadProperties.getUpdate().getMinHealthyTimeMilliSeconds(), TimeUnit.MILLISECONDS));
        strategy.setStagger(TimeUnit.NANOSECONDS.convert(nomadProperties.getUpdate().getStaggerMilliSeconds(), TimeUnit.MILLISECONDS));

        /*
         * https://www.nomadproject.io/docs/job-specification/update#health_check
         *
         * TBD: "task_states" for better comparability, "checks" default
         */
        strategy.setHealthCheck(nomadProperties.getUpdate().getHealthStatus());

        return strategy;
    }

    /**
     * https://www.nomadproject.io/docs/job-specification/restart
     *
     * @return
     */
    public static RestartPolicy getRestartPolicy(NomadProperties nomadProperties) {
        RestartPolicy restart = new RestartPolicy();

        restart.setAttempts(50);
        restart.setDelay(TimeUnit.NANOSECONDS.convert(nomadProperties.getRestart().getDelaySeconds(), TimeUnit.SECONDS));
        restart.setInterval(TimeUnit.NANOSECONDS.convert(nomadProperties.getRestart().getIntervalSeconds(), TimeUnit.SECONDS));

        return restart;
    }

    /**
     * https://www.nomadproject.io/docs/job-specification/network
     *
     * https://www.nomadproject.io/docs/job-specification
     * setting a static port will restrict this task to running once per host
     *
     * @return
     */
    public static NetworkResource createGroupNetwork(ClusterProperties clusterProperties, NomadProperties nomadProperties, boolean isProxy) {
        NetworkResource network = new NetworkResource();

        Port taskPort = new Port();
        if (!isProxy) {
            taskPort.setLabel(nomadProperties.getNaming().getPortLabel());
            taskPort.setTo(clusterProperties.getAppContainerPort());
        } else {
            taskPort.setLabel(nomadProperties.getNaming().getProxyPortLabel());
            taskPort.setTo(8080);
        }

        network.setDynamicPorts(List.of(taskPort));

        return network;
    }

    /* ### TASK ### */

    public static Task createTask(NomadComponents nomadComponents, NomadProperties nomadProperties, boolean isProxy, boolean isUpdate, boolean persistentStorageNeeded) {
        Task task = new Task();

        task.setDriver("docker");

        if (isProxy) {
            task.setName(nomadProperties.getNaming().getProxyTask());
            // task.setServices(List.of(PROXY_SERVICE));
            task.setConfig(nomadComponents.getProxyTaskConfig());
        } else {
            task.setName(nomadProperties.getNaming().getTask());
            // task.setServices(List.of(SERVICE));
            task.setConfig(isUpdate ? nomadComponents.getTaskUpdateConfig() : nomadComponents.getTaskConfig());

            if (persistentStorageNeeded) {
                task.setVolumeMounts(List.of(new VolumeMount().setDestination("/var/log").setReadOnly(false).setVolume(nomadProperties.getStorage().getVolumeSource())));
            }
        }

        return task;
    }

    public static Map<String, Object> createTaskConfig(ClusterProperties clusterProperties, NomadProperties nomadProperties, boolean isProxy, boolean isUpdate) {
        if (isProxy) {
            return Map.of(
                    "image", clusterProperties.getProxyImage(),
                    "ports", new String[] { nomadProperties.getNaming().getProxyPortLabel()});
        } else {
            return Map.of(
                    "image", isUpdate ? clusterProperties.getUpdateImage() : clusterProperties.getAppImage(),
                    "ports", new String[] { nomadProperties.getNaming().getPortLabel()});
        }

        // TBD - args = [...] ?
    }

    /* ### SERVICE ### */

    public static Service createGroupService(ClusterProperties clusterProperties, NomadProperties nomadProperties, boolean isProxy) {
        Service service = new Service();

        if (isProxy) {
            service.setName(nomadProperties.getNaming().getProxyService());
            service.setPortLabel(nomadProperties.getNaming().getProxyPortLabel());
            // service.setAddressMode("alloc");
        } else {
            service.setName(nomadProperties.getNaming().getService());
            service.setPortLabel(nomadProperties.getNaming().getPortLabel());
            service.setAddressMode("auto");
            // service.setAddressMode("alloc");

            // comment out for crash restart
            if (clusterProperties.isAppHealthCheck()) service.setChecks(List.of(getServiceCheck(nomadProperties)));
        }

        return service;
    }

    public static ServiceCheck getServiceCheck(NomadProperties nomadProperties) {
        ServiceCheck check = new ServiceCheck();
        // TBD - check.setExpose(true);

        check.setName(nomadProperties.getNaming().getServiceCheck());
        check.setType("http");
        check.setPath("/health/check");
        check.setPortLabel(nomadProperties.getNaming().getPortLabel());
        check.setInterval(TimeUnit.NANOSECONDS.convert(nomadProperties.getHealth().getIntervalSeconds(), TimeUnit.SECONDS));
        check.setTimeout(TimeUnit.NANOSECONDS.convert(nomadProperties.getHealth().getTimeoutSeconds(), TimeUnit.SECONDS));

        CheckRestart checkRestart = new CheckRestart();
        checkRestart.setLimit(nomadProperties.getRestart().getLimit());
        checkRestart.setGrace(TimeUnit.NANOSECONDS.convert(nomadProperties.getRestart().getGraceSeconds(), TimeUnit.SECONDS));
        checkRestart.setIgnoreWarnings(nomadProperties.getRestart().isIgnoreWarnings());

        check.setCheckRestart(checkRestart);
        return check;
    }
}
