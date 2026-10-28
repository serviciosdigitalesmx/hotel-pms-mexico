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

/** Serves the public password-reset page linked by account recovery emails. */
@RestController
public class PasswordResetPageController {

  private static final SecureRandom NONCE_RANDOM = new SecureRandom();

  /**
   * Displays a reset form without consuming the token on GET, so email scanners cannot use it.
   *
   * @return a no-store HTML page that posts to the canonical reset API
   */
  @GetMapping(value = "/reset-password", produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> resetPasswordPage() {
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
            <title>Restablecer contraseña | Fixi</title>
          </head>
          <body>
            <main>
              <h1>Restablece tu contraseña de Fixi</h1>
              <p id="reset-status">Elige una contraseña nueva para tu cuenta.</p>
              <p>Si no encuentras el correo de recuperación, revisa Spam o correo no deseado.</p>
              <form id="reset-form">
                <label>Nueva contraseña <input name="password" type="password" autocomplete="new-password" minlength="6" pattern="(?=.*[A-Z]).{6,}" required></label>
                <p>Usa al menos 6 caracteres y una mayúscula.</p>
                <button id="reset-button" type="submit">Guardar contraseña</button>
              </form>
            </main>
            <script nonce="%s">
              (() => {
                const status = document.getElementById('reset-status');
                const form = document.getElementById('reset-form');
                const button = document.getElementById('reset-button');
                const token = new URLSearchParams(window.location.search).get('token') || '';
                window.history.replaceState(null, document.title, window.location.pathname);
                if (!token) {
                  status.textContent = 'El enlace no es válido o ya expiró. Solicita otro desde Fixi.';
                  form.hidden = true;
                  return;
                }
                form.addEventListener('submit', async (event) => {
                  event.preventDefault();
                  if (!form.reportValidity()) return;
                  button.disabled = true;
                  status.textContent = 'Actualizando contraseña…';
                  const password = new FormData(form).get('password');
                  try {
                    const response = await fetch('/api/v1/auth/public/reset-password', {
                      method: 'POST',
                      headers: { 'Content-Type': 'application/json' },
                      cache: 'no-store',
                      credentials: 'omit',
                      body: JSON.stringify({ token, password })
                    });
                    if (!response.ok) throw new Error('reset_failed');
                    form.reset();
                    form.hidden = true;
                    status.textContent = 'Contraseña actualizada. Regresa a la aplicación Fixi para iniciar sesión.';
                  } catch {
                    status.textContent = 'No se pudo actualizar. El enlace podría haber vencido o la contraseña no cumplir los requisitos.';
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
