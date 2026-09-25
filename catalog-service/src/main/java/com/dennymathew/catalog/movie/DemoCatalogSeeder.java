package com.dennymathew.catalog.movie;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Opt-in, repeatable demo data. Originals and licenses: https://studio.blender.org/films/ */
@Component
@ConditionalOnProperty(name = "streamhub.seed-demo", havingValue = "true")
public class DemoCatalogSeeder implements ApplicationRunner {
    private static final String SAMPLE_MEDIA = "https://storage.googleapis.com/gtv-videos-bucket/sample/";
    private final MovieRepository repository;

    public DemoCatalogSeeder(MovieRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        for (SeedFilm film : films()) {
            if (repository.existsByTitleIgnoreCase(film.title())) {
                continue;
            }
            Movie movie = new Movie();
            movie.setTitle(film.title());
            movie.setDescription(film.description() + " Open film: Blender Foundation / Blender Studio. Credits: https://studio.blender.org/films/");
            movie.setGenre(film.genre());
            movie.setReleaseYear(film.year());
            movie.setDurationMinutes(film.minutes());
            if (film.sampleFile() != null) {
                movie.setMediaUrl(SAMPLE_MEDIA + film.sampleFile() + ".mp4");
                movie.setThumbnailUrl(SAMPLE_MEDIA + "images/" + film.sampleFile() + ".jpg");
            }
            repository.save(movie);
        }
    }

    private static List<SeedFilm> films() {
        return List.of(
                new SeedFilm("Big Buck Bunny", "A gentle rabbit faces three mischievous forest creatures.", "Animation", 2008, 10, "BigBuckBunny"),
                new SeedFilm("Sintel", "A young traveler searches for a dragon she once befriended.", "Fantasy", 2010, 15, "Sintel"),
                new SeedFilm("Tears of Steel", "A group confronts a robotic threat in a futuristic Amsterdam.", "Sci-Fi", 2012, 13, "TearsOfSteel"),
                new SeedFilm("Elephants Dream", "Two companions explore a vast and unsettling mechanical world.", "Sci-Fi", 2006, 11, "ElephantsDream"),
                new SeedFilm("Cosmos Laundromat", "A sheep on a remote island is offered an extraordinary escape.", "Fantasy", 2015, 12, null),
                new SeedFilm("Spring", "A shepherd and her dog venture into a forest to awaken spring.", "Fantasy", 2019, 8, null),
                new SeedFilm("Agent 327: Operation Barbershop", "An agent follows a lead into a barbershop with a dangerous secret.", "Action", 2017, 4, null),
                new SeedFilm("Caminandes: Llamigos", "Koro the llama competes for a berry in a snowy landscape.", "Adventure", 2016, 3, null),
                new SeedFilm("Glass Half", "Two critics find their opinions challenged by an unexpected artwork.", "Comedy", 2015, 3, null),
                new SeedFilm("Hero", "A fast-moving adventure explores hand-drawn animation in Blender.", "Animation", 2018, 4, null),
                new SeedFilm("Coffee Run", "A cup of coffee stirs a journey through a woman's memories.", "Animation", 2020, 3, null),
                new SeedFilm("Charge", "An intruder searching for energy encounters a security machine.", "Action", 2022, 4, null)
        );
    }

    private record SeedFilm(String title, String description, String genre, int year, int minutes, String sampleFile) { }
}
