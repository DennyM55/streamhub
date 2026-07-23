package com.dennymathew.streamhub.user;

import org.springframework.stereotype.Service;

@Service
class UserService {
    final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public String sayHello(){
        return "Hello Users";
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }
}
