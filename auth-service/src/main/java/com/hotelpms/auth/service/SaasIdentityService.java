package com.hotelpms.auth.service;

import com.hotelpms.auth.domain.*;
import com.hotelpms.auth.dto.*;
import com.hotelpms.auth.exception.DuplicateResourceException;
import com.hotelpms.auth.exception.NotFoundException;
import com.hotelpms.auth.repository.*;
import com.hotelpms.internalauth.contracts.Capability;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.LocalDateTime;
import java.util.*;
import java.text.Normalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class SaasIdentityService {
  private static final String VERIFY="VERIFY_EMAIL", RESET="RESET_PASSWORD";
  private final HotelRegistryRepository hotels; private final UserAccountRepository users;
  private final TenantBranchRepository branches; private final UserBranchMembershipRepository memberships;
  private final TenantCapabilityRepository capabilities; private final AccountTokenRepository tokens;
  private final TenantInvitationRepository invitations; private final PasswordEncoder passwords;
  private final TenantIndustryProfileRepository industryProfiles;
  private final AccountMailService mail;

  @Transactional
  public void signup(SignupRequest r) {
    String email=r.email().trim().toLowerCase(Locale.ROOT), username=email;
    String businessName=r.businessName().trim(), slug=uniqueSlug(businessName);
    if(users.existsByEmailIncludingInactive(email)) throw new DuplicateResourceException("EMAIL_ALREADY_EXISTS");
    var tenant=new HotelRegistry(); tenant.setId(UUID.randomUUID()); tenant.setName(businessName); tenant.setSlug(slug); tenant.setStatus("ONBOARDING"); tenant.setCreatedAt(LocalDateTime.now()); hotels.save(tenant);
    var owner=users.save(UserAccount.builder().username(username).email(email).fullName(r.ownerName().trim()).passwordHash(passwords.encode(r.password())).role(Role.OWNER).hotelId(tenant.getId()).active(true).emailVerified(true).mustChangePassword(false).build());
    var branch=branches.save(TenantBranch.builder().hotelId(tenant.getId()).name(r.branchName().trim()).active(true).build());
    memberships.save(UserBranchMembership.builder().userId(owner.getId()).hotelId(tenant.getId()).branchId(branch.getId()).build());
    var now=LocalDateTime.now(); capabilities.saveAll(Arrays.stream(Capability.values()).map(c->{var x=new TenantCapability();x.setTenantId(tenant.getId());x.setCapabilityKey(c.name());x.setEnabled(true);x.setUpdatedAt(now);return x;}).toList());
    industryProfiles.save(TenantIndustryProfile.builder().tenantId(tenant.getId()).industryKey("ELECTRONICS_REPAIR").enabledModulesJson("[\"customers\",\"devices\",\"serviceOrders\",\"inventory\",\"whatsapp\",\"ai\"]").dynamicFieldsJson("{}").formsLabelsJson("{\"customer\":\"Cliente\",\"device\":\"Equipo\"}").catalogsTemplatesJson("{}").updatedAt(now).build());
  }

  @Transactional
  public void verify(String raw) { var t=consume(raw,VERIFY); var user=users.findByIdIncludingInactive(t.getUserId()).orElseThrow(()->new NotFoundException("TOKEN_INVALID")); user.setEmailVerified(true);user.setActive(true);users.save(user);var tenant=hotels.findById(user.getHotelId()).orElseThrow();tenant.setStatus("ONBOARDING");hotels.save(tenant); }

  @Transactional
  public void forgot(String email) { users.findByEmailIgnoreCase(email.trim()).ifPresent(u->mail.reset(u.getEmail(),issue(u.getId(),RESET,1))); }

  @Transactional
  public void reset(PublicResetPasswordRequest r) { var t=consume(r.token(),RESET);var u=users.findByIdIncludingInactive(t.getUserId()).orElseThrow(()->new NotFoundException("TOKEN_INVALID"));u.setPasswordHash(passwords.encode(r.password()));u.setTokenVersion(u.getTokenVersion()+1);u.setFailedAttempts(0);u.setLockedUntil(null);users.save(u); }

  @Transactional
  public void completeOnboarding(UUID tenantId, OnboardingRequest r) { var h=hotels.findById(tenantId).orElseThrow(()->new NotFoundException("TENANT_NOT_FOUND"));h.setContactPhone(r.contactPhone());h.setBusinessHours(r.businessHours());h.setOnboardingCompleted(true);h.setStatus("ACTIVE");hotels.save(h); }

  @Transactional
  public void invite(UUID tenantId,String username,InvitationRequest r) { if(r.role()==Role.OWNER||r.role()==Role.ADMIN) throw new AccessDeniedException("PRIVILEGED_ROLE_NOT_INVITABLE");var actor=users.findByUsername(username).filter(u->tenantId.equals(u.getHotelId())).orElseThrow(()->new AccessDeniedException("TENANT_MISMATCH"));if(r.branchId()!=null)branches.findByIdAndHotelId(r.branchId(),tenantId).orElseThrow(()->new NotFoundException("BRANCH_NOT_FOUND"));String raw=random();invitations.save(TenantInvitation.builder().hotelId(tenantId).email(r.email().trim().toLowerCase(Locale.ROOT)).role(r.role()).branchId(r.branchId()).tokenHash(hash(raw)).expiresAt(LocalDateTime.now().plusDays(3)).createdBy(actor.getId()).createdAt(LocalDateTime.now()).build());mail.invitation(r.email(),raw); }

  @Transactional
  public void accept(AcceptInvitationRequest r) { var i=invitations.findByTokenHash(hash(r.token())).filter(x->x.getAcceptedAt()==null&&x.getExpiresAt().isAfter(LocalDateTime.now())).orElseThrow(()->new NotFoundException("INVITATION_INVALID"));if(users.existsByEmailIncludingInactive(i.getEmail())||users.existsByUsernameIncludingInactive(r.username()))throw new DuplicateResourceException("ACCOUNT_ALREADY_EXISTS");var u=users.save(UserAccount.builder().username(r.username().trim()).email(i.getEmail()).fullName(r.fullName().trim()).passwordHash(passwords.encode(r.password())).role(i.getRole()).hotelId(i.getHotelId()).active(true).emailVerified(true).mustChangePassword(false).build());if(i.getBranchId()!=null)memberships.save(UserBranchMembership.builder().userId(u.getId()).hotelId(i.getHotelId()).branchId(i.getBranchId()).build());i.setAcceptedAt(LocalDateTime.now());invitations.save(i); }

  @Transactional public void resendVerification(String email){ users.findByEmailIncludingInactive(email.trim()).filter(u->!u.isEmailVerified()).ifPresent(u->mail.verification(u.getEmail(),issue(u.getId(),VERIFY,24))); }
  private String issue(UUID user,String purpose,int hours){String raw=random();tokens.save(AccountToken.builder().userId(user).purpose(purpose).tokenHash(hash(raw)).expiresAt(LocalDateTime.now().plusHours(hours)).createdAt(LocalDateTime.now()).build());return raw;}
  private AccountToken consume(String raw,String purpose){var t=tokens.findByTokenHashAndPurpose(hash(raw),purpose).filter(x->x.getConsumedAt()==null&&x.getExpiresAt().isAfter(LocalDateTime.now())).orElseThrow(()->new NotFoundException("TOKEN_INVALID"));t.setConsumedAt(LocalDateTime.now());return tokens.save(t);}
  private static String random(){byte[] b=new byte[32];new SecureRandom().nextBytes(b);return Base64.getUrlEncoder().withoutPadding().encodeToString(b);}
  private static String hash(String raw){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
  private String uniqueSlug(String businessName){
    String base=Normalizer.normalize(businessName,Normalizer.Form.NFD).replaceAll("\\p{M}+","").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+","-").replaceAll("^-+|-+$","");
    if(base.isBlank()) base="negocio";
    base=base.substring(0,Math.min(80,base.length())).replaceAll("-+$","");
    String candidate=base; int suffix=2;
    while(hotels.existsBySlug(candidate)){String s="-"+suffix++;int max=80-s.length();candidate=base.substring(0,Math.min(base.length(),max)).replaceAll("-+$","")+s;}
    return candidate;
  }
}
