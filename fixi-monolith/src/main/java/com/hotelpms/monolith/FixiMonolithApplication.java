package com.hotelpms.monolith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;
import java.util.Map;

/** Single JVM entry point that reuses Fixi's bounded-context modules. */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = UserDetailsServiceAutoConfiguration.class)
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
@EnableJpaAuditing
@EnableScheduling
@EnableCaching
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class FixiMonolithApplication {

  public static void main(final String[] args) {
    final SpringApplication application = new SpringApplication(FixiMonolithApplication.class);
    application.setDefaultProperties(
        Map.of("spring.config.location", "classpath:/fixi-monolith.yml"));
    application.run(args);
  }
}
