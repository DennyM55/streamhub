package com.dennymathew.streamhub.history;

import com.dennymathew.streamhub.history.dto.UpdateProgressRequest;
import com.dennymathew.streamhub.history.dto.WatchHistoryResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/history")
public class WatchHistoryController {

    private final WatchHistoryService watchHistoryService;

    public WatchHistoryController(WatchHistoryService watchHistoryService) {
        this.watchHistoryService = watchHistoryService;
    }

    @PutMapping("/{movieId}")
    public WatchHistoryResponse updateProgress(
            @PathVariable Long movieId,
            @RequestBody UpdateProgressRequest request,
            Authentication authentication) {

        return watchHistoryService.saveProgressByEmail(
                authentication.getName(),
                movieId,
                request.progressSeconds()
        );
    }
    
    @GetMapping
    public List<WatchHistoryResponse> getHistory(
            Authentication authentication) {

        return watchHistoryService.getHistoryByEmail(
                authentication.getName()
        );
    }
}