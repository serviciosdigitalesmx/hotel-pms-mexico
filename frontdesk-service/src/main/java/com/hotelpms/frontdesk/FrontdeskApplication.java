package com.hotelpms.frontdesk;

import com.hotelpms.frontdesk.config.FrontdeskNativeRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main class for the Frontdesk Service application.
 *
 * <p>Hosts the front-desk, Fixi repair-workshop, inventory, reservations, stays and settings
 * bounded contexts in one deployable.
 */
@SpringBootApplication
@EnableScheduling
@EnableFeignClients
@EnableJpaAuditing
@EnableCaching
@ImportRuntimeHints(FrontdeskNativeRuntimeHints.class)
public class FrontdeskApplication {

  /** Dummy instance method to prevent PMD and Checkstyle from treating this as a utility class. */
  public void init() {
    // Not a utility class
  }

  /**
   * Main method.
   *
   * @param args command line arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(FrontdeskApplication.class, args);
  }
}
