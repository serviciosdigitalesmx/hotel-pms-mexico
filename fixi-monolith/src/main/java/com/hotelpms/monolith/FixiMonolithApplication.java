package com.hotelpms.monolith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import java.util.Map;

/** Single JVM entry point that reuses Fixi's bounded-context modules. */
@SpringBootConfiguration
@EnableAutoConfiguration
@ImportRuntimeHints(FixiMonolithNativeRuntimeHints.class)
@ComponentScan(
    basePackages = "com.hotelpms",
    excludeFilters = {
      @ComponentScan.Filter(
          type = FilterType.REGEX,
          pattern = {
            "com\\.hotelpms\\..*\\.[A-Za-z]+Application",
            "com\\.hotelpms\\..*\\.SecurityConfig",
            "com\\.hotelpms\\..*\\.FeignHeaderConfig",
            "com\\.hotelpms\\..*\\.PdfTemplateConfig",
            "com\\.hotelpms\\.internalauth\\.security\\.InternalAuthFilter"
          })
    })
@EntityScan("com.hotelpms")
@EnableJpaRepositories("com.hotelpms")
public class FixiMonolithApplication {

  public static void main(final String[] args) {
    final SpringApplication application = new SpringApplication(FixiMonolithApplication.class);
    application.setDefaultProperties(
        Map.of("spring.config.location", "classpath:/fixi-monolith.yml"));
    application.run(args);
  }
}
