package tools.descartes.coffee.controller.utils;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SpringPropertyHelper {

    private static Environment environment;

    public static String getProperty(String key) {
        return environment.getProperty(key);
    }

    public SpringPropertyHelper(Environment environment) {
        SpringPropertyHelper.environment = environment;
    }
}
