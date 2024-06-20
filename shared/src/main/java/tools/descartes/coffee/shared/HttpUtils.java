package tools.descartes.coffee.shared;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class HttpUtils {
    private static final Logger logger = Logger.getLogger("HttpUtils");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final int DEFAULT_REQUEST_TIMEOUT_SECONDS = 30;

    /**
     * followRedirects: also follow https to http to avoid errors by unrecognized
     * request lost (header parser received no bytes)
     * 
     * ALWAYS: Always redirect.
     * NORMAL: Always redirect, except from HTTPS URLs to HTTP URLs.
     */
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(Redirect.ALWAYS)
            .build();

    /**
     * 
     * @param uri target
     * @return HttpResponse | null
     */
    public static HttpResponse<String> get(String uri) {
        HttpRequest request = buildGetRequest(uri);

        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception e) {
            logger.warning("Error while sending http get request to " + uri + " : " + e.getMessage());
            return null;
        }
    }

    public static HttpResponse<String> get(String uri, int timeoutSeconds) {
        HttpRequest request = buildGetRequest(uri, timeoutSeconds);

        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception e) {
            logger.warning("Error while sending http get request to " + uri + " : " + e.getMessage());
            return null;
        }
    }

    /**
     * 
     * @param uri target
     * @return HttpResponse | null
     */
    public static HttpResponse<String> get(String uri, Object body) {
        String json = mapToString(body);
        HttpRequest request = buildGetRequest(uri, json);

        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception e) {
            logger.warning("Error while sending http get request to " + uri + " : " + e.getMessage());
            return null;
        }
    }

    public static HttpResponse<String> get(String uri, Object body, int timeoutSeconds) {
        String json = mapToString(body);
        HttpRequest request = buildGetRequest(uri, json, timeoutSeconds);

        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception e) {
            logger.warning("Error while sending http get request to " + uri + " : " + e.getMessage());
            return null;
        }
    }

    /**
     * 
     * @param uri target
     * @return HttpResponse | null
     */
    public static CompletableFuture<HttpResponse<String>> getAsync(String uri) {
        HttpRequest request = buildGetRequest(uri);

        try {
            return client.sendAsync(request, BodyHandlers.ofString());
        } catch (Exception e) {
            logger.warning("Error while sending http get request to " + uri + " : " + e.getMessage());
            return null;
        }
    }

    public static CompletableFuture<HttpResponse<String>> getAsync(String uri, Object body) {
        String json = mapToString(body);
        HttpRequest request = buildGetRequest(uri, json);

        try {
            return client.sendAsync(request, BodyHandlers.ofString());
        } catch (Exception e) {
            logger.warning("Error while sending http get request to " + uri + " : " + e.getMessage());
            return null;
        }
    }

    /**
     * POST request with empty body
     * 
     * @param uri
     * @return
     */
    public static HttpResponse<String> post(String uri) {
        HttpRequest request = buildPostRequest(uri);

        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception e) {
            logger.warning("Error while sending http post request to " + uri + " : " + e.getMessage());
            return null;
        }
    }

    public static HttpResponse<String> post(String uri, int timeoutSeconds) {
        HttpRequest request = buildPostRequest(uri, timeoutSeconds);

        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception e) {
            logger.warning("Error while sending http post request to " + uri + " : " + e.getMessage());
            return null;
        }
    }

    public static HttpResponse<String> post(String uri, Object body) {
        String json = mapToString(body);
        HttpRequest request = buildPostRequest(uri, json);

        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception e) {
            logger.warning("Error while sending http post request to " + uri + " : " + e.getMessage());
            return null;
        }
    }

    /**
     * POST request with empty body
     * 
     * @param uri
     * @return
     */
    public static CompletableFuture<HttpResponse<String>> postAsync(String uri) {
        HttpRequest request = buildPostRequest(uri);

        try {
            return client.sendAsync(request, BodyHandlers.ofString());
        } catch (Exception e) {
            logger.warning("Error while sending http post request to " + uri + " : " + e.getMessage());
            return null;
        }

    }

    public static CompletableFuture<HttpResponse<String>> postAsync(String uri, Object body) {
        String json = mapToString(body);
        HttpRequest request = buildPostRequest(uri, json);

        try {
            return client.sendAsync(request, BodyHandlers.ofString());
        } catch (Exception e) {
            logger.warning("Error while sending http post request to " + uri + " : " + e.getMessage());
            return null;
        }
    }

    private static HttpRequest buildPostRequest(String uri) {
        return buildPostRequest(uri, "");
    }

    private static HttpRequest buildPostRequest(String uri, String bodyJson) {
        return HttpRequest.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .uri(URI.create(uri))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(bodyJson))
                .timeout(Duration.ofSeconds(DEFAULT_REQUEST_TIMEOUT_SECONDS))
                .build();
    }

    private static HttpRequest buildPostRequest(String uri, int timeoutSeconds) {
        if (timeoutSeconds > 0) {
            return HttpRequest.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create(uri))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(""))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .build();
        } else {
            return HttpRequest.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create(uri))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(""))
                    .build();
        }
    }

    private static HttpRequest buildGetRequest(String uri) {
        return HttpRequest.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .uri(URI.create(uri))
                .header("Content-Type", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(DEFAULT_REQUEST_TIMEOUT_SECONDS))
                .build();
    }

    private static HttpRequest buildGetRequest(String uri, String bodyJson) {
        return HttpRequest.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .uri(URI.create(uri))
                .header("Content-Type", "application/json")
                .method("GET", BodyPublishers.ofString(bodyJson))
                .timeout(Duration.ofSeconds(DEFAULT_REQUEST_TIMEOUT_SECONDS))
                .build();
    }

    private static HttpRequest buildGetRequest(String uri, int timeoutSeconds) {
        if (timeoutSeconds > 0) {
            return HttpRequest.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create(uri))
                    .header("Content-Type", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .build();
        } else {
            return HttpRequest.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create(uri))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
        }
    }

    private static HttpRequest buildGetRequest(String uri, String bodyJson, int timeoutSeconds) {
        if (timeoutSeconds > 0) {
            return HttpRequest.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create(uri))
                    .header("Content-Type", "application/json")
                    .method("GET", BodyPublishers.ofString(bodyJson))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .build();
        } else {
            return HttpRequest.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create(uri))
                    .header("Content-Type", "application/json")
                    .method("GET", BodyPublishers.ofString(bodyJson))
                    .build();
        }
    }

    private static String mapToString(Object objectToMap) {
        try {
            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(objectToMap);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Error while mapping object " + objectToMap.toString() + " : " + e.getMessage(), e);
        }
    }

    private HttpUtils() {

    }
}
