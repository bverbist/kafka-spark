package be.cegeka.kafka.consumer.spring;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

@Component
public class NetflixDataReader {

    private static final String DATA_PATH = "D:/Development/explorationDays/nf_prize_dataset.tar/download/training_set";

    public Stream<MovieRating> streamMovieRatings() {
        return Stream.of(new File(DATA_PATH).listFiles())
            .flatMap(file -> getRatings(file))
            .limit(5000);
    }

    private Stream<MovieRating> getRatings(File movieFile) {
        try {
            long movieId = getMovieIdFromFileName(movieFile.getName());
            return IOUtils.readLines(new FileInputStream(movieFile)).stream()
                    .skip(1)
                    .map(s -> toRating(s, movieId));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long getMovieIdFromFileName(String movieFileName) {
        return Long.valueOf(movieFileName.substring(3, 10));
    }

    private MovieRating toRating(String ratingAsString, long movieId) {
        String[] parts = ratingAsString.split(",");
        long userId = Long.valueOf(parts[0]);
        int rating = Integer.valueOf(parts[1]);
        LocalDate date = LocalDate.parse(parts[2], DateTimeFormatter.ISO_DATE);
        return new MovieRating(movieId, userId, rating, date);
    }

}
