package tools.descartes.coffee.controller.monitoring.reporter;

import java.util.List;
import java.util.logging.Logger;

import tools.descartes.coffee.controller.config.ControllerProperties;
import org.springframework.stereotype.Component;

import tools.descartes.coffee.controller.monitoring.database.GenericDatabaseService;
import tools.descartes.coffee.controller.monitoring.database.models.StorageTime;
import tools.descartes.coffee.controller.monitoring.database.storage.StorageService;

@Component
public class StorageReporter {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final GenericDatabaseService<StorageTime> storageService;
    private final ControllerProperties controllerProperties;
    private final CsvExporter csvExporter;
    private final SummaryExporter summaryExporter;

    public StorageReporter(StorageService storageService, ControllerProperties controllerProperties,
                              CsvExporter csvExporter, SummaryExporter summaryExporter) {
        this.storageService = storageService;
        this.controllerProperties = controllerProperties;
        this.csvExporter = csvExporter;
        this.summaryExporter = summaryExporter;
    }

    public void report() {
        List<StorageTime> storageTimings = this.storageService.findAll();

        if (storageTimings.isEmpty()) {
            this.logger.info("No storage timings found.");
            return;
        }

        if (controllerProperties.isExportResults()) {
            if (!csvExporter.exportDataToCsv("STORAGE", storageTimings)) {
                this.logger.info("\n");
                this.logger.warning(
                        "Error occurred while exporting the storage data to csv.");
                this.logger.info("\n");
            }
        }

        long[] writeTime = storageTimings.stream().mapToLong(x -> x.getWriteTimeMillis()).toArray();
        long[] writeBytes = storageTimings.stream().mapToLong(x -> x.getWrittenBytes()).toArray();
        long[] readTime = storageTimings.stream().mapToLong(x -> x.getReadTimeMillis()).toArray();
        long[] readBytes = storageTimings.stream().mapToLong(x -> x.getReadBytes()).toArray();

        this.logger.info("\n\n");
        this.logger.info("######################## STORAGE REPORT SUMMARY ########################");
        this.logger.info("\n\n");

        this.logger.info("Reporting storage timings:");
        this.logger.info("Items              : " + storageTimings.size());

        this.logger.info("Average written bytes:");
        this.logger.info(ReporterUtils.mean(writeBytes) + " +/-" + ReporterUtils.stdDev(writeBytes));

        this.logger.info("Average write time:");
        this.logger.info(ReporterUtils.mean(writeTime) + " +/-" + ReporterUtils.stdDev(writeTime));

        this.logger.info("Average read bytes:");
        this.logger.info(ReporterUtils.mean(readBytes) + " +/-" + ReporterUtils.stdDev(readBytes));

        this.logger.info("Average read time:");
        this.logger.info(ReporterUtils.mean(readTime) + " +/-" + ReporterUtils.stdDev(readTime));

        this.logger.info("\n");

        if (controllerProperties.isExportResults()) {
            summaryExporter.writeToSummary(
                    "######################## STORAGE REPORT SUMMARY ########################");

            summaryExporter.writeToSummary("");
            summaryExporter.writeToSummary("Reporting storage timings:");
            summaryExporter.writeToSummary("Items              : " + storageTimings.size());

            summaryExporter.writeToSummary("Average written bytes:");
            summaryExporter.writeToSummary(ReporterUtils.mean(writeBytes) + " +/-" + ReporterUtils.stdDev(writeBytes));

            summaryExporter.writeToSummary("Average write time:");
            summaryExporter.writeToSummary(ReporterUtils.mean(writeTime) + " +/-" + ReporterUtils.stdDev(writeTime));

            summaryExporter.writeToSummary("Average read bytes:");
            summaryExporter.writeToSummary(ReporterUtils.mean(readBytes) + " +/-" + ReporterUtils.stdDev(readBytes));

            summaryExporter.writeToSummary("Average read time:");
            summaryExporter.writeToSummary(ReporterUtils.mean(readTime) + " +/-" + ReporterUtils.stdDev(readTime));
            summaryExporter.writeToSummary("\n");
        }
    }
}
