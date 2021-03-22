package fontys.ict.kwetter.KwetterAuthenticationService.models;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "Role")
public class RoleDao {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @Column
    private String name;
    @OneToMany(mappedBy = "role")
    List<CredentialsDao> credentialsDaoList;

    public RoleDao() {
    }

    public RoleDao(Long id, String name, List<CredentialsDao> credentialsDaoList) {
        this.id = id;
        this.name = name;
        this.credentialsDaoList = credentialsDaoList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CredentialsDao> getCredentialsDaoList() {
        return credentialsDaoList;
    }

    public void setCredentialsDaoList(List<CredentialsDao> credentialsDaoList) {
        this.credentialsDaoList = credentialsDaoList;
    }
}
