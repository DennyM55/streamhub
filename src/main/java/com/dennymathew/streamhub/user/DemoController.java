package com.dennymathew.streamhub.user;
import com.dennymathew.streamhub.user.dto.LoginResponse;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/demo")
public class DemoController {
    private final UserService users;
    public DemoController(UserService users) { this.users = users; }
    @PostMapping("/session")
    public LoginResponse createSession() { return users.createDemoSession(); }
}
