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
            + "'; style-src 'nonce-"
            + nonce
            + "'; connect-src 'self'; base-uri 'none'; form-action 'none'; "
            + "frame-ancestors 'none'; object-src 'none'");
    return ResponseEntity.ok().headers(headers).body(page(nonce));
  }

  private static String page(final String nonce) {
    final String content =
        """
        <p class="eyebrow">Seguridad de cuenta</p>
        <h1>Restablece tu contraseña</h1>
        <p class="lead">Crea una contraseña nueva para volver a entrar a tu cuenta Fixi.</p>
        <div id="reset-status" class="status" data-state="info" role="status" aria-live="polite">
          Elige una contraseña nueva para tu cuenta.
        </div>
        <form id="reset-form">
          <label for="reset-password">Nueva contraseña</label>
          <input id="reset-password" name="password" type="password" autocomplete="new-password"
                 minlength="6" pattern="(?=.*[A-Z]).{6,}" required>
          <p class="helper">Usa al menos 6 caracteres y una mayúscula.</p>
          <button id="reset-button" class="primary" type="submit">Guardar contraseña</button>
        </form>
        <a id="forgot-link" class="login-link" href="https://app.serviciosdigitalesmx.online/forgot-password" hidden>
          Solicitar otro enlace
        </a>
        <a id="login-link" class="login-link" href="https://app.serviciosdigitalesmx.online/login" hidden>
          Ir a iniciar sesión en Fixi
        </a>
        <script nonce="{{nonce}}">
          (() => {
            const status = document.getElementById('reset-status');
            const form = document.getElementById('reset-form');
            const button = document.getElementById('reset-button');
            const forgotLink = document.getElementById('forgot-link');
            const loginLink = document.getElementById('login-link');
            let token = new URLSearchParams(window.location.search).get('token') || '';
            window.history.replaceState(null, document.title, window.location.pathname);
            if (!token) {
              status.dataset.state = 'error';
              status.textContent = 'El enlace no contiene un código válido. Solicita uno nuevo desde Fixi.';
              form.hidden = true;
              forgotLink.hidden = false;
              return;
            }
            form.addEventListener('submit', async (event) => {
              event.preventDefault();
              if (!form.reportValidity()) return;
              button.disabled = true;
              status.dataset.state = 'loading';
              status.textContent = 'Actualizando tu contraseña…';
              const password = new FormData(form).get('password');
              try {
                const response = await fetch('/api/v1/auth/public/reset-password', {
                  method: 'POST',
                  headers: { 'Content-Type': 'application/json' },
                  cache: 'no-store',
                  credentials: 'omit',
                  body: JSON.stringify({ token, password })
                });
                if (response.status === 404 || response.status === 410) {
                  token = '';
                  form.hidden = true;
                  forgotLink.hidden = false;
                  status.dataset.state = 'error';
                  status.textContent = 'El enlace no es válido o venció. Solicita uno nuevo desde Fixi.';
                  return;
                }
                if (!response.ok) throw new Error('reset_failed');
                token = '';
                form.reset();
                form.hidden = true;
                status.dataset.state = 'success';
                status.textContent = 'Contraseña actualizada correctamente. Ya puedes iniciar sesión en Fixi.';
                loginLink.hidden = false;
              } catch {
                status.dataset.state = 'error';
                status.textContent =
                  'No se pudo actualizar. El enlace pudo vencer o la contraseña no cumple los '
                  + 'requisitos.';
                button.disabled = false;
              }
            });
          })();
        </script>
        """.replace("{{nonce}}", nonce);
    return FixiEmailPageLayout.render("Restablecer contraseña", content, nonce);
  }
}
