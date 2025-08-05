package by.test.krainet.controllers;

import by.test.krainet.dto.AddUser;
import by.test.krainet.dto.SignupRequest;
import by.test.krainet.models.Roles;
import by.test.krainet.models.User;
import by.test.krainet.models.UserDetailsImpl;
import by.test.krainet.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping()
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("user/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = User.userFromDetails((UserDetailsImpl) auth.getPrincipal());
        logger.info("Current user retrieved: {}", user.getUsername());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PutMapping("user/editMe")
    public ResponseEntity<?> editUser(@RequestBody SignupRequest signupRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = User.userFromDetails((UserDetailsImpl) authentication.getPrincipal());
        logger.info("User {} attempting self-edit", user.getUsername());

        if (userService.existsByUsername(signupRequest.getUsername()) &&
                !signupRequest.getUsername().equals(user.getUsername())) {
            logger.warn("Edit failed - username already exists: {}", signupRequest.getUsername());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
        }
        if (userService.existsByEmail(signupRequest.getEmail()) &&
                !signupRequest.getEmail().equals(user.getEmail())) {
            logger.warn("Edit failed - email already exists: {}", signupRequest.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
        }
        saveUser(signupRequest, user);

        if(user.getRoles().contains(Roles.USER)){

            userService.sendNotification("UPDATED", user,
                    (signupRequest.getPassword() == null) ? user.getPassword() : signupRequest.getPassword());
        }

        logger.info("User {} updated their profile successfully", user.getUsername());
        return ResponseEntity.ok("You edited");
    }

    @DeleteMapping("user/deleteMe")
    private ResponseEntity<?> deleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = User.userFromDetails((UserDetailsImpl) authentication.getPrincipal());
        logger.info("User {} attempting self-deletion", user.getUsername());

        if(user.getRoles().contains(Roles.USER)){
            userService.sendNotification("DELETED", user, "deleted");
        }
        userService.delete(user);
        logger.info("User {} deleted their account", user.getUsername());
        return ResponseEntity.ok("You deleted");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("users/add")
    public ResponseEntity<?> add(@RequestBody AddUser addUser) {
        logger.info("Admin attempting to add new user: {}", addUser.getUsername());

        if (addUser.getFirstName() == null || addUser.getLastName() == null ||
                addUser.getEmail() == null || addUser.getPassword() == null ||
                addUser.getUsername() == null || addUser.getRoles() == null) {
            logger.warn("Add user failed - missing required fields");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (userService.existsByUsername(addUser.getUsername())) {
            logger.warn("Add user failed - username exists: {}", addUser.getUsername());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
        }
        if (userService.existsByEmail(addUser.getEmail())) {
            logger.warn("Add user failed - email exists: {}", addUser.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
        }

        User user = new User();
        saveUser(addUser, user);

        if(user.getRoles().contains(Roles.USER)){
            userService.sendNotification("CREATED", user, addUser.getPassword());
        }

        logger.info("User created successfully by admin: {}", user.getUsername());
        return ResponseEntity.ok("User created");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("users/edit/{id}")
    public ResponseEntity<?> edit(@RequestBody AddUser addUser, @PathVariable Long id) {
        logger.info("Admin attempting to edit user ID: {}", id);

        User user = userService.getById(id);
        if (userService.existsByUsername(addUser.getUsername()) &&
                !addUser.getUsername().equals(user.getUsername())) {
            logger.warn("Edit user failed - username exists: {}", addUser.getUsername());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
        }
        if (userService.existsByEmail(addUser.getEmail()) &&
                !addUser.getEmail().equals(user.getEmail())) {
            logger.warn("Edit user failed - email exists: {}", addUser.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
        }
        saveUser(addUser, user);
        if(user.getRoles().contains(Roles.USER)){
            userService.sendNotification("UPDATED", user, addUser.getPassword());
        }
        logger.info("User ID {} updated successfully by admin", id);
        return ResponseEntity.ok("User edited");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("users/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        logger.info("Admin attempting to delete user ID: {}", id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userToDelete = userService.getById(id);

        userService.delete(userToDelete);
        logger.info("User ID {} deleted successfully by admin", id);
        if (userToDelete.getRoles().contains(Roles.USER)) {
            userService.sendNotification("DELETED", userToDelete, "deleted");
        }

        if (authentication.getName().equals(userToDelete.getUsername())) {
            authentication.setAuthenticated(false);
            logger.info("Admin deleted their own account, session invalidated");
        }

        return ResponseEntity.ok("User deleted");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("users/show")
    public ResponseEntity<List<User>> show() {
        logger.info("Admin requesting all users list");
        List<User> users = userService.allUsers();
        logger.info("Returning {} users", users.size());
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    private void saveUser(AddUser addUser, User user) {
        if(addUser.getFirstName() != null && !addUser.getFirstName().isEmpty()){
            user.setFirstName(addUser.getFirstName());
        }
        if(addUser.getLastName() != null && !addUser.getLastName().isEmpty()){
            user.setLastName(addUser.getLastName());
        }
        if(addUser.getEmail() != null && !addUser.getEmail().isEmpty()){
            user.setEmail(addUser.getEmail());
        }
        if(addUser.getPassword() != null && !addUser.getPassword().isEmpty()){
            user.setPassword(passwordEncoder.encode(addUser.getPassword()));
        }
        if(addUser.getRoles() != null && !addUser.getRoles().isEmpty()){
            user.setRoles(addUser.getRoles());
        }
        if(addUser.getUsername() != null && !addUser.getUsername().isEmpty()){
            user.setUsername(addUser.getUsername());
        }
        userService.save(user);
    }
    private void saveUser(SignupRequest addUser, User user) {
        if(addUser.getFirstName() != null){
            user.setFirstName(addUser.getFirstName());
        }
        if(addUser.getLastName() != null){
            user.setLastName(addUser.getLastName());
        }
        if(addUser.getEmail() != null){
            user.setEmail(addUser.getEmail());
        }
        if(addUser.getPassword() != null){
            user.setPassword(passwordEncoder.encode(addUser.getPassword()));
        }
        if(addUser.getUsername() != null){
            user.setUsername(addUser.getUsername());
        }
        userService.save(user);
    }

}
