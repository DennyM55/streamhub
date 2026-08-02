package com.dennymathew.streamhub.user;

import com.dennymathew.streamhub.catalog.Movie;
import com.dennymathew.streamhub.catalog.MovieRepository;
import com.dennymathew.streamhub.user.dto.FavoriteResponse;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public FavoriteService(
            FavoriteRepository favoriteRepository,
            UserRepository userRepository,
            MovieRepository movieRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
    }

    public FavoriteResponse addFavorite(Long userId, Long movieId) {

        if (favoriteRepository.existsByUserIdAndMovieId(userId, movieId)) {
            throw new IllegalArgumentException("Movie already in favorites");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setMovie(movie);

        Favorite savedFavorite = favoriteRepository.save(favorite);

        return new FavoriteResponse(
                savedFavorite.getId(),
                user.getId(),
                movie.getId(),
                movie.getTitle()
        );
    }

    public List<FavoriteResponse> getFavorites(Long userId) {
        return favoriteRepository.findByUserId(userId)
                .stream()
                .map(favorite -> new FavoriteResponse(
                        favorite.getId(),
                        favorite.getUser().getId(),
                        favorite.getMovie().getId(),
                        favorite.getMovie().getTitle()
                ))
                .toList();
    }

    @Transactional
    public void removeFavorite(Long userId, Long movieId) {

        if (!favoriteRepository.existsByUserIdAndMovieId(userId, movieId)) {
            throw new IllegalArgumentException("Favorite not found");
        }

        favoriteRepository.deleteByUserIdAndMovieId(userId, movieId);
    }
}