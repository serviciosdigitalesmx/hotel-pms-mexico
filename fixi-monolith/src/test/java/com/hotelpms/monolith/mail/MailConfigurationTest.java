package com.hotelpms.monolith.mail;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@ExtendWith(OutputCaptureExtension.class)
class MailConfigurationTest {

  private final ApplicationContextRunner mailContext =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(MailSenderAutoConfiguration.class))
          .withInitializer(
              context -> {
                try {
                  final List<PropertySource<?>> properties =
                      new YamlPropertySourceLoader()
                          .load("fixi-monolith", new ClassPathResource("fixi-monolith.yml"));
                  final ConfigurableEnvironment environment =
                      (ConfigurableEnvironment) context.getEnvironment();
                  properties.forEach(environment.getPropertySources()::addLast);
                } catch (final IOException exception) {
                  throw new IllegalStateException(
                      "Could not load monolith mail configuration", exception);
                }
              });

  @Test
  void standardSpringMailAutoConfigurationAppliesSmtpSettingsWithoutLoggingPassword(
      final CapturedOutput output) {
    final String syntheticPassword = "smtp-test-secret-not-for-logs";

    mailContext
        .withPropertyValues(
            "SMTP_HOST=smtp.example.test",
            "SMTP_PORT=2525",
            "SMTP_USERNAME=fixi-test-user",
            "SMTP_PASSWORD=" + syntheticPassword,
            "SMTP_AUTH=true",
            "SMTP_STARTTLS=true",
            "SMTP_CONNECTION_TIMEOUT=1100",
            "SMTP_TIMEOUT=2200",
            "SMTP_WRITE_TIMEOUT=3300",
            "FIXI_MAIL_FROM=accounts@fixi.example.test")
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              final JavaMailSenderImpl sender = context.getBean(JavaMailSenderImpl.class);
              assertThat(sender.getHost()).isEqualTo("smtp.example.test");
              assertThat(sender.getPort()).isEqualTo(2525);
              assertThat(sender.getUsername()).isEqualTo("fixi-test-user");
              assertThat(sender.getPassword()).isEqualTo(syntheticPassword);
              assertThat(context.getEnvironment().getProperty("notification.from-address"))
                  .isEqualTo("accounts@fixi.example.test");
              assertThat(context.getEnvironment().getProperty("fixi.mail-from"))
                  .isEqualTo("accounts@fixi.example.test");
              assertThat(sender.getJavaMailProperties().getProperty("mail.smtp.auth"))
                  .isEqualTo("true");
              assertThat(
                      sender
                          .getJavaMailProperties()
                          .getProperty("mail.smtp.starttls.enable"))
                  .isEqualTo("true");
              assertThat(
                      sender
                          .getJavaMailProperties()
                          .getProperty("mail.smtp.connectiontimeout"))
                  .isEqualTo("1100");
              assertThat(sender.getJavaMailProperties().getProperty("mail.smtp.timeout"))
                  .isEqualTo("2200");
              assertThat(
                      sender
                          .getJavaMailProperties()
                          .getProperty("mail.smtp.writetimeout"))
                  .isEqualTo("3300");
            });

    assertThat(output.getAll()).doesNotContain(syntheticPassword);
  }

  @Test
  void explicitlyDisablesAuthenticationAndStartTlsWhenRequested() {
    mailContext
        .withPropertyValues(
            "SMTP_HOST=localhost",
            "SMTP_PORT=1025",
            "SMTP_USERNAME=",
            "SMTP_PASSWORD=",
            "SMTP_AUTH=false",
            "SMTP_STARTTLS=false")
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              final JavaMailSenderImpl sender = context.getBean(JavaMailSenderImpl.class);
              assertThat(sender.getUsername()).isEmpty();
              assertThat(sender.getPassword()).isEmpty();
              assertThat(sender.getJavaMailProperties().getProperty("mail.smtp.auth"))
                  .isEqualTo("false");
              assertThat(
                      sender
                          .getJavaMailProperties()
                          .getProperty("mail.smtp.starttls.enable"))
                  .isEqualTo("false");
            });
  }

  @Test
  void senderContextStartsWithoutCredentialsOrARealSmtpConnection() {
    mailContext
        .withPropertyValues(
            "SMTP_HOST=localhost",
            "SMTP_PORT=1025",
            "SMTP_AUTH=false",
            "SMTP_STARTTLS=false")
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).hasSingleBean(JavaMailSenderImpl.class);
            });
  }

  @Test
  void productionComposePassesOnlyEnvironmentReferencesForMailSettings() throws Exception {
    final Path composePath =
        Path.of(System.getProperty("fixi.monolith.projectDir"), "deploy", "compose.yml");
    final String compose = Files.readString(composePath);
    final int serviceStart = compose.indexOf("  fixi-monolith:\n");
    final int serviceEnd = compose.indexOf("\n  caddy:\n", serviceStart);
    assertThat(serviceStart).isGreaterThanOrEqualTo(0);
    assertThat(serviceEnd).isGreaterThan(serviceStart);
    final String monolithService = compose.substring(serviceStart, serviceEnd);

    for (final String variable :
        new String[] {
          "SMTP_HOST",
          "SMTP_PORT",
          "SMTP_USERNAME",
          "SMTP_PASSWORD",
          "SMTP_AUTH",
          "SMTP_STARTTLS",
          "SMTP_CONNECTION_TIMEOUT",
          "SMTP_TIMEOUT",
          "SMTP_WRITE_TIMEOUT",
          "FIXI_MAIL_FROM",
          "NOTIFICATION_FROM_ADDRESS",
          "NOTIFICATION_FROM_NAME"
        }) {
      assertThat(monolithService).contains(variable + ": ${" + variable);
    }
    assertThat(monolithService).contains("SMTP_PASSWORD: ${SMTP_PASSWORD:-}");
  }

  @Test
  void notificationAddressCanExplicitlyOverrideAccountMailAddress() {
    mailContext
        .withPropertyValues(
            "FIXI_MAIL_FROM=accounts@fixi.example.test",
            "NOTIFICATION_FROM_ADDRESS=notifications@fixi.example.test")
        .run(
            context -> {
              assertThat(context.getEnvironment().getProperty("fixi.mail-from"))
                  .isEqualTo("accounts@fixi.example.test");
              assertThat(context.getEnvironment().getProperty("notification.from-address"))
                  .isEqualTo("notifications@fixi.example.test");
            });
  }
}
