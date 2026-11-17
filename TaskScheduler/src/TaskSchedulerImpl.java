import java.util.Map;
import java.util.concurrent.*;

public class TaskSchedulerImpl implements TaskScheduler{
    Map<String , ScheduledFuture<?>> map = new ConcurrentHashMap<>();
    ScheduledExecutorService scheduler =  Executors.newScheduledThreadPool(10);
    @Override
    public void schedule(String taskId, long runAtMillis, Runnable task) {
        long delay = runAtMillis-System.currentTimeMillis();
        map.compute(taskId , (key , existingFuture) -> {
            if(existingFuture == null || existingFuture.isDone())
                return scheduler.schedule(task,delay, TimeUnit.MILLISECONDS);
        return existingFuture;


        } );
    }

    @Override
    public boolean cancel(String taskId) {
        ScheduledFuture<?> task = map.get(taskId);
        if(task != null) {return task.cancel(false);}
        return false;
    }

    @Override
    public void start() {


    }

    @Override
    public void shutdown() {
        scheduler.shutdown();
    }
}
