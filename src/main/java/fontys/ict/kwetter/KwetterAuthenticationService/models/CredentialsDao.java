package fontys.ict.kwetter.KwetterAuthenticationService.models;

import javax.persistence.*;

@Entity
@Table(name = "Credentials")
public class CredentialsDao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;
    @ManyToOne
    @JoinColumn(name = "role_id")
    private RoleDao role;
    @Column(unique = true)
    private String username;
    @Column
    private String hashedPassword;


    public CredentialsDao() {
    }

    public CredentialsDao(Long id, Long accountId, RoleDao role, String username, String hashedPassword) {
        this.id = id;
        this.accountId = accountId;
        this.role = role;
        this.username = username;
        this.hashedPassword = hashedPassword;
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

    public RoleDao getRole() {
        return role;
    }

    public void setRole(RoleDao role) {
        this.role = role;
    }
}
