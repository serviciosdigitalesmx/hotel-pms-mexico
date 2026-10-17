package com.hotelpms.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/** Explicit mail sender configuration for JVM and Native Image runtimes. */
@Configuration(proxyBeanMethods = false)
public final class MailConfig {
  @Bean
  JavaMailSender javaMailSender(
      @Value("${spring.mail.host:mailpit}") final String host,
      @Value("${spring.mail.port:1025}") final int port) {
    final JavaMailSenderImpl sender = new JavaMailSenderImpl();
    sender.setHost(host);
    sender.setPort(port);
    return sender;
  }
}
