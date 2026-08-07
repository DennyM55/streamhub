package com.dennymathew.streamhub.history;

import com.dennymathew.streamhub.history.dto.UpdateProgressRequest;
import com.dennymathew.streamhub.history.dto.WatchHistoryResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/history")
public class WatchHistoryController {

    private final WatchHistoryService watchHistoryService;

    public WatchHistoryController(WatchHistoryService watchHistoryService) {
        this.watchHistoryService = watchHistoryService;
    }

    @PutMapping("/{movieId}")
    public WatchHistoryResponse updateProgress(
            @PathVariable Long userId,
            @PathVariable Long movieId,
            @RequestBody UpdateProgressRequest request) {

        return watchHistoryService.saveProgress(
                userId,
                movieId,
                request.progressSeconds()
        );
    }

    @GetMapping
    public List<WatchHistoryResponse> getHistory(Authentication authentication) {
        String email = authentication.getName();
        return watchHistoryService.getHistoryByEmail(email);
    }
}