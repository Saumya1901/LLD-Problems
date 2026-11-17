import models.Task;
import models.User;

import java.util.HashMap;
import java.util.Map;

public class TaskService {
    private final Map<String , User> usersById;
    private final Map<String , Task>tasksById;

    public TaskService() {
        this.usersById = new HashMap<>();
        this.tasksById = new HashMap<>();
    }

    public void createUser(String name){
        User user = new User(name);
        usersById.put(user.getUserId(),user);
    }
    public void createTask(String taskTitle , String taskdisc, String creatorId,String assignee){
        Task newTask = new Task.TaskBuilder().setTaskTitle(taskTitle).setTaskDiscription(taskdisc).setAssingee(assignee).setCreator(creatorId).build();
        tasksById.put(newTask.getTaskId(), newTask);
    }
    public void addAssignee(String taskId,String assignee ){
        Task task = tasksById.get(taskId);
        task.setAssingee(assignee);
        //are we mutitation the same objeccT?

    }

}
