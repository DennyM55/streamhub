package com.dennymathew.streamhub.user;

import com.dennymathew.streamhub.user.dto.FavoriteResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{movieId}")
    public FavoriteResponse addFavorite(
            @PathVariable Long movieId,
            Authentication authentication) {

        return favoriteService.addFavoriteByEmail(
                authentication.getName(),
                movieId
        );
    }

    @GetMapping
    public List<FavoriteResponse> getFavorites(Authentication authentication) {
        return favoriteService.getFavoritesByEmail(authentication.getName());
    }

    @DeleteMapping("/{movieId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(
            @PathVariable Long movieId,
            Authentication authentication) {

        favoriteService.removeFavoriteByEmail(
                authentication.getName(),
                movieId
        );
    }
}