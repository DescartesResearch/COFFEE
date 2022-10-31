package tools.descartes.coffee.controller.monitoring.reporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import tools.descartes.coffee.controller.config.ControllerProperties;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import tools.descartes.coffee.controller.monitoring.database.models.NetworkTime;
import tools.descartes.coffee.controller.monitoring.database.networking.NetworkingService;

@Component
public class NetworkingReporter {
        private final Logger logger = Logger.getLogger(this.getClass().getName());

        private final NetworkingService networkingService;
        private final ControllerProperties controllerProperties;
        private final CsvExporter csvExporter;
        private final SummaryExporter summaryExporter;

        public NetworkingReporter(NetworkingService networkingService, ControllerProperties controllerProperties,
                                  CsvExporter csvExporter, SummaryExporter summaryExporter) {
                this.networkingService = networkingService;
                this.controllerProperties = controllerProperties;
                this.csvExporter = csvExporter;
                this.summaryExporter = summaryExporter;
        }

        public void report() {
                List<NetworkTime> networkingTimings = this.networkingService.findAll();

                if (networkingTimings.isEmpty()) {
                        this.logger.info("No networking timings found.");
                        return;
                }

                if (controllerProperties.isExportResults()) {
                        if (!csvExporter.exportDataToCsv("IN_CLUSTER_NETWORK", networkingTimings)) {
                                this.logger.info("\n");
                                this.logger.warning(
                                                "Error occurred while exporting the in-cluster networking data to csv.");
                                this.logger.info("\n");
                        }
                }

                long[] startToRequestArrival = this.getStartToRequestArrivalTimings(networkingTimings);
                long[] requestResponse = this.getRequestResponseTimings(networkingTimings);
                long[] overallTimings = this.getOverallTimings(networkingTimings);

                double startToRequestArrivalAvgMs = ReporterUtils.mean(startToRequestArrival);
                double requestResponseAvgMs = ReporterUtils.mean(requestResponse);
                double overallTimingsAvgMs = ReporterUtils.mean(overallTimings);

                this.logger.info("\n\n");
                this.logger.info("######################## NETWORKING REPORT SUMMARY ########################");
                this.logger.info("\n\n");

                this.logger.info("Reporting networking timings:");
                this.logger.info("Items              : " + networkingTimings.size());

                this.logger.info("Average time between sending the request and arrival at the target container   : ");
                this.logger.info(startToRequestArrivalAvgMs + " ms or "
                                + (startToRequestArrivalAvgMs / 1000) + " seconds");

                this.logger.info("Average time between target request arrival and response arrival at the source : ");
                this.logger.info(requestResponseAvgMs + " ms or "
                                + (requestResponseAvgMs / 1000) + " seconds");

                this.logger.info("Average time between sending the request and response arrival at the source    : ");
                this.logger.info(overallTimingsAvgMs + " ms or "
                                + (overallTimingsAvgMs / 1000) + " seconds");

                this.logger.info("\n");

                if (controllerProperties.isExportResults()) {
                        summaryExporter.writeToSummary(
                                        "######################## NETWORKING REPORT SUMMARY ########################");

                        summaryExporter.writeToSummary("");
                        summaryExporter.writeToSummary("Reporting networking timings:");
                        summaryExporter.writeToSummary("Items              : " + networkingTimings.size());

                        summaryExporter.writeToSummary(
                                        "Average time between sending the request and arrival at the target container   : "
                                                        + startToRequestArrivalAvgMs + " ms or "
                                                        + (startToRequestArrivalAvgMs / 1000) + " seconds");

                        summaryExporter.writeToSummary(
                                        "Average time between target request arrival and response arrival at the source : "
                                                        + requestResponseAvgMs + " ms or "
                                                        + (requestResponseAvgMs / 1000) + " seconds");

                        summaryExporter.writeToSummary(
                                        "Average time between sending the request and response arrival at the source    : "
                                                        + overallTimingsAvgMs + " ms or "
                                                        + (overallTimingsAvgMs / 1000) + " seconds");
                        summaryExporter.writeToSummary("\n");
                }

                // pair of route (source, target) and networking timings for all routes
                List<Pair<Pair<String, String>, List<NetworkTime>>> routeNetworkingTimings = this
                                .mapRouteToNetworkingTimes(networkingTimings);

                this.logger.info("\n");
                this.logger.info("--------------- ROUTE DETAILS ---------------");
                this.logger.info("\n");
                this.logger.info(
                                "\tSOURCE\t\t|\tTARGET\t|\tSTART - REQ_ARRIVAL\t| REQ_ARRIVAL - RESPONSE\t|\tOVERALL");

                if (controllerProperties.isExportResults()) {
                        summaryExporter.writeToSummary("\n");
                        summaryExporter.writeToSummary("--------------- ROUTE DETAILS ---------------");
                        summaryExporter.writeToSummary("\n");
                        summaryExporter.writeToSummary(
                                        "\tSOURCE\t\t|\tTARGET\t|\tSTART - REQ_ARRIVAL\t| REQ_ARRIVAL - RESPONSE\t|\tOVERALL");
                }

                for (Pair<Pair<String, String>, List<NetworkTime>> routeTiming : routeNetworkingTimings) {

                        long[] routeStartToRequestArrival = this
                                        .getStartToRequestArrivalTimings(routeTiming.getSecond());
                        long[] routeRequestResponse = this.getRequestResponseTimings(routeTiming.getSecond());
                        long[] routeOverallTimings = this.getOverallTimings(routeTiming.getSecond());

                        double routeStartToRequestArrivalAvgMs = ReporterUtils.mean(routeStartToRequestArrival);
                        double routeRequestResponseAvgMs = ReporterUtils.mean(routeRequestResponse);
                        double routeOverallTimingsAvgMs = ReporterUtils.mean(routeOverallTimings);

                        String tableRow = routeTiming.getFirst().getFirst() + "\t| " +
                                        routeTiming.getFirst().getSecond() + "\t| " +
                                        routeStartToRequestArrivalAvgMs + "\t| " +
                                        routeRequestResponseAvgMs + "\t| " +
                                        routeOverallTimingsAvgMs;

                        this.logger.info(tableRow);

                        if (controllerProperties.isExportResults()) {
                                summaryExporter.writeToSummary(tableRow);
                        }

                }
        }

