package com.dennymathew.streamhub.user;

import com.dennymathew.streamhub.user.dto.LoginRequest;
import com.dennymathew.streamhub.user.dto.LoginResponse;
import com.dennymathew.streamhub.user.dto.RegisterUserRequest;
import com.dennymathew.streamhub.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/hello")
    public String sayHello() {
        return userService.sayHello();
    }

    @PostMapping
    public UserResponse createUser(@Valid @RequestBody RegisterUserRequest user) {
        return userService.createUser(user);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }
}
