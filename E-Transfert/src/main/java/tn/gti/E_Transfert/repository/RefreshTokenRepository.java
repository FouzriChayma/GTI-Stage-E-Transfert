package tn.gti.E_Transfert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.gti.E_Transfert.entity.RefreshToken;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
}