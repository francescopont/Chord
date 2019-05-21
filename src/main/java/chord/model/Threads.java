package chord.model;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Threads {
    private static ScheduledExecutorService pool = Executors.newScheduledThreadPool(20);

    public static ScheduledFuture<?> executePeriodically(Runnable runnable){
        return pool.scheduleWithFixedDelay(runnable, Utilities.getPeriod(), Utilities.getPeriod(), TimeUnit.MILLISECONDS);
    }

    public static void executeOnce(Runnable runnable){
        pool.submit(runnable);
    }




}
