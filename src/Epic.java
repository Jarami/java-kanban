import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<SubTask> subtasks;

    public Epic(String name, String description) {
        super(name, description);
        this.subtasks = new ArrayList<>();
    }

    public void addSubTask(SubTask subtask) {
        subtasks.add(subtask);
    }
}
