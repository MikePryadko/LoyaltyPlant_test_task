package local.pryadko.movie_stat;

public class GUI {

    public interface Listener {
        void abort();
    }

    private static final String CLEAR_SCREEN_COMMAND = "\033[H\033[2J";

    public GUI() {
        System.out.println("Program started!");
    }

    public void updateProgress(int progressInPercentage) {
        System.out.println("Progress is " + progressInPercentage + "%");
    }

    public void updateProgress() {
        System.out.println("Progress finished!");
    }

    public void updateProgress(String errorDescription) {
        System.err.println("ERROR: " + errorDescription);
    }

    public void downloadAborted() {
        System.out.println("Download aborted");
    }

    public void print(String text) {
        System.out.println(text);
    }

    public void clearScreen() {
        System.out.print(CLEAR_SCREEN_COMMAND);
        //System.out.flush();
    }

}
