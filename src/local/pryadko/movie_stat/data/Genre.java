package local.pryadko.movie_stat.data;

public class Genre {
    public int id;
    public String name;

    // additional fields
    public int filmsCount;
    public float voteSumm;
    public int moviesWithoutVotes;
    //public float voteAverage;
    public Movie bestMovie;
    public Movie worstMovie;
    //private static final int VOTE_DISTRIBUTION_SIZE = 10;
    //public int[] voteDistribution = new int[VOTE_DISTRIBUTION_SIZE];

    public String toString() {
        return String.format("Genre: %s, movies without votes: %d, vote average: %.1f, best movie: %s, worst movie: %s",
                name, moviesWithoutVotes, voteSumm/filmsCount, bestMovie.toString(), worstMovie.toString());
    }
}
