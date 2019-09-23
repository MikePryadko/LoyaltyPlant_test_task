package local.pryadko.movie_stat;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import local.pryadko.movie_stat.data.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Downloader implements ProgressWatcher.Listener {

    public interface Listener {
        void downloadStarted(int pageCount, int movieCount);
        void addMovies(Movie[] movies);
        void downloadMoviesFinished();
        void downloadError(String description);
    }

    private static final String HOST = "https://easy.test-assignment-a.loyaltyplant.net/";
    private static final String API_KEY = "{protected by NDA}";
    private static final String ROUTE_GENRES = "3/genre/movie/list";
    private static final String ROUTE_MOVIES = "3/discover/movie";
    private final Listener listener;
    private final int THREADS_COUNT = 5;
    private final int CONNECT_TIMEOUT_MS = 2000;
    private final int DOWNLOAD_TIMEOUT_MS = 3000;
    private final Gson gson = new Gson();
    private int pagesDownloaded;
    private int pagesTotal;
    private final Object pagesMutex = new Object();
    private volatile boolean stop = false;

    public Downloader(Listener listener) {
        this.listener = listener;
        //this.THREADS_COUNT = threadsCount;
    }

    public Genre[] downloadGenres() {
        try {
            GenreList genreList = requestGet(getGenresRoute(), GenreList.class);
            return genreList.genres;
        } catch (JsonSyntaxException g) {
            listener.downloadError("Data format error ==> " + g.getMessage());
            return null;
        } catch (Exception e) {
            listener.downloadError(e.getMessage());
            return null;
        }
    }

    public void downloadMovies() {
        //ExecutorService pool = Executors.newFixedThreadPool(THREADS_COUNT);
        CustomThreadPool pool = new CustomThreadPool(THREADS_COUNT);
        // downloading movies in many threads
        createDownloadNode(pool, 1);
        new Thread(() -> {
            pool.waitFor();
            listener.downloadMoviesFinished();
        }).start();
    }

    private void createDownloadNode(CustomThreadPool pool, int nodeNum) {
        pool.submit(() -> {
            //listener.downloadNodeSpawned(nodeNum);
            int pageCounter = 0;
            MoviePage moviePage;
            int pageToDownload = nodeNum;
            boolean firstIteration = true;
            while (!stop && (firstIteration || pageToDownload < pagesTotal)) {
                try {
                    moviePage = requestGet(getMovieRoute(pageToDownload), MoviePage.class);
                    pageCounter++;
                    synchronized (pagesMutex) {
                        pagesTotal = moviePage.total_pages;
                        pagesDownloaded++;
                    }

                    // spawn other threads if we are the first and if it's first iteration and server has enough pages ;)
                    if (firstIteration && nodeNum == 1) {
                        listener.downloadStarted(moviePage.total_pages, moviePage.total_results);
                        for (int x = 2; x <= THREADS_COUNT && x < moviePage.total_pages; x++)
                            createDownloadNode(pool, x);
                    }
                    firstIteration = false;

                    // give movies to listener
                    if (moviePage.results != null && moviePage.results.length > 0)
                        listener.addMovies(moviePage.results);

                    synchronized (pagesMutex) {
                        pageToDownload = THREADS_COUNT * pageCounter + nodeNum;
                    }
                } catch (Exception e) {
                    break;
                }
            }
        });
    }

    private String getGenresRoute() {
        return HOST + ROUTE_GENRES + "?api_key=" + API_KEY;
    }

    private String getMovieRoute(int pageNumber) {
        return HOST + ROUTE_MOVIES + "?api_key=" + API_KEY + "&page=" + pageNumber;
    }

    private <T> T requestGet(String url, Class<T> classOfBody) throws Exception {
        T result;
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setConnectTimeout(CONNECT_TIMEOUT_MS);
            con.setReadTimeout(DOWNLOAD_TIMEOUT_MS);
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Connection", "Keep-Alive");
            con.connect();
            if (con.getResponseCode() == HttpsURLConnection.HTTP_OK)
                result = gson.fromJson(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8), classOfBody);
            else
                throw new Exception("Server's answer is " + con.getResponseCode());
        } finally {
            if (con != null)
                con.disconnect();
        }
        return result;
    }

    public void stop() {
        stop = true;
    }

    @Override
    public float getProgress() {
        // return download progress in percents (from 0 to 100)
        synchronized (pagesMutex) {
            return Math.round(pagesDownloaded * 1000f / (float)(pagesTotal-1)) / 10f;
        }
    }
}
