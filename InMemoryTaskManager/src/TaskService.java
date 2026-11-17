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
    public void createTask(){
        //user builder pattern to create task
    }

}
