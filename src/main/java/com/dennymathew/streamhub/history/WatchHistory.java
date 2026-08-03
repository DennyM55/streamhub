package com.dennymathew.streamhub.history;

import com.dennymathew.streamhub.catalog.Movie;
import com.dennymathew.streamhub.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "watch_history",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "movie_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class WatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    private Integer progressSeconds;
}
