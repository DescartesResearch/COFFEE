package tools.descartes.coffee.controller;

import tools.descartes.coffee.controller.config.ControllerProperties;
import tools.descartes.coffee.controller.load.LoadGenerator;
import tools.descartes.coffee.controller.monitoring.controller.ContainerController;
import tools.descartes.coffee.controller.monitoring.reporter.*;
import tools.descartes.coffee.controller.orchestrator.BaseClusterClient;
import tools.descartes.coffee.controller.orchestrator.ClusterClientWrapper;
import tools.descartes.coffee.controller.procedure.BaseProcedure;
import tools.descartes.coffee.controller.procedure.parsing.ScriptBuilder;
import tools.descartes.coffee.controller.procedure.parsing.ScriptParsingException;
import org.springframework.stereotype.Component;
import tools.descartes.coffee.controller.monitoring.reporter.*;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class TestExecutor implements Runnable {
    private static final Logger logger = Logger.getLogger(TestExecutor.class.getName());

    private final ControllerProperties controllerProperties;
    private final LoadGenerator loadGenerator;
    private final CommandReporter commandReporter;
    private final CrashRestartReporter crashRestartReporter;
    private final HealthRestartReporter healthRestartReporter;
    private final UpdateReporter updateReporter;
    private final NetworkingReporter networkingReporter;
    private final CsvExporter csvExporter;
    private final SummaryExporter summaryExporter;
    private final ClusterClientWrapper clusterClientWrapper;
    private final ScriptBuilder scriptBuilder;

    private BaseClusterClient orchestratorClient;
    private BaseProcedure[] procedures;

    public TestExecutor(ControllerProperties controllerProperties,
                        LoadGenerator loadGenerator, CommandReporter commandReporter,
                        CrashRestartReporter crashRestartReporter, HealthRestartReporter healthRestartReporter,
                        UpdateReporter updateReporter, NetworkingReporter networkingReporter,
                        CsvExporter csvExporter, SummaryExporter summaryExporter, ClusterClientWrapper clusterClientWrapper,
                        ScriptBuilder scriptBuilder) {
        this.controllerProperties = controllerProperties;
        this.loadGenerator = loadGenerator;
        this.commandReporter = commandReporter;
        this.crashRestartReporter = crashRestartReporter;
        this.healthRestartReporter = healthRestartReporter;
        this.updateReporter = updateReporter;
        this.networkingReporter = networkingReporter;
        this.csvExporter = csvExporter;
        this.summaryExporter = summaryExporter;
        this.clusterClientWrapper = clusterClientWrapper;
        this.scriptBuilder = scriptBuilder;
    }

    @Override
    public void run() {
        startUp();

        try {
            execute();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred while executing the test procedure: " + e.getMessage(), e);
            logger.warning("######## Quitting the procedure ########");
            logger.info("Further commands will not be executed.");
            logger.info("Shutting down controller.");
        }

        shutDown();
        System.exit(0);
    }

    private void startUp() {
        orchestratorClient = clusterClientWrapper.getClient();

        try {
            logger.info("Starting parsing script...");
            procedures = scriptBuilder.loadScript(controllerProperties.getTestScript());
            logger.info("Finished script parsing");
        } catch (ScriptParsingException ioe) {
            logger.log(Level.SEVERE, "ScriptParsingException during load of procedure", ioe);
            throw new IllegalStateException("ScriptParsingException during load of procedure", ioe);
        }
        orchestratorClient.connect();

        // TODO: remove clear
        orchestratorClient.clear();
        orchestratorClient.init();

        /*
         * since the load balancer uses information from the client initialization the
         * generator.setup() hast to be called after client.init()
         *
         * According to experience, settings may not yet be fully adopted, so an
         * additional delay is set here
         */
        if (Arrays.stream(procedures).anyMatch(BaseProcedure::needsLoadGenerator)) {
            delay(20);
            loadGenerator.setup();
        }
    }

    private void execute() {
        // delay(10);
        Thread[] procedureThreads = new Thread[procedures.length];
        ContainerController.isProcedureActive = true;
        // starting the procedures
        logger.info("starting the procedures");
        for (int i = 0; i < procedures.length; i++) {
            procedureThreads[i] = new Thread(procedures[i]);
            procedureThreads[i].start();
        }
        // waiting for all procedures to finish
        logger.info("waiting for all procedures to finish");
        for (int i = 0; i < procedures.length; i++) {
            try {
                procedureThreads[i].join();
            } catch (InterruptedException ie) {
                logger.log(Level.SEVERE, "Error during waiting for procedure " + i + " to finish", ie);
            }
        }
        ContainerController.isProcedureActive = false;
        delay(20);

        if (controllerProperties.isExportResults()) {
            if (!csvExporter.prepareCsvExportFolder()) {
                logger.log(Level.SEVERE, "Error occurred creating csv export folder!");
                logger.log(Level.INFO, "Test results will not be exported.");
                controllerProperties.setExportResults(false);
            }

            if (!summaryExporter.setupSummaryExport()) {
                logger.log(Level.SEVERE, "Error occurred setting up the summary text export.");
                logger.log(Level.INFO, "The summary will not be exported as text file.");
            }
        }

        // report db results
        commandReporter.report();
        crashRestartReporter.report();
        healthRestartReporter.report();
        updateReporter.report();
        networkingReporter.report();

        if (controllerProperties.isExportResults()) {
            summaryExporter.cleanUpSummaryExport();
        }
    }

    private void shutDown() {
        orchestratorClient.clear();
        orchestratorClient.disconnect();
        loadGenerator.shutDown();
    }

    private void delay(long sec) {
        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
