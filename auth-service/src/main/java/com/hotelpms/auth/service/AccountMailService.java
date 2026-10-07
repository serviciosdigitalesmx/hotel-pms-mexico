package com.hotelpms.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AccountMailService {
  private final JavaMailSender sender;
  private final String publicUrl;
  private final String from;
  public AccountMailService(JavaMailSender sender,
      @Value("${fixi.public-url:http://localhost}") String publicUrl,
      @Value("${fixi.mail-from:no-reply@fixi.local}") String from) {
    this.sender=sender; this.publicUrl=publicUrl; this.from=from;
  }
  public void verification(String email, String token) { send(email,"Verifica tu cuenta Fixi", publicUrl+"/verify-email?token="+token); }
  public void reset(String email, String token) { send(email,"Restablece tu contraseña Fixi", publicUrl+"/reset-password?token="+token); }
  public void invitation(String email, String token) { send(email,"Te invitaron a Fixi", publicUrl+"/accept-invitation?token="+token); }
  private void send(String to,String subject,String link){ var m=new SimpleMailMessage();m.setFrom(from);m.setTo(to);m.setSubject(subject);m.setText(subject+"\n\n"+link+"\n\nEste enlace es de un solo uso.");sender.send(m); }
}
