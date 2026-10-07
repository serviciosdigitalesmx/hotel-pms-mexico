package com.hotelpms.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/** Explicit mail sender configuration for JVM and Native Image runtimes. */
@Configuration
public class MailConfig {
  @Bean
  JavaMailSender javaMailSender(
      @Value("${spring.mail.host:mailpit}") String host,
      @Value("${spring.mail.port:1025}") int port) {
    JavaMailSenderImpl sender = new JavaMailSenderImpl();
    sender.setHost(host);
    sender.setPort(port);
    return sender;
  }
}
