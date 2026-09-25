package com.dennymathew.catalog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_SYSTEM_TESTS", matches = "true")
class CatalogServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
