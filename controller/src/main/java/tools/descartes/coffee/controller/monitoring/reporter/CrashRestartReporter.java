package tools.descartes.coffee.controller.monitoring.reporter;

import java.util.List;
import java.util.logging.Logger;

import tools.descartes.coffee.controller.config.ControllerProperties;
import org.springframework.stereotype.Component;

import tools.descartes.coffee.controller.monitoring.database.GenericDatabaseService;
import tools.descartes.coffee.controller.monitoring.database.restart.app.AppService;
import tools.descartes.coffee.controller.monitoring.database.models.AppCrashRestartTime;

@Component
public class CrashRestartReporter {
        private final Logger logger = Logger.getLogger(this.getClass().getName());

        private final GenericDatabaseService<AppCrashRestartTime> crashService;
        private final ControllerProperties controllerProperties;
        private final CsvExporter csvExporter;
        private final SummaryExporter summaryExporter;

        public CrashRestartReporter(AppService crashService, ControllerProperties controllerProperties,
                                    CsvExporter csvExporter, SummaryExporter summaryExporter) {
                this.crashService = crashService;
                this.controllerProperties = controllerProperties;
                this.csvExporter = csvExporter;
                this.summaryExporter = summaryExporter;
        }

        public void report() {
                List<AppCrashRestartTime> crashRestartTimings = this.crashService.findAll();

                if (crashRestartTimings.isEmpty()) {
                        this.logger.info("No crash restart timings found.");
                        return;
                }

                if (controllerProperties.isExportResults()) {
                        if (!csvExporter.exportDataToCsv("CRASH_RESTART", crashRestartTimings)) {
                                this.logger.info("\n");
                                this.logger.warning(
                                                "Error occurred while exporting the crash restart data to csv.");
                                this.logger.info("\n");
                        }
                }

                long[] crashToRestart = this.getCrashToRestartTimings(crashRestartTimings);
                long[] overallTimings = this.getOverallTimings(crashRestartTimings);

                double crashToRestartAvgMs = ReporterUtils.mean(crashToRestart);
                double overallTimingsAvgMs = ReporterUtils.mean(overallTimings);

                this.logger.info("\n\n");
                this.logger.info("######################## CRASH RESTART REPORT SUMMARY ########################");
                this.logger.info("\n\n");

                this.logger.info("Reporting crash restart timings:");
                this.logger.info("Items              : " + crashRestartTimings.size());

                this.logger.info("Average time between crash and restart        : " + crashToRestartAvgMs + " ms or "
                                + (crashToRestartAvgMs / 1000) + " seconds");
                this.logger.info("Average overall time                          : " + overallTimingsAvgMs + " ms or "
                                + (overallTimingsAvgMs / 1000) + " seconds");

                this.logger.info("\n");

                if (controllerProperties.isExportResults()) {
                        summaryExporter.writeToSummary(
                                        "######################## CRASH RESTART REPORT SUMMARY ########################");

                        summaryExporter.writeToSummary("");
                        summaryExporter.writeToSummary("Reporting crash restart timings:");
                        summaryExporter.writeToSummary("Items              : " + crashRestartTimings.size());

                        summaryExporter.writeToSummary("Average time between crash and restart        : "
                                        + crashToRestartAvgMs + " ms or "
                                        + (crashToRestartAvgMs / 1000) + " seconds");
                        summaryExporter.writeToSummary("Average overall time                          : "
                                        + overallTimingsAvgMs + " ms or "
                                        + (overallTimingsAvgMs / 1000) + " seconds");
                        summaryExporter.writeToSummary("\n");
                }

        }

        private long[] getCrashToRestartTimings(List<AppCrashRestartTime> timings) {
                return timings.stream()
                                .mapToLong(time -> time.getExecutionFinished().getTime()
                                                - time.getAppCrashTime().getTime())
                                .toArray();
        }

        private long[] getOverallTimings(List<AppCrashRestartTime> timings) {
                return timings.stream()
                                .mapToLong(time -> time.getExecutionFinished().getTime()
                                                - time.getCommandTime().getTime())
                                .toArray();
        }
}
