package fontys.ict.kwetter.KwetterAuthenticationService.controller;

import fontys.ict.kwetter.KwetterAuthenticationService.config.JwtTokenUtil;
import fontys.ict.kwetter.KwetterAuthenticationService.models.CredentialsDao;
import fontys.ict.kwetter.KwetterAuthenticationService.models.CredentialsDto;
import fontys.ict.kwetter.KwetterAuthenticationService.models.JwtRequest;
import fontys.ict.kwetter.KwetterAuthenticationService.models.JwtResponse;
import fontys.ict.kwetter.KwetterAuthenticationService.service.JwtUserDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@CrossOrigin(origins = {"http://localhost:4200","http://localhost:8081","http://localhost:8082","http://localhost:8083"})
@RequestMapping(value = "/credentials")
public class CredentialsController {
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final JwtUserDetailsService userDetailsService;

    public CredentialsController(JwtTokenUtil jwtTokenUtil, AuthenticationManager authenticationManager, JwtUserDetailsService userDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        Optional<CredentialsDao> credentialsDao = userDetailsService.getCredentialsByUsername(authenticationRequest.getUsername());
        CredentialsDao credentials;
        if(credentialsDao.isEmpty()){
            return ResponseEntity.ok(new JwtResponse(null));
        }
        credentials = credentialsDao.get();

        final String token = jwtTokenUtil.generateToken(userDetails, credentials.getAccountId().toString());

        return ResponseEntity.ok(new JwtResponse(token));
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<?> saveUser(@RequestBody CredentialsDto credentialsDto) {
        return ResponseEntity.ok(userDetailsService.save(credentialsDto));
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }



}
