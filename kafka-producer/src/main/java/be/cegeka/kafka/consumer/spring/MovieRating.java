package be.cegeka.kafka.consumer.spring;

import java.time.LocalDate;

public class MovieRating {

    private long movieId;
    private long userId;
    private int rating;
    private LocalDate date;

    public MovieRating(long movieId, long userId, int rating, LocalDate date) {
        this.movieId = movieId;
        this.userId = userId;
        this.rating = rating;
        this.date = date;
    }

    public long getMovieId() {
        return movieId;
    }

    public long getUserId() {
        return userId;
    }

    public int getRating() {
        return rating;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "{" +
                " movieId : " + movieId +
                ", userId : " + userId +
                ", rating : " + rating +
                ", date : " + date +
                '}';
    }
}
