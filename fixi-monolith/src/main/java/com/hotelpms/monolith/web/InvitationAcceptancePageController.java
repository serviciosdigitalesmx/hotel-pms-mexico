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
            + "'; style-src 'nonce-"
            + nonce
            + "'; connect-src 'self'; base-uri 'none'; form-action 'none'; "
            + "frame-ancestors 'none'; object-src 'none'");
    return ResponseEntity.ok().headers(headers).body(page(nonce));
  }

  private static String page(final String nonce) {
    final String content =
        """
        <p class="eyebrow">Invitación de taller</p>
        <h1>Únete a Fixi</h1>
        <p id="invitation-status" class="status" data-state="info" role="status" aria-live="polite">
          Crea tu usuario y contraseña para aceptar la invitación.
        </p>
        <p class="helper">Si no encuentras el correo, revisa Spam o correo no deseado.</p>
        <form id="invitation-form">
          <label for="invite-full-name">Nombre completo</label>
          <input id="invite-full-name" name="fullName" autocomplete="name" maxlength="160" required>
          <label for="invite-username">Usuario</label>
          <input id="invite-username" name="username" autocomplete="username" maxlength="50" required>
          <label for="invite-password">Contraseña de Fixi</label>
          <input id="invite-password" name="password" type="password" autocomplete="new-password"
                 minlength="12" pattern="(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9]).{12,}" required>
          <p class="helper">Usa al menos 12 caracteres, una mayúscula, una minúscula y un número.
            Después podrás iniciar sesión con tu usuario o correo y esta contraseña.</p>
          <button id="accept-button" class="primary" type="submit">Crear contraseña y aceptar invitación</button>
        </form>
        <a id="login-link" class="login-link" href="https://app.serviciosdigitalesmx.online/login" hidden>
          Ir a iniciar sesión en Fixi
        </a>
        <script nonce="{{nonce}}">
          (() => {
            const status = document.getElementById('invitation-status');
            const form = document.getElementById('invitation-form');
            const button = document.getElementById('accept-button');
            const loginLink = document.getElementById('login-link');
            let token = new URLSearchParams(window.location.search).get('token') || '';
            window.history.replaceState(null, document.title, window.location.pathname);
            if (!token) {
              status.dataset.state = 'error';
              status.textContent =
                'El enlace no contiene un código de invitación válido. Solicita otra invitación '
                + 'al administrador de tu taller.';
              form.hidden = true;
              return;
            }
            form.addEventListener('submit', async (event) => {
              event.preventDefault();
              if (!form.reportValidity()) return;
              button.disabled = true;
              status.dataset.state = 'loading';
              status.textContent = 'Estamos aceptando tu invitación…';
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
                token = '';
                form.reset();
                form.hidden = true;
                status.dataset.state = 'success';
                status.textContent =
                  'Invitación aceptada. Tu contraseña quedó configurada y ya puedes iniciar '
                  + 'sesión en Fixi.';
                loginLink.hidden = false;
              } catch {
                status.dataset.state = 'error';
                status.textContent =
                  'No se pudo aceptar. El enlace pudo vencer o la cuenta ya existe. Si hace falta, '
                  + 'solicita otra invitación al administrador del taller.';
                button.disabled = false;
              }
            });
          })();
        </script>
        """.replace("{{nonce}}", nonce);
    return FixiEmailPageLayout.render("Aceptar invitación", content, nonce);
  }
}
