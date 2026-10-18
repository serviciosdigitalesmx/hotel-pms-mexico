package com.hotelpms.frontdesk.portal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** Persistence for expiring portal challenges. */
@SuppressWarnings("checkstyle:MissingJavadocMethod")
public interface PortalOtpChallengeRepository extends JpaRepository<PortalOtpChallenge, UUID> {
    // CHECKSTYLE: JavadocContentLocation|RequireEmptyLineBeforeBlockTagGroup OFF
    /**
     * Finds the latest active challenge for a token.
     *
     * @param tokenId token identifier
     * @return latest unconsumed challenge
     */
    Optional<PortalOtpChallenge> findTopByTokenIdAndConsumedFalseOrderByCreatedAtDesc(UUID tokenId);
    // CHECKSTYLE: JavadocContentLocation|RequireEmptyLineBeforeBlockTagGroup ON
}
