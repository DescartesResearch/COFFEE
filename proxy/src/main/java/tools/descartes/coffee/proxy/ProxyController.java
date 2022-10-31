package tools.descartes.coffee.proxy;

import java.net.http.HttpResponse;
import java.util.logging.Logger;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tools.descartes.coffee.shared.HttpUtils;

@RestController
@RequestMapping("/proxy")
public class ProxyController {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private static final int PROXY_REQUEST_TIMEOUT_SECONDS = 60;

    @GetMapping("/request")
    public String proxyGetRequest(@RequestBody String target) {
        target = this.removeDoubleQuotes(target);

        logger.info("proxy GET to target: " + target);
        HttpResponse<String> response = HttpUtils.get(target, PROXY_REQUEST_TIMEOUT_SECONDS);
        logger.info("GET response: " + response);

        return response.body();
    }

    @PostMapping("/request")
    public String proxyPostRequest(@RequestBody String target) {
        target = this.removeDoubleQuotes(target);

        logger.info("proxy POST to target: " + target);
        HttpResponse<String> response = HttpUtils.post(target, PROXY_REQUEST_TIMEOUT_SECONDS);
        logger.info("POST response: " + response);

        return response.body();
    }

    /**
     * removes double quotes from the beginning and end of the target uri
     * 
     * @param target
     * @return
     */
    private String removeDoubleQuotes(String target) {
        return target.replaceAll("^\"|\"$", "");
    }
}
