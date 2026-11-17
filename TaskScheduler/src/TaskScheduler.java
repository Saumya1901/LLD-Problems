public interface TaskScheduler {
    void schedule(String taskId, long runAtMillis, Runnable task);
    boolean cancel(String taskId);
    void start();
    void shutdown();
}
