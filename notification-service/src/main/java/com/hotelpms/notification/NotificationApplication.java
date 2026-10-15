package com.hotelpms.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.hotelpms.notification.config.NotificationRuntimeHints;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * Entry point for the Notification Service.
 */
@SpringBootApplication
@EnableScheduling
@ImportRuntimeHints(NotificationRuntimeHints.class)
public class NotificationApplication {

    /**
     * Dummy instance method to prevent PMD/Checkstyle treating this as a utility class.
     */
    public void init() {
        // not a utility class
    }

    /**
     * Main method.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(NotificationApplication.class, args);
    }
}
