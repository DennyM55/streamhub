package com.dennymathew.streamhub.user;

import com.dennymathew.streamhub.security.JwtService;
import com.dennymathew.streamhub.user.dto.LoginRequest;
import com.dennymathew.streamhub.user.dto.LoginResponse;
import com.dennymathew.streamhub.user.dto.RegisterUserRequest;
import com.dennymathew.streamhub.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import org.springframework.security.authentication.BadCredentialsException;

@Service
class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String sayHello() {
        return "Hello Users";
    }

    @Transactional
    public UserResponse createUser(RegisterUserRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must be at most 72 UTF-8 bytes");
        }
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);

        user.setPassword(
                passwordEncoder.encode(request.password())
        );

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail()
        );
    }

    @Transactional
    public LoginResponse createDemoSession() {
        User user = new User();
        user.setName("Demo explorer");
        user.setEmail("demo-" + UUID.randomUUID() + "@streamhub.invalid");
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        userRepository.save(user);
        return new LoginResponse(jwtService.generateToken(user.getEmail()));
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword())) {

            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new LoginResponse(token);
    }
}