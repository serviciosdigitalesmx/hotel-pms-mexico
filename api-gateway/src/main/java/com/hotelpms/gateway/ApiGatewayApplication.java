package com.hotelpms.gateway;

import com.hotelpms.gateway.config.GatewayNativeRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * Main class for the ApiGateway Service application.
 */
@SpringBootApplication
@ImportRuntimeHints(GatewayNativeRuntimeHints.class)
public class ApiGatewayApplication {
    /**
     * Dummy instance method to prevent PMD and Checkstyle from treating this as a utility class.
     */
    public void init() {
        // Not a utility class
    }

    /**
     * Main method.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
