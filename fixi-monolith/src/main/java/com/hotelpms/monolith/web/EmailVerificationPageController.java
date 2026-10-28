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

/** Serves the public confirmation page linked by account-verification emails. */
@RestController
public class EmailVerificationPageController {

  private static final SecureRandom NONCE_RANDOM = new SecureRandom();

  /**
   * Displays a confirmation page without consuming the token on GET. Email security scanners
   * commonly follow links automatically, so verification only happens after an explicit click.
   *
   * @return a no-store HTML page that posts the token to the canonical verification API
   */
  @GetMapping(value = "/verify-email", produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> verifyEmailPage() {
    final byte[] nonceBytes = new byte[18];
    NONCE_RANDOM.nextBytes(nonceBytes);
    final String nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(nonceBytes);
    final String html = verificationPage(nonce);

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
    return ResponseEntity.ok().headers(headers).body(html);
  }

  private static String verificationPage(final String nonce) {
    return """
        <!doctype html>
        <html lang="es">
          <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Verificar correo | Fixi</title>
          </head>
          <body>
            <main>
              <h1>Verifica tu correo de Fixi</h1>
              <p id="verification-status">Confirma que quieres activar tu cuenta.</p>
              <p>Si no encuentras el correo, revisa las carpetas de Spam o correo no deseado.</p>
              <button id="verify-button" type="button">Confirmar correo</button>
            </main>
            <script nonce="%s">
              (() => {
                const status = document.getElementById('verification-status');
                const button = document.getElementById('verify-button');
                const token = new URLSearchParams(window.location.search).get('token') || '';
                window.history.replaceState(null, document.title, window.location.pathname);
                if (!token) {
                  status.textContent = 'El enlace no es válido o ya expiró. Solicita uno nuevo.';
                  button.disabled = true;
                  return;
                }
                button.addEventListener('click', async () => {
                  button.disabled = true;
                  status.textContent = 'Verificando…';
                  try {
                    const response = await fetch(
                      '/api/v1/auth/public/verify-email?token=' + encodeURIComponent(token),
                      { method: 'POST', cache: 'no-store', credentials: 'omit' }
                    );
                    if (!response.ok) throw new Error('verification_failed');
                    status.textContent = 'Correo verificado. Tu cuenta ya está activa. Regresa a la aplicación Fixi para iniciar sesión y completar el registro del taller.';
                    button.hidden = true;
                  } catch {
                    status.textContent = 'El enlace no es válido o ya expiró. Solicita uno nuevo.';
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
