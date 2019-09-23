package local.pryadko.movie_stat.data;

public class CustomThreadPool {

    private final Thread[] pool;
    private int threadsInPool;

    public CustomThreadPool(int poolSize) {
        this.pool = new Thread[poolSize];
    }

    public void submit(Runnable task) {
        synchronized (pool) {
            if (threadsInPool < pool.length) {
                pool[threadsInPool] = new Thread(task);
                pool[threadsInPool].start();
                threadsInPool++;
            }
        }
    }

    public void waitFor() {
        boolean finished;
        while (true){
            finished = true;
            synchronized (pool) {
                for (int x = 0; x < threadsInPool; x++)
                    if (pool[x].isAlive())
                        finished = false;
            }
            if (finished)
                break;
            else
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {}
        }
    }

}
