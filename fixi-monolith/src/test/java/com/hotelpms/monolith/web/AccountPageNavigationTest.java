package com.hotelpms.monolith.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.hotelpms.auth.service.FixiFrontendUrls;
import org.junit.jupiter.api.Test;

class AccountPageNavigationTest {

  private static final String FRONTEND = "https://fixi-monolith-web.vercel.app";
  private final FixiFrontendUrls frontendUrls = new FixiFrontendUrls(FRONTEND);

  @Test
  void verificationSuccessLinksToCurrentFrontendLogin() {
    final String html = new EmailVerificationPageController(frontendUrls).verifyEmailPage().getBody();

    assertThat(html)
        .contains("href=\"" + FRONTEND + "/login\"")
        .doesNotContain("admin.serviciosdigitalesmx.online", "app.serviciosdigitalesmx.online");
  }

  @Test
  void passwordResetLinksToCurrentFrontendLoginAndRecovery() {
    final String html = new PasswordResetPageController(frontendUrls).resetPasswordPage().getBody();

    assertThat(html)
        .contains("href=\"" + FRONTEND + "/login\"")
        .contains("href=\"" + FRONTEND + "/forgot-password\"")
        .doesNotContain("admin.serviciosdigitalesmx.online", "app.serviciosdigitalesmx.online");
  }

  @Test
  void acceptedInvitationLinksToCurrentFrontendLogin() {
    final String html =
        new InvitationAcceptancePageController(frontendUrls).acceptInvitationPage().getBody();

    assertThat(html)
        .contains("href=\"" + FRONTEND + "/login\"")
        .doesNotContain("admin.serviciosdigitalesmx.online", "app.serviciosdigitalesmx.online");
  }
}
