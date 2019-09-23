package local.pryadko.movie_stat;


public class ProgressWatcher extends Thread {

    public interface Listener {
        float getProgress(); // in percents (from 0 to 100)
    }

    private final Listener listener;
    private final static int SLEEP_TIMEOUT = 300;
    private boolean aborted;

    public ProgressWatcher(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        final long timestamp = System.currentTimeMillis();
        long time = timestamp;
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(SLEEP_TIMEOUT);
            } catch (InterruptedException e) {
                break;
            }
            time = (System.currentTimeMillis() - timestamp)/1000;
            System.out.printf("\rProgress: %5.1f%%, time left: %02d:%02d ", listener.getProgress(), time / 60, time % 60);
            System.out.flush();
        }
        System.out.printf("\rDownloading and analysis took %02d:%02d\n", time / 60, time % 60);
    }

    public void abort() {
        this.interrupt();
        try {
            this.join();
        } catch (InterruptedException ignored) {}
    }
}
