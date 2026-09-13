package kr.zipimoney.global.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class HealthController {

    private final Instant startedAt = Instant.now();

    @GetMapping("/")
    public Map<String, Object> health() {
        return Map.of(
                "status", "ok",
                "service", "zipimoney-api",
                "startedAt", startedAt.toString(),
                "timestamp", Instant.now().toString()
        );
    }
}
