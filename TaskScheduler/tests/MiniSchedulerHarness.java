import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Temporary harness; accesses package-visible scheduler internals for observation only. */
public class MiniSchedulerHarness {
    private static int passed, failed;

    private static void check(String name, Object expected, Object observed) {
        boolean ok = expected.equals(observed);
        System.out.printf("%s %s | expected=%s, observed=%s%n",
                ok ? "PASS" : "FAIL", name, expected, observed);
        if (ok) passed++; else failed++;
    }

    private static void finish(MiniScheduler scheduler, String id) throws Exception {
        scheduler.map.get(id).getFuture().get(4, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws Exception {
        MiniScheduler scheduler = new MiniScheduler(2);
        CountDownLatch release = new CountDownLatch(1);
        try {
            AtomicInteger count = new AtomicInteger();
            Runnable increment = () -> count.incrementAndGet();
            scheduler.schedule("simple", increment, 1);
            check("simple initial state", JobStatus.SCHEDULED, scheduler.map.get("simple").getStatus());
            finish(scheduler, "simple");
            check("simple executes exactly once", 1, count.get());
            check("simple terminal state", JobStatus.COMPLETED, scheduler.map.get("simple").getStatus());
            check("completed task cannot be cancelled", false, scheduler.cancel("simple"));

            scheduler.schedule("zero", () -> count.incrementAndGet(), 0);
            finish(scheduler, "zero");
            check("zero-delay execution", 2, count.get());
            check("zero-delay terminal state", JobStatus.COMPLETED, scheduler.map.get("zero").getStatus());

            CountDownLatch started = new CountDownLatch(1);
            Runnable longRunning = () -> {
                started.countDown();
                try {
                    if (!release.await(5, TimeUnit.SECONDS)) throw new RuntimeException("release timed out");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            };
            scheduler.schedule("long", longRunning, 0);
            check("long task starts", true, started.await(2, TimeUnit.SECONDS));
            check("running state", JobStatus.IN_PROGRESS, scheduler.map.get("long").getStatus());
            check("running task cancellation refused", false, scheduler.cancel("long"));
            scheduler.schedule("parallel", () -> count.incrementAndGet(), 0);
            finish(scheduler, "parallel");
            check("second worker executes while first blocked", 3, count.get());
            check("first worker remains running", JobStatus.IN_PROGRESS, scheduler.map.get("long").getStatus());
            release.countDown();
            finish(scheduler, "long");
            check("running transitions to completed", JobStatus.COMPLETED, scheduler.map.get("long").getStatus());

            AtomicInteger attempts = new AtomicInteger();
            Runnable throwing = () -> {
                attempts.incrementAndGet();
                throw new RuntimeException("deliberate harness failure");
            };
            scheduler.schedule("throws", throwing, 0);
            finish(scheduler, "throws");
            check("throwing task executed", 1, attempts.get());
            check("exception transitions to failed", JobStatus.FAILED, scheduler.map.get("throws").getStatus());
            scheduler.schedule("after-error", () -> count.incrementAndGet(), 0);
            finish(scheduler, "after-error");
            check("scheduler survives task exception", 4, count.get());

            AtomicInteger cancelledRuns = new AtomicInteger();
            scheduler.schedule("cancelled", () -> cancelledRuns.incrementAndGet(), 1);
            check("future task initial state", JobStatus.SCHEDULED, scheduler.map.get("cancelled").getStatus());
            check("cancel before execution", true, scheduler.cancel("cancelled"));
            check("cancelled state", JobStatus.CANCELLED, scheduler.map.get("cancelled").getStatus());
            check("underlying future cancelled", true, scheduler.map.get("cancelled").getFuture().isCancelled());
            check("repeat cancellation refused", false, scheduler.cancel("cancelled"));
            check("unknown task cancellation", false, scheduler.cancel("missing"));

            // Graceful shutdown should drain an already scheduled future task.
            scheduler.schedule("drain", () -> count.incrementAndGet(), 2);
            scheduler.shutdown();
            check("shutdown flag", true, scheduler.scheduler.isShutdown());
            boolean rejected = false;
            try { scheduler.schedule("late", () -> {}, 0); }
            catch (RejectedExecutionException expected) { rejected = true; }
            check("new work rejected after shutdown", true, rejected);
            check("executor terminates", true, scheduler.scheduler.awaitTermination(4, TimeUnit.SECONDS));
            check("queued work drains during shutdown", 5, count.get());
            check("drained task completed", JobStatus.COMPLETED, scheduler.map.get("drain").getStatus());
            check("cancelled task never executes past due time", 0, cancelledRuns.get());
            check("cancelled state remains terminal", JobStatus.CANCELLED, scheduler.map.get("cancelled").getStatus());
        } finally {
            release.countDown();
            scheduler.scheduler.shutdownNow();
            scheduler.scheduler.awaitTermination(3, TimeUnit.SECONDS);
        }
        System.out.printf("RESULT: %d passed, %d failed%n", passed, failed);
        if (failed != 0) System.exit(1);
    }
}
