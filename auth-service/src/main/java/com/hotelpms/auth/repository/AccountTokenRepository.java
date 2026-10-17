package com.hotelpms.auth.repository;

import com.hotelpms.auth.domain.AccountToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence access for hashed account verification and recovery tokens. */
public interface AccountTokenRepository extends JpaRepository<AccountToken, UUID> {
  /**
   * Finds a token by its digest and intended use.
   *
   * @param tokenHash SHA-256 digest
   * @param purpose token purpose
   * @return matching token, if any
   */
  Optional<AccountToken> findByTokenHashAndPurpose(String tokenHash, String purpose);
}
