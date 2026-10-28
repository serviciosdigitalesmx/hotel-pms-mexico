package com.hotelpms.auth.repository;

import com.hotelpms.auth.domain.AccountToken;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

  /**
   * Atomically consumes an unexpired, unused token.
   *
   * @param tokenHash SHA-256 digest
   * @param purpose token purpose
   * @param now current application time
   * @return number of rows consumed; exactly one indicates success
   */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      "UPDATE AccountToken token SET token.consumedAt = :now "
          + "WHERE token.tokenHash = :tokenHash AND token.purpose = :purpose "
          + "AND token.consumedAt IS NULL AND token.expiresAt > :now")
  int consumeIfAvailable(
      @Param("tokenHash") String tokenHash,
      @Param("purpose") String purpose,
      @Param("now") LocalDateTime now);
}
