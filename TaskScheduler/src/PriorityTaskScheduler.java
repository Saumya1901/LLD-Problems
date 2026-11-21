import java.util.concurrent.*;

enum TaskPriority {
    HIGH(3),
    MEDIUM(2),
    LOW(1);

}
enum TaskStatus {
    SCHEDULED,
    READY,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}

public class PriorityTaskScheduler {
    private ScheduledExecutorService scheduler;
    private final PriorityBlockingQueue<Task> readyQueue;
    private final ExecutorService workerService;

    PriorityTaskScheduler(int workerCount) {
        scheduler = Executors.newScheduledThreadPool(workerCount);

        readyQueue = new PriorityBlockingQueue<>(
                11,
                (a, b) -> Integer.compare(
                        b.getPriority().getValue(),
                        a.getPriority().getValue()
                )
        );
        workerService = Executors.newFixedThreadPool(workerCount);
        for (int i = 0; i < workerCount; i++)
            workerService.submit(this::workerThread);// submitting task multiple times or other works can work on it
    }

    public void scheduleTask(Task task, long scheduleTime) {
        long delayTime = Math.max(0, scheduleTime - System.currentTimeMillis());
        ScheduledFuture<?> future = scheduler.schedule(
                () -> readyQueue.offer(task),
                delayTime,
                TimeUnit.MILLISECONDS
        );

        task.setFuture(future);
    }
    public boolean cancelTask(Task task) {
        ScheduledFuture<?> future = task.getFuture();

        if (future != null) {
            future.cancel(false);
        }

        return readyQueue.remove(task);
    }
    public void workerThread() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Task task = readyQueue.take();
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("oops interrupted");
                break;
            }

        }
    }

    public void shutDown() {
        scheduler.shutdownNow();
        workerService.shutdownNow();
    }

}
