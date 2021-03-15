package fontys.ict.kwetter.KwetterAuthenticationService.repositories;

import fontys.ict.kwetter.KwetterAuthenticationService.models.CredentialsDao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredentialsRepository extends JpaRepository<CredentialsDao, Long> {
    Optional<CredentialsDao> findByUsername(String username);
}
