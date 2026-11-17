package models;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Task {
    private final String taskId;
    private String taskTitle;
    private String taskDiscription;
    private List<User> assingee;
    private String creator;
    private TaskStatus taskStatus;

    public Task(TaskBuilder taskBuilder) {
        this.taskId = UUID.randomUUID().toString();
        this.taskTitle = taskBuilder.taskTitle;
        this.taskDiscription = taskBuilder.taskDiscription;
        this.assingee = taskBuilder.assingee;
        this.creator = taskBuilder.creator;
        this.taskStatus = TaskStatus.TODO;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public void setTaskDiscription(String taskDiscription) {
        this.taskDiscription = taskDiscription;
    }

    public void setAssingee(List<User> assingee) {
        this.assingee = assingee;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public static class TaskBuilder{
        private String taskTitle;
        private String taskDiscription;
        private List<User> assingee;
        private String creator;

        public void setTaskTitle(String taskTitle) {
            this.taskTitle = taskTitle;
        }

        public void setTaskDiscription(String taskDiscription) {
            this.taskDiscription = taskDiscription;
        }

        public void setAssingee(List<User> assingee) {
            this.assingee = assingee;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }

        public Task build(){
            return new Task(this);
        }
    }
}
