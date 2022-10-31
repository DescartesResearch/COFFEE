package tools.descartes.coffee.controller.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import tools.descartes.coffee.controller.config.LoadGeneratorProperties;
import tools.descartes.coffee.controller.utils.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import tools.descartes.coffee.controller.orchestrator.OrchestratorMap;
import org.springframework.stereotype.Component;

/**
 * Class to provide access to the HTTP load generator.
 * 
 * @see <a href="https://github.com/joakimkistowski/HTTP-Load-Generator#2-getting-started-with-the-load-generator">...</a>
 */

@Component
public class LoadGenerator {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /** prefixes to execute cmd commands */
    private final String[] cmdCommand;
    private String GENERATOR_IP;
    private boolean setup;
    private Process generatorProcess;
    private Process directorProcess;

    // private Thread generatorLogger;
    private Thread directorLogger;

    private final LoadGeneratorProperties loadGeneratorProperties;
    private final OrchestratorMap orchestratorMap;

    public LoadGenerator(OrchestratorMap orchestratorMap, LoadGeneratorProperties loadGeneratorProperties) {
        setup = false;
        this.orchestratorMap = orchestratorMap;
        this.loadGeneratorProperties = loadGeneratorProperties;
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");

        if (isWindows) {
            cmdCommand = new String[] { "cmd.exe", "/c" };
        } else {
            cmdCommand = new String[] { "sh", "-c" };
        }
    }

    // executes java -jar httploadgenerator.jar loadgenerator
    public void setup() {
        this.setTargetAddress();
        this.setGeneratorIp();

        File generatorJar = new File(loadGeneratorProperties.getJarFile());
        ProcessBuilder builder = new ProcessBuilder(ArrayUtils.addAll(cmdCommand,
                "java -jar " + generatorJar.getAbsolutePath() + " loadgenerator"));
        // builder.redirectErrorStream(true);

        try {
            this.generatorProcess = builder.start();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error from load generator (generator mode) occurred: " + e.getMessage(), e);
        }

        this.setupGeneratorLogger();
        setup = true;
    }

    public void startLoadProfile() {
        if (!setup) {
            setup();
        }
        String intensityFile = loadGeneratorProperties.getIntensityFile();
        String command;
        // if intensityFile not set, use requestsPerSec
        if (intensityFile == null || intensityFile.equals("NULL")) {
            command = this.createLoadProfileCommand(loadGeneratorProperties.getRequestsPerSec());
        } else {
            command = this.createLoadProfileCommand();
        }
        this.startLoadProcess(command);
    }

    private void startLoadProcess(String command) {
        ProcessBuilder builder = new ProcessBuilder(ArrayUtils.addAll(cmdCommand, command));
        // builder.redirectErrorStream(true);

        try {
            this.logger.info("starting load generator: " + command);
            this.directorProcess = builder.start();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error from load generator (generator mode) occurred: " + e.getMessage(), e);
        }

        this.setupDirectorLogger();
    }

    public void stopLoadProfile() {
        shutDown();
    }

    public void shutDown() {
        // this.shutDownLogger(this.generatorLogger);
        this.shutDownLogger(this.directorLogger);

        if (this.directorProcess != null) {
            this.shutDownProcess(this.directorProcess.toHandle());
        }
        if (this.generatorProcess != null) {
            this.shutDownProcess(this.generatorProcess.toHandle());
        }
        setup = false;
    }

    private void shutDownLogger(Thread logger) {
        if (logger != null && logger.isAlive() && !logger.isInterrupted()) {
            logger.interrupt();
        }
    }

    private void shutDownProcess(ProcessHandle process) {
        if (process != null && process.isAlive()) {
            process.descendants().forEach(this::shutDownProcess);
            process.destroy();
        }
    }

    private void setGeneratorIp() {
        try {
            GENERATOR_IP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new IllegalStateException(
                    "Cannot read the local host address for the load generator: " + e.getMessage(), e);
        }
    }

