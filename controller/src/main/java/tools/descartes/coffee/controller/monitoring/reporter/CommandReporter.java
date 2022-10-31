package tools.descartes.coffee.controller.monitoring.reporter;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import tools.descartes.coffee.controller.config.ControllerProperties;
import org.springframework.stereotype.Component;

import tools.descartes.coffee.controller.monitoring.database.command.CommandExecutionService;
import tools.descartes.coffee.controller.monitoring.database.models.CommandExecutionTime;
import tools.descartes.coffee.controller.procedure.collection.Command;

@Component
public class CommandReporter {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final CommandExecutionService commandExecutionService;
    private final ControllerProperties controllerProperties;
    private final CsvExporter csvExporter;
    private final SummaryExporter summaryExporter;

    public CommandReporter(CommandExecutionService commandExecutionService, ControllerProperties controllerProperties,
                           CsvExporter csvExporter, SummaryExporter summaryExporter) {
        this.commandExecutionService = commandExecutionService;
        this.controllerProperties = controllerProperties;
        this.csvExporter = csvExporter;
        this.summaryExporter = summaryExporter;
    }

    public void report() {
        List<CommandExecutionTime> commandTimings = this.commandExecutionService.findAll();

        if (commandTimings.isEmpty()) {
            this.logger.info("No command timings found.");
            return;
        }

        if (controllerProperties.isExportResults()) {
            if (this.isLoaded(commandTimings) && !csvExporter.copyLoadCsvOutputToMaterials()) {
                this.logger.info("\n");
                this.logger.warning("Error occurred while copying load.log data to materials.");
                this.logger.info("\n");
            }

            summaryExporter.writeToSummary("######################## COMMAND REPORT SUMMARY ########################");
        }

        this.logger.info("\n\n");
        this.logger.info("######################## COMMAND REPORT SUMMARY ########################");
        this.logger.info("\n\n");

        reportCommand(Command.START, "container start", commandTimings);
        reportCommand(Command.REMOVE, "container remove", commandTimings);

        reportCommand(Command.RESTART, "container restart", commandTimings);
        reportCommand(Command.HEALTH, "container health restart", commandTimings);
        reportCommand(Command.CRASH, "container crash restart", commandTimings);

        reportCommand(Command.UPDATE, "deployment rolling update", commandTimings);
        reportCommand(Command.NETWORK, "container networking", commandTimings);

        this.logger.info("\n");
    }

    private boolean isLoaded(List<CommandExecutionTime> commandTimings) {
        return commandTimings.stream()
                .anyMatch(commandTime -> commandTime.getCommand().equals(Command.LOAD.toString()));
    }

    private void reportCommand(Command command, String description, List<CommandExecutionTime> commandTimings) {

        List<CommandExecutionTime> specificCommandTimings = commandTimings.stream()
                .filter(commandTime -> commandTime.getCommand().equals(command.toString()))
                .collect(Collectors.toList());

        if (!specificCommandTimings.isEmpty()) {
            this.reportTimings(description, specificCommandTimings);

            if (controllerProperties.isExportResults()) {
                if (!csvExporter.exportDataToCsv(command.toString() + "_COMMAND", specificCommandTimings)) {
                    this.logger.info("\n");
                    this.logger.warning(
                            "Error occurred while exporting the " + command + " command data to csv.");
                    this.logger.info("\n");
                }
            }
        }

    }

    private void reportTimings(String description, List<CommandExecutionTime> timings) {

        long[] values = timings.stream()
                .mapToLong(startUp -> startUp.getExecutionFinished().getTime() - startUp.getCommandTime().getTime())
                .toArray();

        double avgMs = ReporterUtils.mean(values);
        double varMs = ReporterUtils.var(values, avgMs);
        double stdDevMs = ReporterUtils.stdDev(varMs);

        this.logger.info("\n");
        this.logger.info("Reporting " + description + " timings:");
        this.logger.info("Items              : " + timings.size());
        this.logger.info("Average time       : " + avgMs + " ms or " + (avgMs / 1000) + " seconds");
        this.logger.info("Variance           : " + varMs + " ms or " + (varMs / 1000) + " seconds");
        this.logger.info("Standard deviation : " + stdDevMs + " ms or " + (stdDevMs / 1000) + " seconds");
        this.logger.info("\n");

        if (controllerProperties.isExportResults()) {
            summaryExporter.writeToSummary("");
            summaryExporter.writeToSummary("Reporting " + description + " timings:");
            summaryExporter.writeToSummary("Items              : " + timings.size());
            summaryExporter.writeToSummary("Average time       : " + avgMs + " ms or " + (avgMs / 1000) + " seconds");
            summaryExporter.writeToSummary("Variance           : " + varMs + " ms or " + (varMs / 1000) + " seconds");
            summaryExporter
                    .writeToSummary("Standard deviation : " + stdDevMs + " ms or " + (stdDevMs / 1000) + " seconds");
            summaryExporter.writeToSummary("\n");
        }
    }

}
