package local.pryadko.movie_stat.data;

public class Movie {
    public int id;
    public String title;
    public int vote_count;
    public float vote_average;
    public int[] genre_ids;
    public String overview;
    public int[] release_date; // looks like [year, month, day]

    public static final Movie poison = new Movie(); // stop-marker for queue

    public String toString() {
        return title + '(' + vote_average + "/" + vote_count + ", " + formatReleaseDate() + ')';
    }

    private String formatReleaseDate() {
        StringBuilder result = new StringBuilder();
        if (release_date == null || release_date.length == 0)
            return "N/A";
        for (int x : release_date) {
            if (result.length() > 0)
                result.append('.');
            result.append(x);
        }
        return result.toString();
    }
}
