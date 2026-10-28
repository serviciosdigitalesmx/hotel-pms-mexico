package com.hotelpms.monolith.web;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public page for accepting one-time Fixi invitations without exposing the API to browser auth. */
@RestController
public class InvitationAcceptancePageController {

  private static final SecureRandom NONCE_RANDOM = new SecureRandom();

  /**
   * Renders the password-creation form without consuming the invitation token on GET.
   *
   * @return a no-store invitation acceptance page
   */
  @GetMapping(value = "/accept-invitation", produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> acceptInvitationPage() {
    final byte[] nonceBytes = new byte[18];
    NONCE_RANDOM.nextBytes(nonceBytes);
    final String nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(nonceBytes);
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(new MediaType("text", "html", StandardCharsets.UTF_8));
    headers.setCacheControl(CacheControl.noStore().getHeaderValue());
    headers.setPragma("no-cache");
    headers.set("Referrer-Policy", "no-referrer");
    headers.set("X-Content-Type-Options", "nosniff");
    headers.set("X-Frame-Options", "DENY");
    headers.set(
        "Content-Security-Policy",
        "default-src 'none'; script-src 'nonce-"
            + nonce
            + "'; connect-src 'self'; base-uri 'none'; form-action 'none'; "
            + "frame-ancestors 'none'; object-src 'none'");
    return ResponseEntity.ok().headers(headers).body(page(nonce));
  }

  private static String page(final String nonce) {
    return """
        <!doctype html>
        <html lang="es">
          <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Aceptar invitación | Fixi</title>
          </head>
          <body>
            <main>
              <h1>Únete a Fixi</h1>
              <p id="invitation-status">Crea tu usuario y contraseña de Fixi para aceptar la invitación.</p>
              <p>Si no encuentras el correo de invitación, revisa Spam o correo no deseado.</p>
              <form id="invitation-form">
                <label>Nombre completo <input name="fullName" autocomplete="name" maxlength="160" required></label>
                <label>Usuario <input name="username" autocomplete="username" maxlength="50" required></label>
                <label>Contraseña de Fixi <input name="password" type="password" autocomplete="new-password" minlength="12" pattern="(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9]).{12,}" required></label>
                <p>Usa al menos 12 caracteres, una mayúscula, una minúscula y un número. Después podrás iniciar sesión con tu correo o usuario y esta contraseña; Google no es necesario.</p>
                <button id="accept-button" type="submit">Crear contraseña y aceptar invitación</button>
              </form>
            </main>
            <script nonce="%s">
              (() => {
                const status = document.getElementById('invitation-status');
                const form = document.getElementById('invitation-form');
                const button = document.getElementById('accept-button');
                const token = new URLSearchParams(window.location.search).get('token') || '';
                window.history.replaceState(null, document.title, window.location.pathname);
                if (!token) {
                  status.textContent = 'El enlace no es válido o ya venció. Solicita una nueva invitación.';
                  form.hidden = true;
                  return;
                }
                form.addEventListener('submit', async (event) => {
                  event.preventDefault();
                  if (!form.reportValidity()) return;
                  button.disabled = true;
                  status.textContent = 'Aceptando invitación…';
                  const values = new FormData(form);
                  try {
                    const response = await fetch('/api/v1/auth/public/invitations/accept', {
                      method: 'POST',
                      headers: { 'Content-Type': 'application/json' },
                      cache: 'no-store',
                      credentials: 'omit',
                      body: JSON.stringify({
                        token,
                        fullName: values.get('fullName'),
                        username: values.get('username'),
                        password: values.get('password')
                      })
                    });
                    if (!response.ok) throw new Error('invitation_failed');
                    form.reset();
                    form.hidden = true;
                    status.textContent = 'Invitación aceptada. Tu contraseña de Fixi quedó configurada. Regresa a la aplicación Fixi para iniciar sesión con tu correo o usuario.';
                  } catch {
                    status.textContent = 'No se pudo aceptar la invitación. El enlace podría haber vencido o la cuenta ya existir.';
                    button.disabled = false;
                  }
                });
              })();
            </script>
          </body>
        </html>
        """.formatted(nonce);
  }
}
