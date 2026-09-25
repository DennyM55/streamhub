package com.dennymathew.streamhub.security;

import com.dennymathew.streamhub.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import java.util.Map;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PublicApiSecurityTest {
    @Test void anonymousBrowsingWorksWhileRegisteredUserCannotMutateCatalogue() throws Exception {
        try (var context = context()) {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            mvc.perform(get("/movies")).andExpect(status().isOk());
            mvc.perform(get("/history")).andExpect(status().isUnauthorized());
            String token=context.getBean(JwtService.class).generateToken("viewer@example.com");
            mvc.perform(post("/movies").header("Authorization","Bearer "+token))
                    .andExpect(status().isForbidden());
            mvc.perform(post("/movies").header("X-Admin-Key","wrong"))
                    .andExpect(status().isUnauthorized());
            mvc.perform(post("/movies").header("X-Admin-Key","admin-test-key"))
                    .andExpect(status().isOk());
            mvc.perform(get("/history").header("Authorization","Bearer corrupted"))
                    .andExpect(status().isUnauthorized());
            mvc.perform(get("/history").header("Authorization","Bearer "+token))
                    .andExpect(status().isOk());
        }
    }
    @Test void demoOnlyBlocksEmailRegistrationAndLoginEvenWithExistingToken() throws Exception {
        try (var context = context(true)) {
            MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            String token = context.getBean(JwtService.class).generateToken("viewer@example.com");
            for (String path : new String[]{"/users", "/users/login"}) {
                mvc.perform(post(path)).andExpect(status().isUnauthorized());
                mvc.perform(post(path).header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
            }
            mvc.perform(get("/movies")).andExpect(status().isOk());
        }
    }
    private AnnotationConfigWebApplicationContext context() { return context(false); }
    private AnnotationConfigWebApplicationContext context(boolean demoOnly) {
        var context=new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test",Map.of(
                "STREAMHUB_DEMO_ONLY", Boolean.toString(demoOnly),
                "streamhub.cors.origin","http://localhost:5173",
                "streamhub.admin.key","admin-test-key",
                "streamhub.jwt.secret","test-jwt-key-that-is-at-least-thirty-two-characters")));
        context.register(TestConfig.class,SecurityConfig.class,JwtAuthenticationFilter.class,
                JwtService.class,RestAuthenticationEntryPoint.class);
        context.refresh();return context;
    }
    @Configuration @EnableWebMvc @EnableWebSecurity
    static class TestConfig {
        @Bean Endpoints endpoints() { return new Endpoints(); }
    }
    @RestController
    static class Endpoints {
        @GetMapping("/movies") String movies() { return "[]"; }
        @PostMapping("/movies") String create() { return "{}"; }
        @GetMapping("/history") String history() { return "[]"; }
    }
}
