package tools.descartes.coffee.application;

import java.math.BigInteger;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tools.descartes.coffee.shared.HttpUtils;
import tools.descartes.coffee.shared.NetworkingData;

@RestController
public class AppController {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Value("${app.controllerAddress}")
    private String appControllerAddress;

    @Value("${app.controllerPort}")
    private int appControllerPort;

    private boolean isHealthy = true;

    /* avoid multiple health status entries from same container */
    private boolean healthStatusSent = false;

    private final LoadCounter loadCounter;

    public AppController(LoadCounter loadCounter) {
        this.loadCounter = loadCounter;
    }

    @GetMapping("/")
    public String accept() {
        return "Application accessible!";
    }

    @GetMapping("/health/check")
    public ResponseEntity<Boolean> getHealth() {
        if (this.isHealthy) {
            logger.info("successful health check. ");
            return ResponseEntity.ok(true);
        } else {
            if (!healthStatusSent) {
                AppUtils.sendCurrentTimestamp(appControllerAddress, appControllerPort, "health", "check");
                healthStatusSent = true;
            }

            logger.info("health check failed. restart required");
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/unhealthy")
    public String setUnhealthy() {
        AppUtils.sendCurrentTimestamp(appControllerAddress, appControllerPort, "health", "unhealthy");
        this.isHealthy = false;

        return "Set healthy flag to false.";
    }

    @GetMapping("/unhealthy")
    public String setUnhealthyViaGet() {
        AppUtils.sendCurrentTimestamp(appControllerAddress, appControllerPort, "health", "unhealthy");
        this.isHealthy = false;

        return "Set healthy flag to false.";
    }

    @GetMapping("/crash")
    public String crashContainer() {
        AppUtils.sendCurrentTimestamp(appControllerAddress, appControllerPort, "app", "crash");
        System.exit(1);

        // not reachable
        return "App crashed.";
    }

    /**
     * disables proxy before sending in-cluster request and reenables the proxy
     * afterwards
     */
    @GetMapping("/network")
    public NetworkingData network(@RequestParam(value = "source") String source,
            @RequestParam(value = "target") String target) {
        long startNetworking = System.currentTimeMillis();

        String targetPath = "http://" + target + "/in-cluster-network";
        logger.info("start networking");
        logger.info("sending get request to " + targetPath);

        // TODO: Set read timeout and error handling for timeout (e.g. there is no connection between sender and receiver)
        HttpResponse<String> response = HttpUtils.get(targetPath);
        long endNetworking = System.currentTimeMillis();

        logger.info("in-cluster response:" + response);
        long networkRequestReceived = Long.parseLong(response.body());

        return new NetworkingData(source, target, startNetworking, networkRequestReceived,
                endNetworking);
    }

    /**
     * @return the current timestamp as long
     */
    @GetMapping("/in-cluster-network")
    public long network() {
        long time = System.currentTimeMillis();
        logger.info("received in-cluster network request; returning current time to requesting container");
        return time;
    }

    @GetMapping("/load")
    public String load() {
        loadCounter.addLoad();
        long start = System.currentTimeMillis();
        logger.info("Generating Load ... ");

        checkPrime(250);
        long endPrime = System.currentTimeMillis();
        logger.info("Calculated Prime Numbers in " + (endPrime - start) + " ms.");

        calculateFactorial(250);
        long endFactorial = System.currentTimeMillis();
        logger.info("Calculated Factorial in " + (endFactorial - start) + " ms.");

        long end = System.currentTimeMillis();
        logger.info("Generated Load for " + (end - start) + " ms.");

        return "load generated";
    }

    /**
     * searches for prime numbers from 0 to max
     */
    private void checkPrime(int max) {
        ArrayList<BigInteger> primeNumbers = new ArrayList<>();
        int current = 0;

        while (true) {
            if (current > max) {
                break;
            }
            if (current > 1) {
                BigInteger currentBigInteger = new BigInteger(current + "");
                if (currentBigInteger.isProbablePrime(current / 2)) {
                    primeNumbers.add(currentBigInteger);
                }
            }
            current++;
        }
    }

    public void calculateFactorial(int n) {
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) result = result.multiply(BigInteger.valueOf(i));
    }
}
