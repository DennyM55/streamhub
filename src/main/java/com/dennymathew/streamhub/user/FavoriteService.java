package com.dennymathew.streamhub.user;

import com.dennymathew.streamhub.catalog.CatalogClient;
import com.dennymathew.streamhub.catalog.dto.MovieResponse;
import com.dennymathew.streamhub.user.dto.FavoriteResponse;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final CatalogClient catalogClient;

    public FavoriteService(
            FavoriteRepository favoriteRepository,
            UserRepository userRepository,
            CatalogClient catalogClient) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.catalogClient = catalogClient;
    }

    public FavoriteResponse addFavoriteByEmail(String email, Long movieId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Long userId = user.getId();

        if (favoriteRepository.existsByUserIdAndMovieId(userId, movieId)) {
            throw new IllegalArgumentException("Movie already in favorites");
        }

        MovieResponse movie = catalogClient.getMovie(movieId);

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setMovieId(movieId);

        Favorite savedFavorite = favoriteRepository.save(favorite);

        return new FavoriteResponse(
                savedFavorite.getId(),
                user.getId(),
                movie.id(),
                movie.title()
        );
    }
    public List<FavoriteResponse> getFavoritesByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Favorite> favorites =
                favoriteRepository.findByUserId(user.getId());

        List<Long> movieIds = favorites.stream()
                .map(Favorite::getMovieId)
                .toList();

        List<MovieResponse> movies =
                catalogClient.getMoviesByIds(movieIds);

        return favorites.stream()
                .map(favorite -> {
                    MovieResponse movie = movies.stream()
                            .filter(m -> m.id().equals(favorite.getMovieId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Movie not found"));

                    return new FavoriteResponse(
                            favorite.getId(),
                            favorite.getUser().getId(),
                            favorite.getMovieId(),
                            movie.title()
                    );
                })
                .toList();
    }

    @Transactional
    public void removeFavoriteByEmail(String email, Long movieId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Long userId = user.getId();

        if (!favoriteRepository.existsByUserIdAndMovieId(userId, movieId)) {
            throw new IllegalArgumentException("Favorite not found");
        }

        favoriteRepository.deleteByUserIdAndMovieId(userId, movieId);
    }
}
