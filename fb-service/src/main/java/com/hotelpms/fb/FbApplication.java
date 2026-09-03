package com.hotelpms.fb;

import com.hotelpms.fb.config.FbNativeRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main class for the Fb Service application.
 */
@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
@ImportRuntimeHints(FbNativeRuntimeHints.class)
public class FbApplication {
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
        SpringApplication.run(FbApplication.class, args);
    }
}
