package com.hotelpms.auth.repository;
import com.hotelpms.auth.domain.AccountToken;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AccountTokenRepository extends JpaRepository<AccountToken, UUID> {
  Optional<AccountToken> findByTokenHashAndPurpose(String tokenHash, String purpose);
}
