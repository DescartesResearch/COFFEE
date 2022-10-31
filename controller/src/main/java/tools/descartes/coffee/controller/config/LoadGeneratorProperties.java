package tools.descartes.coffee.controller.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("loadgenerator")
public class LoadGeneratorProperties {
    private String jarFile;
    private String userProfile;
    private int durationSeconds;
    private String loggingFile;
    private String requestLoggingFile;
    private String intensityFile;
    private int requestsPerSec;

    public String getJarFile() {
        return jarFile;
    }

    public void setJarFile(String jarFile) {
        this.jarFile = jarFile;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getLoggingFile() {
        return loggingFile;
    }

    public void setLoggingFile(String loggingFile) {
        this.loggingFile = loggingFile;
    }

    public String getRequestLoggingFile() {
        return requestLoggingFile;
    }

    public void setRequestLoggingFile(String requestLoggingFile) {
        this.requestLoggingFile = requestLoggingFile;
    }

    public String getIntensityFile() {
        return intensityFile;
    }

    public void setIntensityFile(String intensityFile) {
        this.intensityFile = intensityFile;
    }

    public int getRequestsPerSec() {
        return requestsPerSec;
    }

    public void setRequestsPerSec(int requestsPerSec) {
        this.requestsPerSec = requestsPerSec;
    }
}
