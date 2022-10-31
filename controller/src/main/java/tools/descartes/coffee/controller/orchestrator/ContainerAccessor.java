package tools.descartes.coffee.controller.orchestrator;

import java.util.List;

import org.springframework.data.util.Pair;

public interface ContainerAccessor {

    /**
     * 
     * @return proxy (service) address
     * @throws Exception
     */
    String getProxyAddress() throws Exception;

    /**
     * 
     * @return address for load generator
     * @throws Exception
     */
    String getLoadBalancerAddress() throws Exception;

    /**
     * 
     * @param numberOfIps #addresses
     * @return container addresses to send crash or health requests
     * @throws Exception
     */
    List<String> getRandomContainerAddresses(int numberOfIps) throws Exception;

    /**
     * 
     * 
     * @return pair: node names + inner container IP addresses to send in-cluster
     *         networking requests
     * @throws Exception
     */
    List<Pair<String, String>> getNetworkingContainerAddresses() throws Exception;

    /**
     * 
     * @return current scale of cluster deployment
     * @throws Exception
     */
    int getCurrentScale() throws Exception;
}
