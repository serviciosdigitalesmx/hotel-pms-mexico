package com.hotelpms.auth.service;

import java.net.URI;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/** Builds account links against the one configured public Fixi frontend. */
@Component
public final class FixiFrontendUrls {

  private final String baseUrl;

  /**
   * Creates a URL builder from the configured public frontend origin.
   *
   * @param frontendUrl configured origin used to generate account links
   */
  public FixiFrontendUrls(
      @Value("${fixi.frontend-url:${fixi.public-url:http://localhost}}") final String frontendUrl) {
    final URI uri = URI.create(frontendUrl.trim());
    final String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
    if (!"https".equals(scheme) && !"http".equals(scheme)
        || uri.getHost() == null
        || uri.getUserInfo() != null
        || uri.getQuery() != null
        || uri.getFragment() != null
        || uri.getPath() != null && !uri.getPath().isEmpty() && !"/".equals(uri.getPath())) {
      throw new IllegalArgumentException(
          "FIXI_FRONTEND_URL must be an HTTP(S) origin without credentials, path, query, or fragment");
    }
    if ("http".equals(scheme) && !"localhost".equalsIgnoreCase(uri.getHost())) {
      throw new IllegalArgumentException("FIXI_FRONTEND_URL must use HTTPS outside localhost");
    }
    this.baseUrl = uri.toString().replaceAll("/+$", "");
  }

  /**
   * Builds a frontend URL for the supplied application path.
   *
   * @param path absolute path within the frontend application
   * @return absolute URL on the configured frontend origin
   */
  public String page(final String path) {
    return baseUrl + normalizePath(path);
  }

  /**
   * Builds a frontend URL with a safely encoded one-time token query parameter.
   *
   * @param path absolute path within the frontend application
   * @param token one-time account token
   * @return absolute URL with an encoded token parameter
   */
  public String pageWithToken(final String path, final String token) {
    return UriComponentsBuilder.fromUriString(baseUrl)
        .path(normalizePath(path))
        .queryParam("token", token)
        .build()
        .encode()
        .toUriString();
  }

  /**
   * Returns the canonical frontend login URL.
   *
   * @return absolute login URL
   */
  public String login() {
    return page("/login");
  }

  /**
   * Returns the canonical frontend password-recovery URL.
   *
   * @return absolute password-recovery URL
   */
  public String forgotPassword() {
    return page("/forgot-password");
  }

  private static String normalizePath(final String path) {
    if (path == null || !path.startsWith("/") || path.contains("?") || path.contains("#")) {
      throw new IllegalArgumentException("Account frontend paths must be absolute paths without query data");
    }
    return path;
  }
}
