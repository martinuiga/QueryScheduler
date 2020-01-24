import java.util.concurrent.ExecutorService;

public abstract class AbstractScheduler {

    public abstract ExecutorService getScheduler();

    public abstract void startScheduler();

    public void stopScheduler() {
        if ((getScheduler() != null) && !getScheduler().isShutdown()) {
            getScheduler().shutdown();
        }
    }
}
