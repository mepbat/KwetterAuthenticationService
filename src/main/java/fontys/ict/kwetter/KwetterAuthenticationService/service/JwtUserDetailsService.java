package fontys.ict.kwetter.KwetterAuthenticationService.service;

import fontys.ict.kwetter.KwetterAuthenticationService.models.CredentialsDao;
import fontys.ict.kwetter.KwetterAuthenticationService.models.CredentialsDto;
import fontys.ict.kwetter.KwetterAuthenticationService.models.RoleDao;
import fontys.ict.kwetter.KwetterAuthenticationService.repositories.CredentialsRepository;
import fontys.ict.kwetter.KwetterAuthenticationService.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

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
        return new org.springframework.security.core.userdetails.User(credentialsDao.get().getUsername(), credentialsDao.get().getHashedPassword(),
                new ArrayList<>());
    }

    public CredentialsDao save(CredentialsDto credentialsDto) {
        CredentialsDao newCredentials = new CredentialsDao();
        newCredentials.setUsername(credentialsDto.getUsername());
        newCredentials.setHashedPassword(bcryptEncoder.encode(credentialsDto.getPassword()));
        Optional<RoleDao> role = roleRepository.findRoleDaoByName("user");
        role.ifPresent(newCredentials::setRole);
        return credentialsRepository.save(newCredentials);
    }

    public Optional<CredentialsDao> getCredentialsByUsername(String username){
        return credentialsRepository.findByUsername(username);
    }

}
