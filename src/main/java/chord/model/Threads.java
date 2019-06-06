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
    private static ScheduledExecutorService pool = Executors.newScheduledThreadPool(10000);

    /**
     * add to the pool a new Thread to be executed periodically, with a period set by the Utilities
     * @param runnable thread to execute
     * @return a Future to stop executing periodical operations, when the node is being cancelled
     */
    public static ScheduledFuture<?> executePeriodically(Runnable runnable){
        return pool.scheduleWithFixedDelay(runnable, Utilities.getPeriod(), Utilities.getPeriod(), TimeUnit.MILLISECONDS);
    }

    /**
     * add to the pool a new Thread to be executed with a bigger period with respect to the previous method
     * this method is called only by the socketNode, to run a thread which checks from time to time the actual use of the open channels
     * @param runnable thread to execute
     * @return a Future to stop executing periodical operations, when the SocketNode is being cancelled
     */
    public static ScheduledFuture<?> executeRarely(Runnable runnable){
        return pool.scheduleWithFixedDelay(runnable, Utilities.getPeriod()*10, Utilities.getPeriod()*10, TimeUnit.MILLISECONDS);
    }

    /**
     * to execute a Thread
     * @param runnable thread to execute
     */
    public static void executeImmediately(Runnable runnable){
        pool.submit(runnable);
    }

    /**
     * to execute a Thread after a period of Time
     * this method is called from nodeDispatcher to check, after a while, that the answer to a message has been received
     * @param runnable thread to execute
     */
    public static void executeAfterDelay(Runnable runnable){
        pool.schedule(runnable,Utilities.getTimer(), TimeUnit.MILLISECONDS);
    }


}
