import java.util.concurrent.ScheduledFuture;

public class Job {
    private JobStatus status;
    private String taskId;
    private ScheduledFuture<?> future;
    private final Runnable task;
    private final Object schedulingLock = new Object();

    public Job(String taskId  , Runnable task) {
        this.taskId = taskId;
        this.task = task;
        this.status = JobStatus.SCHEDULED;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public ScheduledFuture<?> getFuture() {
        return future;
    }

    public void setFuture(ScheduledFuture<?> future) {
        synchronized (schedulingLock) {
            this.future = future;

            if (status == JobStatus.CANCELLED) {
                future.cancel(false);
            }
        }
    }


    public Runnable exectue(){
        try {
            if(!updateState(JobStatus.IN_PROGRESS))return null;

                task.run();
                updateState(JobStatus.COMPLETED);



        }catch (Exception e){
            updateState(JobStatus.FAILED);
        }

        return null;
    }

    public boolean cancel() {
        synchronized (schedulingLock) {
            if (!updateState(JobStatus.CANCELLED)) {
                return false;
            }

            if (future != null) {
                future.cancel(false);
            }

            return true;
        }
    }
    private boolean checkValidity(JobStatus oldJobStatus , JobStatus newJobStatus){
        if(newJobStatus == JobStatus.CANCELLED ) {
            if (oldJobStatus == JobStatus.SCHEDULED) return true;
        }
        if(newJobStatus == JobStatus.IN_PROGRESS)
        {
            if (oldJobStatus == JobStatus.SCHEDULED) return true;
        }
        if(newJobStatus == JobStatus.COMPLETED)
        {
            if (oldJobStatus == JobStatus.IN_PROGRESS) return true;
        }        return false;
    }
    public boolean updateState(JobStatus newJobStatus){
        synchronized (schedulingLock){
            if(checkValidity(status,newJobStatus)){
                status = newJobStatus;
                return true;
            }
            return false;
        }

    }

}
