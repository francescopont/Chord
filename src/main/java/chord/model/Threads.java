package chord.model;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Threads {

    //don't let anyone to instantiate this class
    private Threads(){

    }
    private static ScheduledExecutorService pool = Executors.newScheduledThreadPool(1000);

    //periodic functions on nodes
    public static ScheduledFuture<?> executePeriodically(Runnable runnable){
        return pool.scheduleWithFixedDelay(runnable, Utilities.getPeriod(), Utilities.getPeriod(), TimeUnit.MILLISECONDS);
    }

    //threads a caso
    public static void executeImmediately(Runnable runnable){
        pool.submit(runnable);
    }

    //timer per i messaggi
    public static void executeAfterDelay(Runnable runnable){
        pool.schedule(runnable,Utilities.getTimer(), TimeUnit.MILLISECONDS);
    }




}
