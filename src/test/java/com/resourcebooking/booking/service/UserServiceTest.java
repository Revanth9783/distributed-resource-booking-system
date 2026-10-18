
package com.resourcebooking.booking.service;

import com.resourcebooking.booking.entity.User;
import com.resourcebooking.booking.entity.UserStatus;
import com.resourcebooking.booking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Test
    void createUserShouldHashPasswordBeforeSaving() {

        // Arrange: prepare the dependencies
        UserRepository userRepository = mock(UserRepository.class);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        UserService userService = new UserService(
                userRepository,
                passwordEncoder
        );

        when(userRepository.findByEmail("newuser@example.com"))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    return user;
                });

        // Act: create the user
        User savedUser = userService.createUser(
                "Test User",
                "newuser@example.com",
                "TestPass123!"
        );

        // Assert: verify the password is hashed
        assertNotNull(savedUser.getPasswordHash());
        assertNotEquals("TestPass123!", savedUser.getPasswordHash());
        assertTrue(passwordEncoder.matches(
                "TestPass123!",
                savedUser.getPasswordHash()
        ));

        assertEquals(UserStatus.ACTIVE, savedUser.getStatus());

        verify(userRepository).save(any(User.class));
    }


    @Test
    void createUserShouldRejectDuplicateEmail() {

        UserRepository userRepository = mock(UserRepository.class);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        UserService userService = new UserService(
                userRepository,
                passwordEncoder
        );

        when(userRepository.findByEmail("existing@example.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(
                com.resourcebooking.booking.exception.DuplicateEmailException.class,
                () -> userService.createUser(
                        "Test User",
                        "existing@example.com",
                        "TestPass123!"
                )
        );

        verify(userRepository, never()).save(any(User.class));
    }

}
