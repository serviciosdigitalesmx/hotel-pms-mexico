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
            + "'; style-src 'nonce-"
            + nonce
            + "'; connect-src 'self'; base-uri 'none'; form-action 'none'; "
            + "frame-ancestors 'none'; object-src 'none'");
    return ResponseEntity.ok().headers(headers).body(html);
  }

  private static String verificationPage(final String nonce) {
    final String content =
        """
        <p class="eyebrow">Activación de cuenta</p>
        <h1>Confirma tu correo</h1>
        <p class="lead">Verifica que esta dirección te pertenece para activar tu cuenta Fixi.</p>
        <div id="verification-status" class="status" data-state="info" role="status" aria-live="polite">
          Pulsa el botón para confirmar tu dirección de correo.
        </div>
        <button id="verify-button" class="primary" type="button">Confirmar correo</button>
        <form id="resend-form" hidden>
          <label for="resend-email">Correo de tu cuenta</label>
          <input id="resend-email" name="email" type="email" autocomplete="email" required>
          <button id="resend-button" class="secondary" type="submit">Enviar un nuevo enlace</button>
        </form>
        <p id="resend-status" class="status" data-state="info" role="status" aria-live="polite" hidden></p>
        <a id="login-link" class="login-link" href="https://app.serviciosdigitalesmx.online/login" hidden>
          Ir a iniciar sesión en Fixi
        </a>
        <script nonce="{{nonce}}">
          (() => {
            const status = document.getElementById('verification-status');
            const button = document.getElementById('verify-button');
            const resendForm = document.getElementById('resend-form');
            const resendButton = document.getElementById('resend-button');
            const resendStatus = document.getElementById('resend-status');
            const loginLink = document.getElementById('login-link');
            let token = new URLSearchParams(window.location.search).get('token') || '';
            window.history.replaceState(null, document.title, window.location.pathname);

            const showResend = (message) => {
              status.dataset.state = 'error';
              status.textContent = message;
              button.hidden = true;
              resendForm.hidden = false;
            };
            if (!token) {
              showResend('Este enlace no contiene un código de verificación. Solicita uno nuevo.');
            }

            button.addEventListener('click', async () => {
              if (!token) return;
              button.disabled = true;
              status.dataset.state = 'loading';
              status.textContent = 'Estamos verificando tu correo…';
              let response;
              try {
                response = await fetch(
                  '/api/v1/auth/public/verify-email?token=' + encodeURIComponent(token),
                  { method: 'POST', cache: 'no-store', credentials: 'omit' }
                );
              } catch {
                status.dataset.state = 'error';
                status.textContent = 'No pudimos conectar con Fixi. Comprueba tu conexión e inténtalo de nuevo.';
                button.disabled = false;
                return;
              }
              if (response.status === 410) {
                token = '';
                showResend('El enlace venció. Solicita que Fixi te envíe uno nuevo.');
                return;
              }
              if (response.status === 404) {
                token = '';
                showResend('El enlace no es válido. Puedes solicitar un nuevo correo de verificación.');
                return;
              }
              if (response.status === 200) {
                const result = await response.json().catch(() => null);
                if (result?.status === 'ALREADY_VERIFIED') {
                  token = '';
                  status.dataset.state = 'success';
                  status.textContent = 'Este correo ya estaba verificado. Puedes iniciar sesión en Fixi.';
                  button.hidden = true;
                  loginLink.hidden = false;
                  return;
                }
              }
              if (response.status !== 204) {
                status.dataset.state = 'error';
                status.textContent = 'Fixi recibió la solicitud, pero no pudo verificar el enlace. Inténtalo de nuevo más tarde.';
                button.disabled = false;
                return;
              }
              token = '';
              status.dataset.state = 'success';
              status.textContent = 'Correo verificado. Tu cuenta quedó activada y ya puedes iniciar sesión en Fixi.';
              button.hidden = true;
              loginLink.hidden = false;
            });

            resendForm.addEventListener('submit', async (event) => {
              event.preventDefault();
              if (!resendForm.reportValidity()) return;
              resendButton.disabled = true;
              resendStatus.hidden = false;
              resendStatus.dataset.state = 'loading';
              resendStatus.textContent = 'Solicitando un nuevo correo…';
              try {
                const email = new FormData(resendForm).get('email');
                const response = await fetch('/api/v1/auth/public/resend-verification', {
                  method: 'POST',
                  headers: { 'Content-Type': 'application/json' },
                  cache: 'no-store',
                  credentials: 'omit',
                  body: JSON.stringify({ email })
                });
                if (response.status !== 202) throw new Error('resend_failed');
                resendStatus.dataset.state = 'success';
                resendStatus.textContent =
                  'Si existe una cuenta pendiente para ese correo, te enviaremos un nuevo correo de verificación. '
                  + 'Revisa tu bandeja de entrada y, si no aparece, Spam o correo no deseado.';
                resendForm.reset();
              } catch {
                resendStatus.dataset.state = 'error';
                resendStatus.textContent = 'No pudimos solicitar el correo. Inténtalo de nuevo más tarde.';
                resendButton.disabled = false;
              }
            });
          })();
        </script>
        """.replace("{{nonce}}", nonce);
    return FixiEmailPageLayout.render("Verificar correo", content, nonce);
  }
}
