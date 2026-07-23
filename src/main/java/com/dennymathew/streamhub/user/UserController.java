package com.dennymathew.streamhub.user;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    UserService userService;

    UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/hello")
    public String sayHello(){
        return userService.sayHello();
    }
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

}
