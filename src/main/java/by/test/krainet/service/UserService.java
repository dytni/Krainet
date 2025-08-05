package by.test.krainet.service;

import by.test.krainet.dto.Letter;
import by.test.krainet.models.User;
import by.test.krainet.models.UserDetailsImpl;
import by.test.krainet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {



    private KafkaTemplate<String, Letter> kafkaTemplate;
    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository, KafkaTemplate<String, Letter> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(
                String.format("User %s not found", username)));
        return UserDetailsImpl.build(user);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }

    public User getById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> allUsers() {
        return userRepository.findAll();
    }

    public void sendNotification(String eventType, User user, String password) {
        Letter event = new Letter();
        event.setEventType(eventType);
        event.setUserId(user.getId());
        event.setUsername(user.getUsername());
        event.setEmail(user.getEmail());
        event.setPassword(password);

        kafkaTemplate.send("user-events", event);
    }

}
