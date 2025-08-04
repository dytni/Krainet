package by.test.krainet.controllers;

import by.test.krainet.dto.AddUser;
import by.test.krainet.dto.SignupRequest;
import by.test.krainet.models.Roles;
import by.test.krainet.models.User;
import by.test.krainet.models.UserDetailsImpl;
import by.test.krainet.service.UserService;
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
        return new ResponseEntity<>(user, HttpStatus.OK);
    }


    @PutMapping("user/editMe")
    public ResponseEntity<?> editUser(@RequestBody SignupRequest signupRequest) {
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        User user = User.userFromDetails((UserDetailsImpl) authentication.getPrincipal());
        if(userService.existsByUsername(signupRequest.getUsername()) &&
                !signupRequest.getUsername().equals(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
        }
        if(userService.existsByEmail(signupRequest.getEmail()) &&
        !signupRequest.getEmail().equals(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
        }
        if(user.getRoles().contains(Roles.USER)){
            //todo
        }
        saveUser(signupRequest, user);
        return ResponseEntity.ok("You edited");
    }

    @DeleteMapping("user/deleteMe")
    private ResponseEntity<?> deleteUser() {
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        User user = User.userFromDetails((UserDetailsImpl) authentication.getPrincipal());
        if(user.getRoles().contains(Roles.USER)){
            //todo
        }
        userService.delete(user);
        return ResponseEntity.ok("You deleted");
    }

    @PostMapping("users/add")
    public ResponseEntity<?> add(@RequestBody AddUser addUser) {
        if(addUser.getFirstName() == null
                || addUser.getLastName() == null
        || addUser.getEmail() == null
        || addUser.getPassword() == null
        || addUser.getUsername() == null
        || addUser.getRoles() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if(userService.existsByUsername(addUser.getUsername())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
        }
        if(userService.existsByEmail(addUser.getEmail())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
        }
        User user = new User();
        saveUser(addUser, user);
        if(user.getRoles().contains(Roles.USER)){
            //todo
        }
        return ResponseEntity.ok("User created");
    }

    @PutMapping("users/edit/{id}")
    public ResponseEntity<?> edit(@RequestBody AddUser addUser,@PathVariable Long id) {
        User user = userService.getById(id);
        if(userService.existsByUsername(addUser.getUsername()) &&
                !addUser.getUsername().equals(user.getUsername())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
        }
        if(userService.existsByEmail(addUser.getEmail()) &&
        !addUser.getEmail().equals(user.getEmail())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
        }
        if(user.getRoles().contains(Roles.USER)){
            //todo
        }
        saveUser(addUser, user);
        return ResponseEntity.ok("User edited");
    }

    @DeleteMapping("users/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        User userToDelete = userService.getById(id);
        userService.delete(userToDelete);
        if(userToDelete.getRoles().contains(Roles.USER)){
            //todo
        }
        if(userToDelete.getId().equals(id)){
            authentication.setAuthenticated(false);
        }
        return ResponseEntity.ok("User deleted");

    }
    @GetMapping("users/show")
    public ResponseEntity<List<User>> show() {
        return new ResponseEntity<>(userService.allUsers(), HttpStatus.OK);
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
