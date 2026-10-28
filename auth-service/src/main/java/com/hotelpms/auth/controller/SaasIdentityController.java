package com.hotelpms.auth.controller;

import com.hotelpms.auth.dto.AcceptInvitationRequest;
import com.hotelpms.auth.dto.ForgotPasswordRequest;
import com.hotelpms.auth.dto.InvitationRequest;
import com.hotelpms.auth.dto.OnboardingRequest;
import com.hotelpms.auth.dto.PublicResetPasswordRequest;
import com.hotelpms.auth.dto.SignupRequest;
import com.hotelpms.auth.service.AccountVerificationResult;
import com.hotelpms.auth.service.JwtService;
import com.hotelpms.auth.service.SaasIdentityService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** HTTP endpoints for self-service Fixi tenant identity workflows. */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class SaasIdentityController {
  private final SaasIdentityService service;
  private final JwtService jwt;

  /**
   * Registers a tenant owner and starts the account verification flow.
   *
   * @param request validated tenant and owner details
   * @return accepted response indicating that verification is required
   */
  @PostMapping("/register")
  public ResponseEntity<Map<String, String>> signup(
      @Valid @RequestBody final SignupRequest request) {
    service.signup(request);
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(Map.of("status", "VERIFICATION_REQUIRED"));
  }

  /**
   * Verifies an account using its one-time token.
   *
   * @param token account verification token
   */
  @PostMapping("/public/verify-email")
  public ResponseEntity<Map<String, String>> verify(@RequestParam final String token) {
    final AccountVerificationResult result = service.verify(token);
    if (result == AccountVerificationResult.ALREADY_VERIFIED) {
      return ResponseEntity.ok(Map.of("status", "ALREADY_VERIFIED"));
    }
    return ResponseEntity.noContent().build();
  }

  /**
   * Resends verification to an eligible account without disclosing account existence.
   *
   * @param request validated email address
   */
  @PostMapping("/public/resend-verification")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void resend(@Valid @RequestBody final ForgotPasswordRequest request) {
    service.resendVerification(request.email());
  }

  /**
   * Starts a password reset flow without disclosing account existence.
   *
   * @param request validated account email
   */
  @PostMapping("/public/forgot-password")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void forgot(@Valid @RequestBody final ForgotPasswordRequest request) {
    service.forgot(request.email());
  }

  /**
   * Resets a password using a valid single-use token.
   *
   * @param request validated reset token and replacement password
   */
  @PostMapping("/public/reset-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void reset(@Valid @RequestBody final PublicResetPasswordRequest request) {
    service.reset(request);
  }

  /**
   * Accepts an invitation and creates the invited tenant user.
   *
   * @param request invitation token and new account details
   */
  @PostMapping("/public/invitations/accept")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void accept(@Valid @RequestBody final AcceptInvitationRequest request) {
    service.accept(request);
  }

  /**
   * Creates an invitation for the authenticated actor's tenant.
   *
   * @param tenant tenant identifier from the trusted gateway header
   * @param auth authenticated actor
   * @param request invitation role, email, and optional branch
   */
  @PostMapping("/invitations")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void invite(
      @RequestHeader("X-Auth-Hotel") final UUID tenant,
      final Authentication auth,
      @Valid @RequestBody final InvitationRequest request) {
    service.invite(tenant, auth.getName(), request);
  }

  /**
   * Completes tenant onboarding for the authenticated session.
   *
   * @param token session JWT cookie
   * @param request validated tenant onboarding details
   */
  @PostMapping("/onboarding/complete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void onboarding(
      @CookieValue("jwt") final String token, @Valid @RequestBody final OnboardingRequest request) {
    service.completeOnboarding(jwt.extractHotelId(token), request);
  }
}
