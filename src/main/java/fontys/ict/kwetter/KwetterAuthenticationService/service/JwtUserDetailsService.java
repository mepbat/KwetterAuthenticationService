package fontys.ict.kwetter.KwetterAuthenticationService.service;

import fontys.ict.kwetter.KwetterAuthenticationService.models.CredentialsDao;
import fontys.ict.kwetter.KwetterAuthenticationService.models.CredentialsDto;
import fontys.ict.kwetter.KwetterAuthenticationService.models.RoleDao;
import fontys.ict.kwetter.KwetterAuthenticationService.repositories.CredentialsRepository;
import fontys.ict.kwetter.KwetterAuthenticationService.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    @Autowired
    private CredentialsRepository credentialsRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder bcryptEncoder;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<CredentialsDao> credentialsDao = credentialsRepository.findByUsername(username);
        if (credentialsDao.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        GrantedAuthority authority = new SimpleGrantedAuthority(credentialsDao.get().getRole().getName());
        return new org.springframework.security.core.userdetails.User(credentialsDao.get().getUsername(), credentialsDao.get().getHashedPassword(),
                Collections.singletonList(authority));
    }

    public CredentialsDao save(CredentialsDto credentialsDto) {
        CredentialsDao newCredentials = new CredentialsDao();
        newCredentials.setUsername(credentialsDto.getUsername());
        newCredentials.setHashedPassword(bcryptEncoder.encode(credentialsDto.getPassword()));
        Optional<RoleDao> role = roleRepository.findRoleDaoByName("user");
        //newCredentials.setAccountId();
        role.ifPresent(newCredentials::setRole);
        newCredentials.setActive(true);
        return credentialsRepository.save(newCredentials);
    }

    public List<CredentialsDao> getAll(){
        List<CredentialsDao> credentialsDaos = credentialsRepository.findAll();
        for (CredentialsDao credentialsDao: credentialsDaos) {
            credentialsDao.setHashedPassword(null);
        }
        return credentialsDaos;
    }

    public void activate(String username){
        Optional<CredentialsDao> optionalCredentialsDao = credentialsRepository.findByUsername(username);
        if(optionalCredentialsDao.isEmpty()) return;
        CredentialsDao credentialsDao = optionalCredentialsDao.get();
        credentialsDao.setActive(true);
        credentialsRepository.save(credentialsDao);
    }

    public void deactivate(String username){
        Optional<CredentialsDao> optionalCredentialsDao = credentialsRepository.findByUsername(username);
        if(optionalCredentialsDao.isEmpty()) return;
        CredentialsDao credentialsDao = optionalCredentialsDao.get();
        credentialsDao.setActive(false);
        credentialsRepository.save(credentialsDao);
    }

    public void promote(String username){
        Optional<CredentialsDao> optionalCredentialsDao = credentialsRepository.findByUsername(username);
        if(optionalCredentialsDao.isEmpty()) return;
        CredentialsDao credentialsDao = optionalCredentialsDao.get();
        Optional<RoleDao> role = roleRepository.findRoleDaoByName("admin");
        role.ifPresent(credentialsDao::setRole);
        credentialsRepository.save(credentialsDao);
    }

    public Optional<CredentialsDao> getCredentialsByUsername(String username){
        return credentialsRepository.findByUsername(username);
    }

}
