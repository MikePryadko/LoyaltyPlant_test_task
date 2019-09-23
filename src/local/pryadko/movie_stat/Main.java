package local.pryadko.movie_stat;

import local.pryadko.movie_stat.data.Genre;
import local.pryadko.movie_stat.data.Movie;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main implements Downloader.Listener, Analyzer.Listener {

    private static Main instance;
    private Genre[] genres;
    private static final int MOVIE_QUEUE_SIZE = 1000;
    private BlockingQueue<Movie> movieQueue = new ArrayBlockingQueue<>(MOVIE_QUEUE_SIZE);
    //private GUI gui;
    private Analyzer analyzer;
    private Downloader downloader;
    //private ProgressWatcher progressWatcher;
    private ProgressWatcher progressWatcher;
    private volatile boolean finished;

    public static void main(String[] args) {
	    instance = new Main();
	    instance.start();
    }

    private void start() {
        //gui = new GUI();
        downloader = new Downloader(instance);
        System.out.print("Downloading genres... ");
        genres = downloader.downloadGenres();
        if (genres == null) {
            System.err.println("ERROR");
            return;
        }
        System.out.println("[OK] (" + genres.length + " items)");
        downloader.downloadMovies();
        analyzer = new Analyzer(instance, movieQueue, genres);
    }

    @Override
    public void analyzerFinished() {
        finished = true;
        System.out.println("Analyzer finished. Results: (sorted by average rating)");
        Arrays.sort(genres, (g1, g2) -> {
            float g1f = g1.filmsCount > 0 ? g1.voteSumm/g1.filmsCount : 0f;
            float g2f = g2.filmsCount > 0 ? g2.voteSumm/g2.filmsCount : 0f;
            if (g1f == 0f && g2f == 0f)
                return g1.name.compareToIgnoreCase(g2.name);
            return g1f >= g2f ? -1 : 1;
        });
        for (Genre genre : genres) {
            System.out.println("\t" + genre.toString());
        }
    }

    @Override
    public void downloadStarted(int pageCount, int movieCount) {
        System.out.println("Download started (pages: " + pageCount + ", movies: " + movieCount + ")");
        System.out.println("(For aborting press Ctrl+C)");
        progressWatcher = new ProgressWatcher(downloader);
        progressWatcher.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!finished)
                System.err.println("\nProgram aborted!");
        }));
    }

    @Override
    public void addMovies(Movie[] movies) {
        for (Movie movie : movies) {
            try {
                movieQueue.put(movie);
            } catch (InterruptedException ignored) {}
        }
    }

    @Override
    public void downloadMoviesFinished() {
        progressWatcher.abort();
        System.out.println("Downloading finished");
        movieQueue.offer(Movie.poison);
    }

    @Override
    public void downloadError(String description) {
        downloader.stop();
        movieQueue.offer(Movie.poison);
        System.err.println("DOWNLOAD ERROR: " + description);
    }
}
