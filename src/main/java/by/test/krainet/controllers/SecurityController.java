package by.test.krainet.controllers;

import by.test.krainet.configuration.JwtCore;
import by.test.krainet.dto.SigninRequest;
import by.test.krainet.dto.SignupRequest;
import by.test.krainet.models.Roles;
import by.test.krainet.models.User;
import by.test.krainet.repository.UserRepository;
import by.test.krainet.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/auth")
public class SecurityController {


    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);

    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private UserService userService;
    private AuthenticationManager authenticationManager;
    private JwtCore jwtCore;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    public void setJwtCore(JwtCore jwtCore) {
        this.jwtCore = jwtCore;
    }

    @PostMapping("/signup")
    ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        logger.info("Attempt to register new user: {}", signupRequest.getUsername());

        if(userRepository.existsByUsername(signupRequest.getUsername())){
            logger.warn("Registration failed - username already exists: {}", signupRequest.getUsername());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
        }
        if(userRepository.existsByEmail(signupRequest.getEmail())){
            logger.warn("Registration failed - email already exists: {}", signupRequest.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
        }

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setEmail(signupRequest.getEmail());
        user.setRoles(Set.of(Roles.USER));
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());

        userRepository.save(user);

        userService.sendNotification("CREATED", user, signupRequest.getPassword());

        logger.info("User registered successfully: {}", user.getUsername());
        return ResponseEntity.ok("User created");
    }

    @PostMapping("/signin")
    ResponseEntity<?> signin(@RequestBody SigninRequest signinRequest) {
        logger.info("Login attempt for user: {}", signinRequest.getUsername());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            signinRequest.getUsername(),
                            signinRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            logger.warn("Login failed for user: {}", signinRequest.getUsername(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtCore.generateToken(authentication);

        logger.info("User authenticated successfully: {}", signinRequest.getUsername());
        return ResponseEntity.ok(jwt);
    }

}
