package com.hotelpms.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Delivers account verification, password reset, and invitation messages. */
@Service
public class AccountMailService {
  private final JavaMailSender sender;
  private final String publicUrl;
  private final String from;

  /**
   * Creates the mail service with the configured sender and public URL.
   *
   * @param sender configured mail transport
   * @param publicUrl browser-accessible Fixi base URL
   * @param from sender address used in account messages
   */
  public AccountMailService(
      final JavaMailSender sender,
      @Value("${fixi.public-url:http://localhost}") final String publicUrl,
      @Value("${fixi.mail-from:no-reply@fixi.local}") final String from) {
    this.sender = sender;
    this.publicUrl = publicUrl;
    this.from = from;
  }

  /**
   * Sends a one-time account verification link.
   *
   * @param email recipient address
   * @param token one-time verification token
   */
  public void verification(final String email, final String token) {
    send(email, "Verifica tu cuenta Fixi", publicUrl + "/verify-email?token=" + token);
  }

  /**
   * Sends a password reset link.
   *
   * @param email recipient address
   * @param token one-time reset token
   */
  public void reset(final String email, final String token) {
    send(email, "Restablece tu contraseña Fixi", publicUrl + "/reset-password?token=" + token);
  }

  /**
   * Sends a tenant invitation link.
   *
   * @param email recipient address
   * @param token one-time invitation token
   */
  public void invitation(final String email, final String token) {
    send(email, "Te invitaron a Fixi", publicUrl + "/accept-invitation?token=" + token);
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
