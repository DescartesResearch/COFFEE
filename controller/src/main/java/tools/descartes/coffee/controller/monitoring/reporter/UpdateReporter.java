package tools.descartes.coffee.controller.monitoring.reporter;

import java.util.List;
import java.util.logging.Logger;

import tools.descartes.coffee.controller.config.ControllerProperties;
import org.springframework.stereotype.Component;

import tools.descartes.coffee.controller.monitoring.database.deployment.DeploymentService;
import tools.descartes.coffee.controller.monitoring.database.models.UpdateRestartTime;

@Component
public class UpdateReporter {
        private final Logger logger = Logger.getLogger(this.getClass().getName());

        private final DeploymentService deploymentService;
        private final ControllerProperties controllerProperties;
        private final CsvExporter csvExporter;
        private final SummaryExporter summaryExporter;

        public UpdateReporter(DeploymentService deploymentService, ControllerProperties controllerProperties,
                              CsvExporter csvExporter, SummaryExporter summaryExporter) {
                this.deploymentService = deploymentService;
                this.controllerProperties = controllerProperties;
                this.csvExporter = csvExporter;
                this.summaryExporter = summaryExporter;
        }

        public void report() {
                List<UpdateRestartTime> updateTimings = this.deploymentService.findAll();

                if (updateTimings.isEmpty()) {
                        this.logger.info("No update timings found.");
                        return;
                }

                if (controllerProperties.isExportResults()) {
                        if (!csvExporter.exportDataToCsv("UPDATE_DEPLOYMENT", updateTimings)) {
                                this.logger.info("\n");
                                this.logger.warning(
                                                "Error occurred while exporting the deployment update data to csv.");
                                this.logger.info("\n");
                        }
                }

                long[] shutDownStartUpDifference = this.getShutDownStartUpDifference(updateTimings);
                double shutDownStartUpDifferenceAvgMs = ReporterUtils.mean(shutDownStartUpDifference);

                this.logger.info("\n\n");
                this.logger.info("######################## UPDATE REPORT SUMMARY ########################");
                this.logger.info("\n\n");

                this.logger.info("Reporting update timings:");
                this.logger.info("Items              : " + updateTimings.size());

                this.logger.info("Average time between shut down old and start up new container : "
                                + shutDownStartUpDifferenceAvgMs + " ms or "
                                + (shutDownStartUpDifferenceAvgMs / 1000) + " seconds");

                this.logger.info("\n");

                if (controllerProperties.isExportResults()) {
                        summaryExporter.writeToSummary(
                                        "######################## UPDATE REPORT SUMMARY ########################");

                        summaryExporter.writeToSummary("");
                        summaryExporter.writeToSummary("Reporting update timings:");
                        summaryExporter.writeToSummary("Items              : " + updateTimings.size());

                        summaryExporter.writeToSummary(
                                        "Average time between shut down old and start up new container : "
                                                        + shutDownStartUpDifferenceAvgMs + " ms or "
                                                        + (shutDownStartUpDifferenceAvgMs / 1000) + " seconds");
                        summaryExporter.writeToSummary("\n");
                }

        }

        private long[] getShutDownStartUpDifference(List<UpdateRestartTime> timings) {
                return timings.stream()
                                .mapToLong(time -> time.getRestartTime().getTime()
                                                - time.getShutDownTime().getTime())
                                .toArray();
        }
}
