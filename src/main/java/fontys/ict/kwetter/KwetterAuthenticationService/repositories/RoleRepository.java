package fontys.ict.kwetter.KwetterAuthenticationService.repositories;

import fontys.ict.kwetter.KwetterAuthenticationService.models.RoleDao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleDao,Long> {
    Optional<RoleDao> findRoleDaoByName(String name);
}
