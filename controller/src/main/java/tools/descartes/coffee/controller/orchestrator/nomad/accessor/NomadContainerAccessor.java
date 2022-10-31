package tools.descartes.coffee.controller.orchestrator.nomad.accessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import tools.descartes.coffee.controller.orchestrator.nomad.configuration.NomadProperties;
import tools.descartes.coffee.controller.orchestrator.nomad.util.NomadUtils;
import org.springframework.data.util.Pair;

import com.hashicorp.nomad.apimodel.AllocationListStub;
import com.hashicorp.nomad.apimodel.NetworkResource;
import com.hashicorp.nomad.javasdk.NomadException;

import tools.descartes.coffee.controller.orchestrator.nomad.NomadClient;
import tools.descartes.coffee.controller.orchestrator.ContainerAccessor;

/**
 * Access to container addresses: getAllocationsAPI:info
 */
public class NomadContainerAccessor implements ContainerAccessor {

        private final NomadClient client;
        private final NomadProperties nomadProperties;

        public NomadContainerAccessor(NomadClient client, NomadProperties nomadProperties) {
                this.client = client;
                this.nomadProperties = nomadProperties;
        }

        public String getRandomContainerIp() throws NomadException, IOException {
                List<AllocationListStub> currentAllocs = NomadUtils.getCurrentAllocs(client, nomadProperties);

                if (currentAllocs.isEmpty()) {
                        throw new IllegalStateException(
                                        "Error occurred while getting a pod ip: No pods currently deployed.");
                }

                String randomContainerId = currentAllocs.iterator().next().getId();
                NetworkResource containerNetwork = client.getAllocationsAPI().info(randomContainerId).getValue()
                                .getAllocatedResources().getShared().getNetworks().iterator().next();

                return containerNetwork.getIp() + ":" + containerNetwork.getDynamicPorts().iterator().next().getValue();
        }

        public List<String> getRandomContainerAddresses(int numberOfAddresses) throws NomadException, IOException {
                List<AllocationListStub> currentAllocs = NomadUtils.getCurrentAllocs(client, nomadProperties);
                if (currentAllocs.size() < numberOfAddresses) {
                        throw new IllegalStateException(
                                        "Error occurred while getting " + numberOfAddresses
                                                        + " container allocations: Less containers currently deployed: "
                                                        + currentAllocs.size());
                }

                List<String> randomContainerIds = currentAllocs.subList(0, numberOfAddresses).stream()
                                .map(AllocationListStub::getId).collect(Collectors.toList());

                List<String> addresses = new ArrayList<>();
                for (String id : randomContainerIds) {
                        addresses.add(getAddressForContainerId(id, true));
                }

                return addresses;
        }

        private String getAddressForContainerId(String id, boolean isDynamicPort) throws NomadException, IOException {
                // extract service addresses
                NetworkResource containerNetwork = client.getAllocationsAPI()
                                .info(id).getValue()
                                .getAllocatedResources().getShared().getNetworks().iterator().next();

                int port = isDynamicPort
                                ? containerNetwork.getDynamicPorts().iterator().next().getValue()
                                : containerNetwork.getReservedPorts().iterator().next().getValue();

                return containerNetwork.getIp() + ":"
                                + port;
        }

        @Override
        public List<Pair<String, String>> getNetworkingContainerAddresses() throws NomadException, IOException {
                List<AllocationListStub> currentAllocs = NomadUtils.getCurrentAllocs(client, nomadProperties);

                List<AllocationListStub> distinctAllocs = currentAllocs.stream()
                                .filter(distinctByNodeName(AllocationListStub::getNodeName)).collect(Collectors.toList());

                List<Pair<String, String>> distinctNodeIds = distinctAllocs.stream()
                                .map(alloc -> Pair.of(alloc.getNodeName(), alloc.getId())).collect(Collectors.toList());

                List<Pair<String, String>> nodeAddresses = new ArrayList<>();
                for (Pair<String, String> pair : distinctNodeIds) {
                        nodeAddresses.add(Pair.of(pair.getFirst(), getAddressForContainerId(pair.getSecond(), true)));
                }

                return nodeAddresses;
        }

        private static <T> Predicate<T> distinctByNodeName(
                        Function<? super T, ?> nodeNameExtractor) {

                Map<Object, Boolean> seen = new ConcurrentHashMap<>();
                return pair -> seen.putIfAbsent(nodeNameExtractor.apply(pair), Boolean.TRUE) == null;
        }

        @Override
        public int getCurrentScale() throws Exception {
                return NomadUtils.getCurrentScale(client, nomadProperties);
        }

        @Override
        public String getProxyAddress() throws Exception {
                AllocationListStub proxyAlloc = client.getAllocationsAPI()
                                .list(NomadUtils.getNamespacedQueryOptions(nomadProperties))
                                .getValue()
                                .stream()
                                .filter(alloc -> alloc.getTaskGroup()
                                                .equals(nomadProperties.getNaming().getProxyTaskGroup()))
                                .filter(alloc -> alloc.getTaskStates().values().stream()
                                                .allMatch(state -> state.getState().equals("running")))
                                .findFirst().get();

                String proxyAllocId = proxyAlloc.getId();
                return getAddressForContainerId(proxyAllocId, true);
        }

        @Override
        public String getLoadBalancerAddress() throws Exception {

                AllocationListStub haproxyAlloc = client.getAllocationsAPI()
                                .list(NomadUtils.getNamespacedQueryOptions(nomadProperties))
                                .getValue()
                                .stream()
                                .filter(alloc -> alloc.getTaskGroup()
                                                .equals(nomadProperties.getHaproxy().getName()))
                                .filter(alloc -> alloc.getTaskStates().values().stream()
                                                .allMatch(state -> state.getState().equals("running")))
                                .findFirst().get();

                String haproxyAllocId = haproxyAlloc.getId();
                return getAddressForContainerId(haproxyAllocId, false);
        }

}