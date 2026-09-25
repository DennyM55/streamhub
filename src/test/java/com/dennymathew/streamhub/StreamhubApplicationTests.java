package com.dennymathew.streamhub;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_SYSTEM_TESTS", matches = "true")
class StreamhubApplicationTests {

    @Test
    void contextLoads() {
    }

}
