package tools.descartes.coffee.controller.orchestrator.nomad;

import tools.descartes.coffee.controller.config.ClusterProperties;
import tools.descartes.coffee.controller.config.ControllerProperties;
import tools.descartes.coffee.controller.orchestrator.BaseClusterClient;
import tools.descartes.coffee.controller.orchestrator.nomad.balancer.HAProxy;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hashicorp.nomad.apimodel.Node;
import com.hashicorp.nomad.javasdk.AgentApi;
import com.hashicorp.nomad.javasdk.AllocationsApi;
import com.hashicorp.nomad.javasdk.ClientApi;
import com.hashicorp.nomad.javasdk.DeploymentsApi;
import com.hashicorp.nomad.javasdk.JobsApi;
import com.hashicorp.nomad.javasdk.NamespacesApi;
import com.hashicorp.nomad.javasdk.NodesApi;
import com.hashicorp.nomad.javasdk.NomadApiClient;
import com.hashicorp.nomad.javasdk.NomadApiConfiguration;
import com.hashicorp.nomad.javasdk.NomadException;
import com.hashicorp.nomad.javasdk.ScalingApi;
import com.hashicorp.nomad.javasdk.StatusApi;

import tools.descartes.coffee.controller.orchestrator.nomad.configuration.NomadProperties;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.function.FailableSupplier;

public class NomadClient extends BaseClusterClient {
    private static final Logger logger = Logger.getLogger(NomadClient.class.getName());
    private final NomadApiConfiguration nomadConfiguration;
    private final NomadProperties nomadProperties;
    private final NomadComponents nomadComponents;
    private final HAProxy haProxy;
    private NomadApiClient client;
    private boolean connected = false;

    public NomadClient(NomadComponents nomadComponents, ControllerProperties controllerProperties, ClusterProperties clusterConfig, NomadProperties nomadProperties, HAProxy haProxy) {
        super(controllerProperties, clusterConfig);
        this.nomadProperties = nomadProperties;
        this.nomadComponents = nomadComponents;
        this.haProxy = haProxy;
        nomadConfiguration = new NomadApiConfiguration.Builder()
                .setAddress("http://" + clusterConfig.getIp() + ":" + clusterConfig.getPort())
                .build();
    }

    public NodesApi getNodesAPI() {
        if (connected) {
            return client.getNodesApi();
        } else {
            throw new IllegalStateException("Trying to access the nomad nodes api while disconnected");
        }
    }

    public NamespacesApi getNamespacesAPI() {
        if (connected) {
            return client.getNamespacesApi();
        } else {
            throw new IllegalStateException("Trying to access the nomad namespaces api while disconnected");
        }
    }

    public AgentApi getAgentAPI() {
        if (connected) {
            return client.getAgentApi();
        } else {
            throw new IllegalStateException("Trying to access the nomad agent api while disconnected");
        }
    }

    public DeploymentsApi getDeploymentsAPI() {
        if (connected) {
            return client.getDeploymentsApi();
        } else {
            throw new IllegalStateException("Trying to access the nomad deployments api while disconnected");
        }
    }

    public JobsApi getJobsAPI() {
        if (connected) {
            return client.getJobsApi();
        } else {
            throw new IllegalStateException("Trying to access the nomad jobs api while disconnected");
        }
    }

    public AllocationsApi getAllocationsAPI() {
        if (connected) {
            return client.getAllocationsApi();
        } else {
            throw new IllegalStateException("Trying to access the nomad allocations api while disconnected");
        }
    }

    public ClientApi getClientAPI(Node node) {
        if (connected) {
            return client.getClientApi(node);
        } else {
            throw new IllegalStateException("Trying to access the nomad client api while disconnected");
        }
    }

    public StatusApi getStatusAPI() {
        if (connected) {
            return client.getStatusApi();
        } else {
            throw new IllegalStateException("Trying to access the nomad status api while disconnected");
        }
    }

    public ScalingApi getScalingAPI() {
        if (connected) {
            return client.getScalingApi();
        } else {
            throw new IllegalStateException("Trying to access the nomad scaling api while disconnected");
        }
    }

    @Override
    public void connect() {
        client = new NomadApiClient(nomadConfiguration);
        connected = true;
    }

    @Override
    public void init(boolean persistentStorageNeeded) {
        nomadComponents.init(persistentStorageNeeded);
        callAPI("namespace:register", () -> getNamespacesAPI().register(nomadComponents.getNamespace()));
        client.setNamespace(nomadProperties.getNaming().getNamespace());

        callAPI("jobs:deregister", () -> getJobsAPI().deregister(nomadProperties.getHaproxy().getJobId()));
        callAPI("jobs:deregister", () -> getJobsAPI().deregister(nomadProperties.getNaming().getJobId()));

        callAPI("jobs:register", () -> getJobsAPI().register(nomadComponents.getJob()));

        // register load balancer job
        callAPI("jobs:register", () -> getJobsAPI().register(haProxy.getJob()));

        // TODO: check & create environment

        clean();
    }

    @Override
    public void clear() {
        clean();

        // TODO
    }

    @Override
    public void disconnect() {
        clean();
        try {
            client.close();
        } catch (IOException e) {
            NomadClient.logger.log(Level.SEVERE, "Error occurred closing Nomad client.", e);
        }
        connected = false;
    }

    /**
     * SystemApi:garbageCollect
     */
    public void clean() {
        try {
            client.getSystemApi().garbageCollect();
        } catch (IOException e1) {
            logger.warning("Unable to garbage collect nomad cluster (I/O): " + e1.getMessage());
        } catch (NomadException e2) {
            logger.warning("Unable to garbage collect nomad cluster (Nomad): " + e2.getMessage());
        }

    }

    /**
     * Nomad calls throw APIException or IOException
     */

    private <T> T callAPI(String context, FailableSupplier<T, Exception> call) {
        try {
            return call.get();
        } catch (NomadException e) {
            this.throwApiException(context, e.getMessage(), e);
            return null;
        } catch (Exception e) {
            this.throwIOExceptionException(context, e.getMessage(), e);
            return null;
        }
    }

    private <T, R> T callAPI(String context, FailableFunction<R, T, NomadException> call, R arg) {
        try {
            return call.apply(arg);
        } catch (NomadException e) {
            logger.warning("Argument in NomadException: " + arg);
            this.throwApiException(context, e.getMessage(), e);
            return null;
        }
    }

    private void throwApiException(String context, String message, Throwable cause) {
        throw new IllegalStateException(
                "API Exception while accessing " + context + " Nomad client API:\n" + message, cause);
    }

    private void throwIOExceptionException(String context, String message, Throwable cause) {
        throw new IllegalStateException(
                "IOException while accessing " + context + " Nomad client API:\n" + message, cause);
    }
}
