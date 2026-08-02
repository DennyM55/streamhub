package com.dennymathew.streamhub.user;

import com.dennymathew.streamhub.user.dto.FavoriteResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{movieId}")
    public FavoriteResponse addFavorite(
            @PathVariable Long userId,
            @PathVariable Long movieId) {

        return favoriteService.addFavorite(userId, movieId);
    }

    @GetMapping
    public List<FavoriteResponse> getFavorites(@PathVariable Long userId) {
        return favoriteService.getFavorites(userId);
    }

    @DeleteMapping("/{movieId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(
            @PathVariable Long userId,
            @PathVariable Long movieId) {

        favoriteService.removeFavorite(userId, movieId);
    }
}