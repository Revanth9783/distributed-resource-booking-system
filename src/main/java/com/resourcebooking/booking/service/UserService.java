
package com.resourcebooking.booking.service;

import com.resourcebooking.booking.entity.User;
import com.resourcebooking.booking.entity.UserStatus;
import com.resourcebooking.booking.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.resourcebooking.booking.exception.DuplicateEmailException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(String name, String email, String password) {

        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateEmailException("Email is already registered");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setStatus(UserStatus.ACTIVE);

        return userRepository.save(user);
    }
}
