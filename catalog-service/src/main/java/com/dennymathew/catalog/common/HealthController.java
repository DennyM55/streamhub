package com.dennymathew.catalog.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
    /** Process liveness only; the deployment smoke test separately verifies backing services. */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "catalog-service");
    }
}
