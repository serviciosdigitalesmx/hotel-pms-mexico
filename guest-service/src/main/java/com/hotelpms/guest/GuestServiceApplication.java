package com.hotelpms.guest;

import com.hotelpms.guest.config.GuestNativeRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main class for the GuestService Service application.
 */
@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
@EnableScheduling
@ImportRuntimeHints(GuestNativeRuntimeHints.class)
public class GuestServiceApplication {
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
        SpringApplication.run(GuestServiceApplication.class, args);
    }
}
