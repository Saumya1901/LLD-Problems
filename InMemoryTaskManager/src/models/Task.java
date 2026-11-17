package models;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Task {
    private final String taskId;
    private final String taskTitle;
    private final String taskDiscription;
    private String assingee;
    private final String creator;
    private final TaskStatus taskStatus;

    private Task(TaskBuilder taskBuilder) {
        this.taskId = UUID.randomUUID().toString();
        this.taskTitle = taskBuilder.taskTitle;
        this.taskDiscription = taskBuilder.taskDiscription;
        this.assingee = taskBuilder.assingee;
        this.creator = taskBuilder.creator;
        this.taskStatus = TaskStatus.TODO;
    }


    public void setAssingee(String assingee) {
        this.assingee = assingee;
    }


    public String getTaskId() {
        return taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public String getTaskDiscription() {
        return taskDiscription;
    }

    public List<User> getAssingee() {
        return assingee;
    }

    public String getCreator() {
        return creator;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public static class TaskBuilder {
        private String taskTitle;
        private String taskDiscription;
        private String assingee;
        private String creator;

        public TaskBuilder setTaskTitle(String taskTitle) {
            this.taskTitle = taskTitle;
            return this;
        }

        public TaskBuilder setTaskDiscription(String taskDiscription) {
            this.taskDiscription = taskDiscription;
            return this;
        }

        public TaskBuilder setAssingee(String assingee) {
            this.assingee = assingee;
            return this;
        }

        public TaskBuilder setCreator(String creator) {
            this.creator = creator;
            return this;
        }

        public Task build() {
            return new Task(this);
        }
    }
}
