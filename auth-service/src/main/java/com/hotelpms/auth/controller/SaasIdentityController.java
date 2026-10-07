package com.hotelpms.auth.controller;

import com.hotelpms.auth.dto.*;
import com.hotelpms.auth.service.JwtService;
import com.hotelpms.auth.service.SaasIdentityService;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/auth") @RequiredArgsConstructor
public class SaasIdentityController {
  private final SaasIdentityService service; private final JwtService jwt;
  @PostMapping("/register") public ResponseEntity<Map<String,String>> signup(@Valid @RequestBody SignupRequest r){service.signup(r);return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("status","VERIFICATION_REQUIRED"));}
  @PostMapping("/public/verify-email") @ResponseStatus(HttpStatus.NO_CONTENT) public void verify(@RequestParam String token){service.verify(token);}
  @PostMapping("/public/resend-verification") @ResponseStatus(HttpStatus.ACCEPTED) public void resend(@Valid @RequestBody ForgotPasswordRequest r){service.resendVerification(r.email());}
  @PostMapping("/public/forgot-password") @ResponseStatus(HttpStatus.ACCEPTED) public void forgot(@Valid @RequestBody ForgotPasswordRequest r){service.forgot(r.email());}
  @PostMapping("/public/reset-password") @ResponseStatus(HttpStatus.NO_CONTENT) public void reset(@Valid @RequestBody PublicResetPasswordRequest r){service.reset(r);}
  @PostMapping("/public/invitations/accept") @ResponseStatus(HttpStatus.NO_CONTENT) public void accept(@Valid @RequestBody AcceptInvitationRequest r){service.accept(r);}
  @PostMapping("/invitations") @ResponseStatus(HttpStatus.ACCEPTED) public void invite(@RequestHeader("X-Auth-Hotel") UUID tenant, Authentication auth,@Valid @RequestBody InvitationRequest r){service.invite(tenant,auth.getName(),r);}
  @PostMapping("/onboarding/complete") @ResponseStatus(HttpStatus.NO_CONTENT) public void onboarding(@CookieValue("jwt") String token,@Valid @RequestBody OnboardingRequest r){service.completeOnboarding(jwt.extractHotelId(token),r);}
}
