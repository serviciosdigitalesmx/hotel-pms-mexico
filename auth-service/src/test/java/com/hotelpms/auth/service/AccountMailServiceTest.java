package com.hotelpms.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class AccountMailServiceTest {

  private static final String FRONTEND = "https://fixi-monolith-web.vercel.app";

  private final JavaMailSender sender = org.mockito.Mockito.mock(JavaMailSender.class);

  private AccountMailService mail;

  @BeforeEach
  void setUp() {
    mail = new AccountMailService(sender, new FixiFrontendUrls(FRONTEND), "no-reply@fixi.example");
  }

  @Test
  void verificationEmailUsesCanonicalFrontendAndPreservesTokenEncoding() {
    mail.verification("owner@example.test", "synthetic+verify/token");

    assertMailLink("/verify-email?token=synthetic%2Bverify%2Ftoken");
  }

  @Test
  void passwordResetEmailUsesCanonicalFrontend() {
    mail.reset("owner@example.test", "synthetic-reset-token");

    assertMailLink("/reset-password?token=synthetic-reset-token");
  }

  @Test
  void invitationEmailUsesCanonicalFrontend() {
    mail.invitation("employee@example.test", "synthetic-invitation-token");

    assertMailLink("/accept-invitation?token=synthetic-invitation-token");
  }

  private void assertMailLink(final String pathAndQuery) {
    final ArgumentCaptor<SimpleMailMessage> message = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(sender).send(message.capture());
    assertThat(message.getValue().getText())
        .contains(FRONTEND + pathAndQuery)
        .doesNotContain("admin.serviciosdigitalesmx.online", "app.serviciosdigitalesmx.online");
  }
}
