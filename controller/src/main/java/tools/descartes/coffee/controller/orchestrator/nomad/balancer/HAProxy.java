package tools.descartes.coffee.controller.orchestrator.nomad.balancer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.hashicorp.nomad.apimodel.Job;
import com.hashicorp.nomad.apimodel.NetworkResource;
import com.hashicorp.nomad.apimodel.Port;
import com.hashicorp.nomad.apimodel.Resources;
import com.hashicorp.nomad.apimodel.Service;
import com.hashicorp.nomad.apimodel.ServiceCheck;
import com.hashicorp.nomad.apimodel.Task;
import com.hashicorp.nomad.apimodel.TaskGroup;
import com.hashicorp.nomad.apimodel.Template;

import tools.descartes.coffee.controller.orchestrator.nomad.configuration.NomadProperties;
import org.springframework.stereotype.Component;

@Component
public class HAProxy {

    private final NomadProperties nomadProperties;
    private final Job job;

    public HAProxy(NomadProperties nomadProperties) {
        this.nomadProperties = nomadProperties;
        job = createJob();
    }

    public Job getJob() {
        return job;
    }

    private Job createJob() {
        Job job = new Job();

        job.setName(nomadProperties.getHaproxy().getName());
        job.setId(nomadProperties.getHaproxy().getJobId());
        job.setDatacenters(List.of(nomadProperties.getDatacenter()));
        job.setType("service");
        job.setTaskGroups(List.of(createTaskGroup()));

        return job;
    }

    private TaskGroup createTaskGroup() {
        TaskGroup taskGroup = new TaskGroup();

        taskGroup.setName(nomadProperties.getHaproxy().getName());
        taskGroup.setCount(1);
        taskGroup.setNetworks(List.of(createNetwork()));
        taskGroup.setServices(List.of(createService()));
        taskGroup.setTasks(List.of(createTask()));

        return taskGroup;
    }

    private NetworkResource createNetwork() {
        NetworkResource network = new NetworkResource();

        Port http = new Port();
        http.setLabel("http");
        http.setValue(nomadProperties.getHaproxy().getPort());

        Port haproxyUi = new Port();
        haproxyUi.setLabel("haproxy_ui");
        haproxyUi.setValue(nomadProperties.getHaproxy().getUiPort());

        network.setReservedPorts(List.of(http, haproxyUi));
        return network;
    }

    private Service createService() {
        Service service = new Service();
        service.setName(nomadProperties.getHaproxy().getName());

        ServiceCheck check = new ServiceCheck();
        check.setName("alive");
        check.setType("tcp");
        check.setPortLabel("http");
        check.setInterval(TimeUnit.NANOSECONDS.convert(nomadProperties.getHaproxy().getCheckIntervalSeconds(), TimeUnit.SECONDS));
        check.setTimeout(TimeUnit.NANOSECONDS.convert(nomadProperties.getHaproxy().getCheckTimeoutSeconds(), TimeUnit.SECONDS));

        service.setChecks(List.of(check));
        return service;
    }

    private Task createTask() {
        Task task = new Task();

        task.setName(nomadProperties.getHaproxy().getName());
        task.setDriver(nomadProperties.getDriver());
        task.setConfig(createTaskConfig());
        task.setTemplates(List.of(createTaskTemplate()));
        task.setResources(createTaskResources());

        return task;
    }

    private Map<String, Object> createTaskConfig() {
        return Map.of(
                "image", "haproxy:" + nomadProperties.getHaproxy().getVersion(),
                "network_mode", "host"
                // , "ports", List.of(Map.of("http", 80))
                , "volumes", new String[] {
                        "local/haproxy.cfg:/usr/local/etc/haproxy/haproxy.cfg" });
    }

    /**
     * alternatively stored as haproxy.cfg in nodes
     * 
     * @return
     */
    private Template createTaskTemplate() {
        Template template = new Template();

        template.setEmbeddedTmpl(
                "defaults\n" +
                        "   mode http\n" +
                        " timeout connect 5s\n" +
                        " timeout client 1m\n" +
                        " timeout server 1m\n" +
                        "\n" +
                        "frontend stats\n" +
                        "   bind *:" + nomadProperties.getHaproxy().getUiPort() + "\n" +
                        // " stats enable\n" +
                        "   stats uri /\n" +
                        // " stats refresh 10s\n" +
                        "   stats show-legends\n" +
                        "   no log\n" +
                        "\n" +
                        "frontend http_front\n" +
                        "   bind *:" + nomadProperties.getHaproxy().getPort() + "\n" +
                        "   default_backend http_back\n" +
                        "\n" +
                        "backend http_back\n" +
                        "    balance roundrobin\n" +
                        createServerTemplate() +
                        "\n" +
                        "resolvers consul\n" +
                        "    nameserver consul 127.0.0.1:8600\n" +
                        "    accepted_payload_size 8192\n" +
                        "    hold valid 5s\n"

        );

        /*
         * dest to local/haproxy.cfg and then provide as volume in
         * /usr/local/etc/haproxy/haproxy.cfg (see task config above)
         */
        template.setDestPath("local/haproxy.cfg");

        return template;
    }

    private String createServerTemplate() {
        return "    server-template test_app 10 _"
                + nomadProperties.getNaming().getService()
                + "._tcp.service.consul resolvers consul resolve-opts allow-dup-ip resolve-prefer ipv4 check\n";
    }

    private Resources createTaskResources() {
        Resources res = new Resources();
        res.setCpu(nomadProperties.getHaproxy().getCpu());
        res.setMemoryMb(nomadProperties.getHaproxy().getMemory());
        return res;
    }

}