    /**
     * modifies the service-user.profile.yaml
     * services > hosts entry
     */
    private void setTargetAddress() {
        String targetAddress;
        try {
            targetAddress = orchestratorMap.getContainerAccessor().getLoadBalancerAddress();
        } catch (Exception e) {
            throw new IllegalStateException("Could not specify target address for load generator.", e);
        }

        File loadProfile = new File(loadGeneratorProperties.getUserProfile());

        if (!loadProfile.exists()) {
            throw new IllegalStateException(
                    "Could not find profile yaml file of load generator: " + loadProfile.getAbsolutePath());
        }

        ObjectMapper objectMapper = new YAMLMapper();
        Map<String, Object> profile;

        try {
            profile = objectMapper.readValue(loadProfile,
                    new TypeReference<>() {
                    });
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not load profile yaml file of load generator.", e);
        }

        List<Map<String, Object>> services = (List<Map<String, Object>>) profile.get("services");
        List<String> hosts = (List<String>) services.get(0).get("hosts");
        hosts.clear();
        hosts.add(targetAddress);

        try {
            objectMapper.writeValue(loadProfile, profile);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not write target address to profile yaml file of load generator.", e);
        }
    }

    private String createLoadProfileCommand(int requestsPerSec) {
        int DEFAULT_PROFILE_DURATION_SECONDS = loadGeneratorProperties.getDurationSeconds();
        String intensitySpec = " --arrival-profile-duration " + DEFAULT_PROFILE_DURATION_SECONDS
                + " --arrival-profile-start-rate " + requestsPerSec
                + " --arrival-profile-end-rate " + requestsPerSec;

        return this.constructCommand(intensitySpec);
    }

    private String createLoadProfileCommand() {
        String loadProfilePath = loadGeneratorProperties.getIntensityFile();
        return this.constructCommand(" --load " + loadProfilePath);
    }

    /**
     * $ java -jar httploadgenerator.jar director \
     * --ip localhost \ # IP of the generator
     * -t 256 \ # No. of threads of the generator
     * --yaml user_profile.yaml \ # User behavior description
     * --load sinus.csv \ # Load intensity pattern
     * -o out.csv \ # Location of the output file
     * --ro rout.csv \ # Detailed output file
     * --wr 10 \ # Warmup rate (req/s)
     * --wp 10 \ # Warmup pause (s)
     * --wd 120 \ # Warmup duration (s)
     * -u 1200 \ # Number of simulated users
     * --timeout 8000 # HTTP read timeout
     */
    private String constructCommand(String intensitySpec) {

        File generatorDirectorJar = new File(loadGeneratorProperties.getJarFile());
        String originalLoadLog = loadGeneratorProperties.getLoggingFile();
        String loadLog = originalLoadLog;
        int counter = 0;
        while (FileUtils.fileExists(loadLog)) {
            counter++;
            if (counter == 1) {
                loadLog = counter + loadLog;
            } else {
                loadLog = loadLog.replaceAll(".*" + originalLoadLog, counter + originalLoadLog);
            }
        }
        String loadRequestLog = loadGeneratorProperties.getRequestLoggingFile();
        if (counter != 0) {
            loadRequestLog = counter + loadRequestLog;
        }
        String userRequestProfile = loadGeneratorProperties.getUserProfile();

        String command = "java -jar " + generatorDirectorJar.getAbsolutePath() + " director";
        command += " --ip " + GENERATOR_IP;
        command += " -o " + loadLog;
        command += " --ro " + loadRequestLog;
        command += " --yaml " + userRequestProfile;

        command += " --timeout " + 10000;
        command += " --threads " + 200;
        command += " --users " + 500;

        command += intensitySpec;

        // Verbose output
        // command += " -v ";

        // Warmup
        // command += " --wr " + 20;
        // command += " --wp " + 10;
        // command += " --wd " + 20;

        return command;
    }

    private void setupGeneratorLogger() {
        // this.generatorLogger = this.setupLogger("generator", this.generatorProcess);
        // this.generatorLogger.start();
    }

    private void setupDirectorLogger() {
        this.directorLogger = this.setupLogger("director", this.directorProcess);
        this.directorLogger.start();
    }

    /**
     * Sets up a thread for logging the generator output.
     * Currently only the error log is printed.
     * 
     * @param mode      generator | director
     * @param generator process
     * @return Thread
     */
    private Thread setupLogger(String mode, Process generator) {
        return new Thread(() -> {
            Logger logger = Logger.getLogger("load generator (" + mode + " mode)");
            BufferedReader inputReader = new BufferedReader(
                    new InputStreamReader(generator.getInputStream())); // for full stream: getInputStream

            String line = null;
            while (true) {
                try {
                    line = inputReader.readLine();
                } catch (Exception e) {
                    if (!generator.isAlive()) {
                        break;
                    }

                    logger.warning(
                            "Failed to read output of the load generator (" + mode + " mode): " + e.getMessage());
                }

                if (line == null) {
                    break;
                }

                logger.info(line);
            }
        });
    }
}
