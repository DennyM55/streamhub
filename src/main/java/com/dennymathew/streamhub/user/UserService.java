package com.dennymathew.streamhub.user;

import com.dennymathew.streamhub.user.dto.RegisterUserRequest;
import com.dennymathew.streamhub.user.dto.UserResponse;
import org.springframework.stereotype.Service;

@Service
class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String sayHello() {
        return "Hello Users";
    }

    public UserResponse createUser(RegisterUserRequest request) {

        User user = new User();
        user.setName(request.name());

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName()
        );
    }
}