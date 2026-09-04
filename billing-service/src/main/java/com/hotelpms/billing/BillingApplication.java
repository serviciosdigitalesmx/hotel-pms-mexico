package com.hotelpms.billing;

import com.hotelpms.billing.config.BillingNativeRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main class for the Billing Service application.
 */
@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
@ImportRuntimeHints(BillingNativeRuntimeHints.class)
public class BillingApplication {
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
        SpringApplication.run(BillingApplication.class, args);
    }
}
