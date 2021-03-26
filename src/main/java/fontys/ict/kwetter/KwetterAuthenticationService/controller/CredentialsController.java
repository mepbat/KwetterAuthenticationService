package fontys.ict.kwetter.KwetterAuthenticationService.controller;

import com.google.gson.Gson;
import fontys.ict.kwetter.KwetterAuthenticationService.config.JwtTokenUtil;
import fontys.ict.kwetter.KwetterAuthenticationService.events.CreateAccountEvent;
import fontys.ict.kwetter.KwetterAuthenticationService.models.*;
import fontys.ict.kwetter.KwetterAuthenticationService.service.JwtUserDetailsService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
    private final AmqpTemplate rabbitTemplate;
    private final Gson gson;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    public CredentialsController(JwtTokenUtil jwtTokenUtil, AuthenticationManager authenticationManager, JwtUserDetailsService userDetailsService, AmqpTemplate rabbitTemplate, Gson gson) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.rabbitTemplate = rabbitTemplate;
        this.gson = gson;
    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        Optional<CredentialsDao> credentialsDao = userDetailsService.getCredentialsByUsername(authenticationRequest.getUsername());
        CredentialsDao credentials;
        if(credentialsDao.isEmpty()){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        credentials = credentialsDao.get();

        final String token = jwtTokenUtil.generateToken(userDetails, credentials);

        return new ResponseEntity<>(new JwtResponse(token), HttpStatus.OK);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<?> saveUser(@RequestBody CredentialsDto credentialsDto) {
        if (credentialsDto.getUsername().isEmpty() || credentialsDto.getUsername() == null ||
                credentialsDto.getPassword().isEmpty() || credentialsDto.getPassword() == null) {
            return new ResponseEntity<>("Your email or password cannot be empty!", HttpStatus.BAD_REQUEST);
        }
        if(userDetailsService.getCredentialsByUsername(credentialsDto.getUsername()).isPresent()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        CredentialsDao newCredentials = userDetailsService.save(credentialsDto);
        createAccount(newCredentials);
        return new ResponseEntity<>(newCredentials, HttpStatus.CREATED);
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

    private void createAccount(CredentialsDao credentialsDao){
        CreateAccountEvent createAccountEvent = new CreateAccountEvent();
        createAccountEvent.setUsername(credentialsDao.getUsername());
        createAccountEvent.setId(credentialsDao.getId());
        rabbitTemplate.convertAndSend(exchange, "create-account", gson.toJson(createAccountEvent));
    }
}
