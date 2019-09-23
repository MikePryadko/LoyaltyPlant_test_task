package local.pryadko.movie_stat;

import local.pryadko.movie_stat.data.Genre;
import local.pryadko.movie_stat.data.Movie;

import java.util.concurrent.BlockingQueue;

public class Analyzer {

    public interface Listener {
        void analyzerFinished();
    }

    private final Listener listener;
    private final BlockingQueue<Movie> movieQueue;
    private final Genre[] genres;
    private Integer movieCount = 0;
    private static final int THREADS_COUNT = Runtime.getRuntime().availableProcessors();
    private Integer threadsCounter = 0;

    public Analyzer(Listener listener, BlockingQueue<Movie> movieQueue, Genre[] genres) {
        this.listener = listener;
        this.movieQueue = movieQueue;
        this.genres = genres;

        // spawn threads
        for (int x = 0; x < THREADS_COUNT; x++) {
            int threadNum = x;
            synchronized (threadsCounter) {
                threadsCounter++;
            }
            new Thread(() -> {
                Movie movie;
                while (true) {
                    try {
                        movie = movieQueue.take();
                        if (movie == Movie.poison) {
                            movieQueue.put(Movie.poison); // for other threads
                            break;
                        }

                        // get genres and calculate statistics
                        for (int genreId : movie.genre_ids) {
                            for (Genre genre : genres) {
                                if (genre.id != genreId)
                                    continue;
                                synchronized (genre) {
                                    if (movie.vote_count < 1)
                                        genre.moviesWithoutVotes++;
                                    else {
                                        genre.filmsCount++;
                                        genre.voteSumm += movie.vote_average;
                                        if (genre.bestMovie == null || movie.vote_average > genre.bestMovie.vote_average)
                                            genre.bestMovie = movie;
                                        if (genre.worstMovie == null || movie.vote_average < genre.worstMovie.vote_average)
                                            genre.worstMovie = movie;
                                        // TODO calculate votes distribution
                                    }
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        break; // на всякий случай
                    }
                }
                synchronized (threadsCounter) {
                    threadsCounter--;
                    if (threadsCounter == 0)
                        listener.analyzerFinished();
                }
            }).start();
        }
    }
}
