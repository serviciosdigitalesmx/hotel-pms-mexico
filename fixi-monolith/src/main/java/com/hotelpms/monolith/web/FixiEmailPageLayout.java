package com.hotelpms.monolith.web;

/** Shared, self-contained visual shell for account emails opened on the monolith API host. */
final class FixiEmailPageLayout {

  private static final String SHELL =
      """
      <!doctype html>
      <html lang="es">
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <meta name="color-scheme" content="light">
          <title>{{title}} | Fixi</title>
          <style nonce="{{nonce}}">
            :root {
              font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont,
                "Segoe UI", sans-serif;
              color: #17212b;
              background: #f6f8fa;
              font-synthesis: none;
              text-rendering: optimizeLegibility;
            }
            * { box-sizing: border-box; }
            body {
              min-height: 100vh;
              margin: 0;
              display: grid;
              place-items: center;
              padding: 24px;
              background: radial-gradient(ellipse at 10% 0%, #d7f2f5 0, transparent 38%), #f6f8fa;
            }
            .shell { width: min(100%, 480px); }
            .brand {
              display: flex;
              align-items: center;
              justify-content: center;
              gap: 10px;
              margin: 0 0 18px;
              color: #164e63;
              font-size: 18px;
              font-weight: 850;
              letter-spacing: .2em;
              text-decoration: none;
            }
            .brand-mark {
              display: grid;
              width: 38px;
              height: 38px;
              place-items: center;
              border-radius: 12px;
              background: #164e63;
              color: #fff;
              font-size: 19px;
              letter-spacing: 0;
              box-shadow: 0 5px 14px #164e6333;
            }
            .card {
              padding: clamp(24px, 6vw, 38px);
              border: 1px solid #e3e8ed;
              border-radius: 22px;
              background: #fff;
              box-shadow: 0 18px 55px #17212b12;
            }
            .eyebrow {
              margin: 0 0 8px;
              color: #52606d;
              font-size: 12px;
              font-weight: 750;
              letter-spacing: .12em;
              text-transform: uppercase;
            }
            h1 {
              margin: 0 0 12px;
              color: #17212b;
              font-size: clamp(25px, 6vw, 32px);
              line-height: 1.2;
              letter-spacing: -.035em;
            }
            .lead { margin: 0 0 22px; color: #52606d; font-size: 15px; line-height: 1.65; }
            .status {
              margin: 18px 0;
              padding: 13px 15px;
              border: 1px solid #c7d1d6;
              border-radius: 12px;
              background: #f6f8fa;
              color: #17212b;
              font-size: 14px;
              line-height: 1.55;
            }
            .status[data-state="success"] { border-color: #a7d9c4; background: #effaf4; color: #14532d; }
            .status[data-state="error"] { border-color: #f2b8b5; background: #fff4f3; color: #7f1d1d; }
            .status[data-state="loading"] { border-color: #a9d8df; background: #effbfc; color: #083344; }
            .helper { margin: 0 0 22px; color: #52606d; font-size: 13px; line-height: 1.6; }
            label { display: block; margin: 15px 0 7px; color: #17212b; font-size: 14px; font-weight: 650; }
            input {
              width: 100%;
              min-height: 46px;
              padding: 11px 13px;
              border: 1px solid #aebbc2;
              border-radius: 10px;
              background: #fff;
              color: #17212b;
              font: inherit;
            }
            input:focus { border-color: #164e63; outline: 3px solid #164e6326; }
            .primary, .secondary, .login-link {
              display: inline-flex;
              min-height: 46px;
              align-items: center;
              justify-content: center;
              border-radius: 11px;
              font: inherit;
              font-size: 14px;
              font-weight: 700;
              text-decoration: none;
              cursor: pointer;
            }
            .primary {
              width: 100%;
              margin-top: 8px;
              padding: 12px 18px;
              border: 0;
              background: #164e63;
              color: #fff;
              box-shadow: 0 5px 12px #164e6326;
            }
            .primary:hover { background: #0f3f50; }
            .secondary {
              width: 100%;
              margin-top: 12px;
              padding: 11px 16px;
              border: 1px solid #9eb5bc;
              background: #fff;
              color: #164e63;
            }
            .primary:disabled, .secondary:disabled { cursor: wait; opacity: .65; }
            .login-link { width: 100%; margin-top: 10px; padding: 11px 16px; background: #d7f2f5; color: #083344; }
            .login-link[hidden], form[hidden] { display: none; }
            .footer {
              margin: 18px 8px 0;
              color: #65727d;
              font-size: 12px;
              line-height: 1.6;
              text-align: center;
            }
            @media (max-width: 420px) { body { padding: 16px; } .card { border-radius: 18px; } }
          </style>
        </head>
        <body>
          <main class="shell">
            <a class="brand" href="https://app.serviciosdigitalesmx.online/login"
               aria-label="Fixi, iniciar sesión">
              <span class="brand-mark" aria-hidden="true">F</span><span>FIXI</span>
            </a>
            <section class="card" aria-live="polite">{{content}}</section>
            <p class="footer">
              Plataforma Fixi para talleres de reparación.<br>
              Si no esperabas este correo, puedes cerrar esta página.
            </p>
          </main>
        </body>
      </html>
      """;

  private FixiEmailPageLayout() {}

  static String render(final String title, final String content, final String nonce) {
    return SHELL.replace("{{title}}", title)
        .replace("{{content}}", content)
        .replace("{{nonce}}", nonce);
  }
}
