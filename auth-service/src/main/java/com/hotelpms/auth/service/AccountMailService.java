package com.hotelpms.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Delivers account verification, password reset, and invitation messages. */
@Service
public class AccountMailService {
  private final JavaMailSender sender;
  private final FixiFrontendUrls frontendUrls;
  private final String from;

  /**
   * Creates the mail service with the configured sender and public URL.
   *
   * @param sender configured mail transport
   * @param frontendUrls canonical browser-accessible Fixi frontend URLs
   * @param from sender address used in account messages
   */
  public AccountMailService(
      final JavaMailSender sender,
      final FixiFrontendUrls frontendUrls,
      @Value("${fixi.mail-from:no-reply@fixi.local}") final String from) {
    this.sender = sender;
    this.frontendUrls = frontendUrls;
    this.from = from;
  }

  /**
   * Sends a one-time account verification link.
   *
   * @param email recipient address
   * @param token one-time verification token
   */
  public void verification(final String email, final String token) {
    send(email, "Verifica tu cuenta Fixi", frontendUrls.pageWithToken("/verify-email", token));
  }

  /**
   * Sends a password reset link.
   *
   * @param email recipient address
   * @param token one-time reset token
   */
  public void reset(final String email, final String token) {
    send(email, "Restablece tu contraseña Fixi", frontendUrls.pageWithToken("/reset-password", token));
  }

  /**
   * Sends a tenant invitation link.
   *
   * @param email recipient address
   * @param token one-time invitation token
   */
  public void invitation(final String email, final String token) {
    send(email, "Te invitaron a Fixi", frontendUrls.pageWithToken("/accept-invitation", token));
  }

  private void send(final String to, final String subject, final String link) {
    final SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(to);
    message.setSubject(subject);
    message.setText(subject + "\n\n" + link + "\n\nEste enlace es de un solo uso.");
    sender.send(message);
  }
}
