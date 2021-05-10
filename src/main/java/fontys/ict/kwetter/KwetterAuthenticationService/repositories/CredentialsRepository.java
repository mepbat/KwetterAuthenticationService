package fontys.ict.kwetter.KwetterAuthenticationService.repositories;

import fontys.ict.kwetter.KwetterAuthenticationService.models.CredentialsDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CredentialsRepository extends JpaRepository<CredentialsDao, Long> {
    Optional<CredentialsDao> findByUsername(String username);

    @Query(value = "SELECT \n" +
            "credentials.id as 'id'\n" +
            ",username as 'username'\n" +
            ",role.id as 'roleId'\n" +
            ",role.name as 'role'\n" +
            "FROM dbo.credentials\n" +
            "INNER JOIN dbo.role ON dbo.credentials.role_id = dbo.role.id",  nativeQuery = true)
    List<Object> getAllWithoutPasswords();
}
