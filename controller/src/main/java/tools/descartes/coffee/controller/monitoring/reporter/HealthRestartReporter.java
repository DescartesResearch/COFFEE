package tools.descartes.coffee.controller.monitoring.reporter;

import java.util.List;
import java.util.logging.Logger;

import tools.descartes.coffee.controller.config.ControllerProperties;
import org.springframework.stereotype.Component;

import tools.descartes.coffee.controller.monitoring.database.GenericDatabaseService;
import tools.descartes.coffee.controller.monitoring.database.restart.health.HealthService;
import tools.descartes.coffee.controller.monitoring.database.models.HealthRestartTime;

@Component
public class HealthRestartReporter {
        private final Logger logger = Logger.getLogger(this.getClass().getName());

        private final GenericDatabaseService<HealthRestartTime> healthService;
        private final ControllerProperties controllerProperties;
        private final CsvExporter csvExporter;
        private final SummaryExporter summaryExporter;

        public HealthRestartReporter(HealthService healthService, ControllerProperties controllerProperties,
                                     CsvExporter csvExporter, SummaryExporter summaryExporter) {
                this.healthService = healthService;
                this.controllerProperties = controllerProperties;
                this.csvExporter = csvExporter;
                this.summaryExporter = summaryExporter;
        }

        public void report() {
                List<HealthRestartTime> healthRestartTimings = this.healthService.findAll();

                if (healthRestartTimings.isEmpty()) {
                        this.logger.info("No health restart timings found.");
                        return;
                }

                if (controllerProperties.isExportResults()) {
                        if (!csvExporter.exportDataToCsv("HEALTH_RESTART", healthRestartTimings)) {
                                this.logger.info("\n");
                                this.logger.warning(
                                                "Error occurred while exporting the health restart data to csv.");
                                this.logger.info("\n");
                        }
                }

                long[] startToHealthCheck = this.getStartToHealthCheckTimings(healthRestartTimings);
                long[] unhealthyToCheck = this.getUnhealthyToCheckTimings(healthRestartTimings);
                long[] unhealthyToRestart = this.getUnhealthyToRestartTimings(healthRestartTimings);
                long[] checkToRestart = this.getCheckToRestartTimings(healthRestartTimings);
                long[] overallTimings = this.getOverallTimings(healthRestartTimings);

                double startToHealthCheckAvgMs = ReporterUtils.mean(startToHealthCheck);
                double unhealthyToCheckAvgMs = ReporterUtils.mean(unhealthyToCheck);
                double unhealthyToRestartAvgMs = ReporterUtils.mean(unhealthyToRestart);
                double checkToRestartAvgMs = ReporterUtils.mean(checkToRestart);
                double overallTimingsAvgMs = ReporterUtils.mean(overallTimings);

                this.logger.info("\n\n");
                this.logger.info("######################## HEALTH RESTART REPORT SUMMARY ########################");
                this.logger.info("\n\n");

                this.logger.info("Reporting health restart timings:");
                this.logger.info("Items              : " + healthRestartTimings.size());

                this.logger.info(
                                "Average time to health check                  : " + startToHealthCheckAvgMs + " ms or "
                                                + (startToHealthCheckAvgMs / 1000) + " seconds");
                this.logger.info("Average time between unhealthy and check      : " + unhealthyToCheckAvgMs + " ms or "
                                + (unhealthyToCheckAvgMs / 1000) + " seconds");
                this.logger.info(
                                "Average time between unhealthy and restart    : " + unhealthyToRestartAvgMs + " ms or "
                                                + (unhealthyToRestartAvgMs / 1000) + " seconds");
                this.logger.info("Average time between check and restart        : " + checkToRestartAvgMs + " ms or "
                                + (checkToRestartAvgMs / 1000) + " seconds");
                this.logger.info("Average overall time                          : " + overallTimingsAvgMs + " ms or "
                                + (overallTimingsAvgMs / 1000) + " seconds");

                this.logger.info("\n");

                if (controllerProperties.isExportResults()) {
                        summaryExporter.writeToSummary(
                                        "######################## HEALTH RESTART REPORT SUMMARY ########################");

                        summaryExporter.writeToSummary("");
                        summaryExporter.writeToSummary("Reporting health restart timings:");
                        summaryExporter.writeToSummary("Items              : " + healthRestartTimings.size());

                        summaryExporter.writeToSummary(
                                        "Average time to health check                  : " + startToHealthCheckAvgMs
                                                        + " ms or " + (startToHealthCheckAvgMs / 1000) + " seconds");
                        summaryExporter.writeToSummary("Average time between unhealthy and check      : "
                                        + unhealthyToCheckAvgMs + " ms or "
                                        + (unhealthyToCheckAvgMs / 1000) + " seconds");
                        summaryExporter.writeToSummary(
                                        "Average time between unhealthy and restart    : " + unhealthyToRestartAvgMs
                                                        + " ms or " + (unhealthyToRestartAvgMs / 1000) + " seconds");
                        summaryExporter.writeToSummary("Average time between check and restart        : "
                                        + checkToRestartAvgMs + " ms or "
                                        + (checkToRestartAvgMs / 1000) + " seconds");
                        summaryExporter.writeToSummary("Average overall time                          : "
                                        + overallTimingsAvgMs + " ms or "
                                        + (overallTimingsAvgMs / 1000) + " seconds");
                        summaryExporter.writeToSummary("\n");
                }

        }

        private long[] getStartToHealthCheckTimings(List<HealthRestartTime> timings) {
                return timings.stream()
                                .mapToLong(time -> time.getHealthCheckTime().getTime()
                                                - time.getCommandTime().getTime())
                                .toArray();
        }

        private long[] getUnhealthyToCheckTimings(List<HealthRestartTime> timings) {
                return timings.stream()
                                .mapToLong(time -> time.getHealthCheckTime().getTime()
                                                - time.getUnhealthyTime().getTime())
                                .toArray();
        }

        private long[] getUnhealthyToRestartTimings(List<HealthRestartTime> timings) {
                return timings.stream()
                                .mapToLong(time -> time.getExecutionFinished().getTime()
                                                - time.getUnhealthyTime().getTime())
                                .toArray();
        }

        private long[] getCheckToRestartTimings(List<HealthRestartTime> timings) {
                return timings.stream()
                                .mapToLong(time -> time.getExecutionFinished().getTime()
                                                - time.getHealthCheckTime().getTime())
                                .toArray();
        }

        private long[] getOverallTimings(List<HealthRestartTime> timings) {
                return timings.stream()
                                .mapToLong(time -> time.getExecutionFinished().getTime()
                                                - time.getCommandTime().getTime())
                                .toArray();
        }
}
