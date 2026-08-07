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

    public FavoriteResponse addFavoriteByEmail(String email, Long movieId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Long userId = user.getId();

        if (favoriteRepository.existsByUserIdAndMovieId(userId, movieId)) {
            throw new IllegalArgumentException("Movie already in favorites");
        }

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

    public List<FavoriteResponse> getFavoritesByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return favoriteRepository.findByUserId(user.getId())
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