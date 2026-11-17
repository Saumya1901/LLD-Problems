import java.util.concurrent.*;

public class MiniScheduler {
    private final int workerCount;
    ScheduledExecutorService scheduler;
    ConcurrentHashMap<String, Job> map = new ConcurrentHashMap<>();

    public MiniScheduler(int workerCount) {
        this.workerCount = workerCount;
        scheduler = Executors.newScheduledThreadPool(this.workerCount);
    }

    public void schedule(
            String taskId,
            Runnable task,
            long delaySeconds
    ) {
        if (scheduler.isShutdown()) {
            throw new RejectedExecutionException(
                    "Scheduler is shutting down"
            );
        }
        Job newJob  = new Job(taskId, task);
        Job existingJob = map.putIfAbsent(taskId, newJob);
        if(existingJob == null){
            try{
                newJob.setFuture(scheduler.schedule(newJob::exectue, delaySeconds, TimeUnit.SECONDS));

            }
            catch (RejectedExecutionException e){
                map.remove(taskId,newJob);
                throw  e;
            }
        }
        //job with the task id already exists


    }

    public boolean cancel(String taskId) {
        if(map.containsKey(taskId)){
            Job job = map.get(taskId);

            return job.cancel();
        }
        return false;
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}