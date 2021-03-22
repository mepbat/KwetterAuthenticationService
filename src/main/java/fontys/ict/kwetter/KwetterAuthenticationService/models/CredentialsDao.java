package fontys.ict.kwetter.KwetterAuthenticationService.models;

import javax.persistence.*;

@Entity
@Table(name = "Credentials")
public class CredentialsDao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String username;
    @Column
    private String hashedPassword;

    @GeneratedValue
    private Long accountId;

    public CredentialsDao() {
    }

    public CredentialsDao(Long id, String username, String hashedPassword, Long accountId) {
        this.id = id;
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.accountId = accountId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
