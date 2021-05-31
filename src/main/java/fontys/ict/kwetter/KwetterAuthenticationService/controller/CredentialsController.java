package fontys.ict.kwetter.KwetterAuthenticationService.controller;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fontys.ict.kwetter.KwetterAuthenticationService.config.JwtTokenUtil;
import fontys.ict.kwetter.KwetterAuthenticationService.events.CreateAccountEvent;
import fontys.ict.kwetter.KwetterAuthenticationService.models.*;
import fontys.ict.kwetter.KwetterAuthenticationService.service.JwtUserDetailsService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/credentials")
public class CredentialsController {
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final JwtUserDetailsService userDetailsService;
    private final AmqpTemplate rabbitTemplate;
    private final Gson gson;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    public CredentialsController(JwtTokenUtil jwtTokenUtil, AuthenticationManager authenticationManager, JwtUserDetailsService userDetailsService, AmqpTemplate rabbitTemplate) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.rabbitTemplate = rabbitTemplate;
        this.gson = initiateGson();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/getAll", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getAll() {
        return new ResponseEntity<>(gson.toJson(userDetailsService.getAll()), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/activate/{username}", method = RequestMethod.GET)
    public ResponseEntity<?> activate(@PathVariable("username") String username) {
        userDetailsService.activate(username);
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/deactivate/{username}", method = RequestMethod.GET)
    public ResponseEntity<?> deactivate(@PathVariable("username") String username) {
        userDetailsService.deactivate(username);
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/promote/{username}", method = RequestMethod.GET)
    public ResponseEntity<?> promote(@PathVariable("username") String username) {
        userDetailsService.promote(username);
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        try {
            authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        Optional<CredentialsDao> credentialsDao = userDetailsService.getCredentialsByUsername(authenticationRequest.getUsername());
        CredentialsDao credentials;
        if(credentialsDao.isEmpty()){
            return new ResponseEntity<>("Incorrect username or password!", HttpStatus.UNAUTHORIZED);
        }
        credentials = credentialsDao.get();
        if(!credentials.isActive()) {
            return new ResponseEntity<>("Account deactivated!", HttpStatus.UNAUTHORIZED);

        }
        final String token = jwtTokenUtil.generateToken(userDetails, credentials);

        return new ResponseEntity<>(new JwtResponse(token), HttpStatus.OK);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<?> saveUser(@RequestBody CredentialsDto credentialsDto) {
        if (credentialsDto.getUsername().isEmpty() || credentialsDto.getUsername() == null ||
                credentialsDto.getPassword().isEmpty() || credentialsDto.getPassword() == null) {
            return new ResponseEntity<>("Your username or password cannot be empty!", HttpStatus.BAD_REQUEST);
        }
        if(userDetailsService.getCredentialsByUsername(credentialsDto.getUsername()).isPresent()){
            return new ResponseEntity<>("User already exists!", HttpStatus.BAD_REQUEST);
        }
        CredentialsDao newCredentials = userDetailsService.save(credentialsDto);
        createAccount(newCredentials);
        return new ResponseEntity<>(newCredentials, HttpStatus.CREATED);
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("User disabled!", e);
        } catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or password!", e);
        }
    }

    private void createAccount(CredentialsDao credentialsDao){
        CreateAccountEvent createAccountEvent = new CreateAccountEvent();
        createAccountEvent.setUsername(credentialsDao.getUsername());
        createAccountEvent.setId(credentialsDao.getId());
        rabbitTemplate.convertAndSend(exchange, "create-account", gson.toJson(createAccountEvent));
    }

    private Gson initiateGson() {
        GsonBuilder b = new GsonBuilder();
        b.registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY)
                .excludeFieldsWithModifiers(Modifier.TRANSIENT)
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        boolean exclude = false;
                        try {
                            exclude = EXCLUDE.contains(f.getName());
                            if (f.getDeclaredClass() == CredentialsDao.class) {
                                exclude = true;
                            }

                        } catch (Exception ignore) {
                        }
                        return exclude;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                });
        return b.create();
    }

    private static final List<String> EXCLUDE = new ArrayList<>() {{
        add("credentialsDaoList");
    }};
}
