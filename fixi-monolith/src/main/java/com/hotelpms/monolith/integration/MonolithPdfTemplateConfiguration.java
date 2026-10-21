package com.hotelpms.monolith.integration;

import com.hotelpms.pdftemplate.PdfTemplateRenderer;
import com.hotelpms.pdftemplate.ThymeleafPdfTemplateRenderer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Shared PDF renderer replacing the duplicate per-service configuration beans. */
@Configuration(proxyBeanMethods = false)
public class MonolithPdfTemplateConfiguration {

  @Bean
  PdfTemplateRenderer pdfTemplateRenderer() {
    return new ThymeleafPdfTemplateRenderer("templates/pdf/");
  }
}
