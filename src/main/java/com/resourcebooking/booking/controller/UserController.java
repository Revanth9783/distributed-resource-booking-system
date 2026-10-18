
package com.resourcebooking.booking.controller;

import com.resourcebooking.booking.dto.CreateUserRequest;
import com.resourcebooking.booking.dto.UserResponse;
import com.resourcebooking.booking.entity.User;
import com.resourcebooking.booking.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(
            @Valid @RequestBody CreateUserRequest request) {

        User user = userService.createUser(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );

        return UserResponse.from(user);
    }
}
