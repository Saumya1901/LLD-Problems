import java.util.concurrent.ScheduledFuture;

public class Task implements   Runnable{
    private final TaskPriority priority;
    private volatile ScheduledFuture<?> future;

    public void setFuture(ScheduledFuture<?> future) {
        this.future = future;
    }
    public Task(TaskPriority priority) {
        this.priority = priority;
    }

    public ScheduledFuture<?> getFuture() {
        return future;
    }

    @Override
    public void run() {
        System.out.println("running runable task ");
    }

    public TaskPriority getPriority() {
        return priority;
    }
}
