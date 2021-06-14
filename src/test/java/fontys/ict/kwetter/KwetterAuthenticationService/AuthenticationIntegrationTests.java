package fontys.ict.kwetter.KwetterAuthenticationService;

import com.google.gson.Gson;
import fontys.ict.kwetter.KwetterAuthenticationService.config.JwtAuthenticationEntryPoint;
import fontys.ict.kwetter.KwetterAuthenticationService.config.JwtTokenUtil;
import fontys.ict.kwetter.KwetterAuthenticationService.config.WebSecurityConfiguration;
import fontys.ict.kwetter.KwetterAuthenticationService.controller.CredentialsController;
import fontys.ict.kwetter.KwetterAuthenticationService.models.CredentialsDao;
import fontys.ict.kwetter.KwetterAuthenticationService.models.CredentialsDto;
import fontys.ict.kwetter.KwetterAuthenticationService.models.JwtRequest;
import fontys.ict.kwetter.KwetterAuthenticationService.models.RoleDao;
import fontys.ict.kwetter.KwetterAuthenticationService.repositories.CredentialsRepository;
import fontys.ict.kwetter.KwetterAuthenticationService.service.JwtUserDetailsService;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = KwetterAuthenticationServiceApplication.class)
@WebMvcTest(CredentialsController.class)
class AuthenticationIntegrationTests {
	@Autowired
	private MockMvc mvc;
	@MockBean
	private CredentialsRepository credentialsRepository;
	@MockBean
	private JwtUserDetailsService jwtUserDetailsService;
	@MockBean
	private AmqpTemplate rabbitTemplate;
	@Autowired
	private WebSecurityConfiguration webSecurityConfiguration;
	@MockBean
	private JwtTokenUtil jwtTokenUtil;
	@MockBean
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	@MockBean
	private AuthenticationManager authenticationManager;

	private final Gson gson = new Gson();

	@Before()
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void contextLoads() {
		assertThat(credentialsRepository).isNotNull();
		assertThat(jwtUserDetailsService).isNotNull();
		assertThat(webSecurityConfiguration).isNotNull();
		assertThat(jwtAuthenticationEntryPoint).isNotNull();
		assertThat(jwtTokenUtil).isNotNull();
		assertThat(webSecurityConfiguration).isNotNull();
	}

	@Test
	public void registerAPI() throws Exception {
		CredentialsDao credentialsDao = new CredentialsDao(0L,true, new RoleDao(),"user","user");
		CredentialsDto credentialsDto = new CredentialsDto("user","user");
		given(jwtUserDetailsService.save(any())).willReturn(credentialsDao);
		mvc.perform(MockMvcRequestBuilders
				.post("/credentials/register").content(gson.toJson(credentialsDto))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty());
	}

	@Test
	public void authenticateAPI() throws Exception {
		JwtRequest jwtRequest = new JwtRequest("user","user");
		UserDetails userDetails = new User("user","user", Collections.singletonList(new SimpleGrantedAuthority("user")));
		CredentialsDao credentialsDao = new CredentialsDao(0L, true, new RoleDao(), "user", "user");
		given(jwtUserDetailsService.loadUserByUsername(jwtRequest.getUsername())).willReturn(userDetails);
		given(jwtUserDetailsService.getCredentialsByUsername(jwtRequest.getUsername())).willReturn(Optional.of(credentialsDao));
		given(authenticationManager.authenticate(any())).willReturn(null);
		given(jwtTokenUtil.generateToken(any(), any())).willReturn("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
		mvc.perform(MockMvcRequestBuilders
				.post("/credentials/authenticate").content(gson.toJson(jwtRequest))
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.token").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"));
	}

	@Test
	@WithMockUser(username = "admin", authorities = "admin")
	public void getAllAPI() throws Exception {
		CredentialsDao credentialsDao = new CredentialsDao(0L,true, new RoleDao(),"user","user");
		given(jwtUserDetailsService.getAll()).willReturn(Collections.singletonList(credentialsDao));
		mvc.perform(MockMvcRequestBuilders
				.get("/credentials/getAll")
				.accept(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[*].id").isNotEmpty());
	}

	@Test
	@WithMockUser(username = "admin", authorities = "admin")
	public void activateAPI() throws Exception {
		CredentialsDao credentialsDao = new CredentialsDao(0L,true, new RoleDao(),"user","user");
		given(jwtUserDetailsService.getAll()).willReturn(Collections.singletonList(credentialsDao));
		mvc.perform(MockMvcRequestBuilders
				.get("/credentials/getAll")
				.accept(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[*].id").isNotEmpty());
	}
}