        /**
         * 
         * @param networkingTimings
         * @return pair of route (source, target) and networking timings as a list for
         *         all routes
         */
        private List<Pair<Pair<String, String>, List<NetworkTime>>> mapRouteToNetworkingTimes(
                        List<NetworkTime> networkingTimings) {

                Map<Pair<String, String>, List<NetworkTime>> routeNetworkingTimesMap = new HashMap<>();

                // add all routes to map
                networkingTimings.stream()
                                .filter(time -> time.getNetworkingNo() == 1)
                                .forEach(time -> routeNetworkingTimesMap.putIfAbsent(
                                                Pair.of(time.getSource(), time.getTarget()),
                                                new ArrayList<>()));

                for (NetworkTime networkTime : networkingTimings) {
                        routeNetworkingTimesMap
                                        .get(Pair.of(networkTime.getSource(), networkTime.getTarget()))
                                        .add(networkTime);
                }

                // map of entries to list of pairs
                return routeNetworkingTimesMap.entrySet().stream()
                                .map(entry -> Pair.of(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        }

        private long[] getStartToRequestArrivalTimings(List<NetworkTime> timings) {
                return timings.stream()
                                .mapToLong(time -> time.getRequestArrival().getTime()
                                                - time.getStartNetworking().getTime())
                                .toArray();
        }

        private long[] getRequestResponseTimings(List<NetworkTime> timings) {
                return timings.stream()
                                .mapToLong(time -> time.getResponseArrival().getTime()
                                                - time.getRequestArrival().getTime())
                                .toArray();
        }

        private long[] getOverallTimings(List<NetworkTime> timings) {
                return timings.stream()
                                .mapToLong(time -> time.getResponseArrival().getTime()
                                                - time.getStartNetworking().getTime())
                                .toArray();
        }
}
