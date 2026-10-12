package com.hotelpms.frontdesk.portal;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Tenant and hash scoped token access. */
@SuppressWarnings("checkstyle:MissingJavadocMethod")
public interface PublicPortalTokenRepository extends JpaRepository<PublicPortalToken, UUID> {
  // CHECKSTYLE: JavadocContentLocation|RequireEmptyLineBeforeBlockTagGroup OFF
  /** Finds an active token by its stored hash.
   * @param tokenHash SHA-256 token hash
   * @return active token
   */
  Optional<PublicPortalToken> findByTokenHashAndRevokedFalse(String tokenHash);
  // CHECKSTYLE: JavadocContentLocation|RequireEmptyLineBeforeBlockTagGroup ON
}
