package chord.model;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Class which manage the creation and the execution of the application threads
 */
public class Threads {

    private Threads(){}
    private static ScheduledExecutorService pool = Executors.newScheduledThreadPool(100000);

    /**
     * Create a pool of threads to execute periodically on the node
     * @param runnable thread to execute
     * @return a pool of threads
     */
    public static ScheduledFuture<?> executePeriodically(Runnable runnable){
        return pool.scheduleWithFixedDelay(runnable, Utilities.getPeriod(), Utilities.getPeriod(), TimeUnit.MILLISECONDS);
    }

    /**
     * Create a pool of threads to execute on the node
     * @param runnable thread to execute
     * @return a pool of threads
     */
    public static ScheduledFuture<?> executeRarely(Runnable runnable){
        return pool.scheduleWithFixedDelay(runnable, Utilities.getPeriod()*10, Utilities.getPeriod()*10, TimeUnit.MILLISECONDS);
    }

    //free threads
    public static void executeImmediately(Runnable runnable){
        pool.submit(runnable);
    }

    //timers for node dispatcher
    public static void executeAfterDelay(Runnable runnable){
        pool.schedule(runnable,Utilities.getTimer(), TimeUnit.MILLISECONDS);
    }


}
