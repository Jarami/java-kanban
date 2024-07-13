package tasks;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final List<Integer> subtasksId;

    public Epic(String name, String description) {
        this(null, name, description);
    }

    public Epic(Integer id, String name, String description) {
        super(id, name, description);
        this.subtasksId = new ArrayList<>();
    }

    public void addSubtaskIdIfAbsent(Subtask subtask) {
        if (!subtasksId.contains(subtask.getId())) {
            subtasksId.add(subtask.getId());
        }
    }

    public List<Integer> getSubtasksId() {
        return List.copyOf(subtasksId);
    }

    public void removeSubtask(Subtask subtask) {
        subtasksId.remove(subtask.getId());
    }

    public void removeSubtasks() {
        subtasksId.clear();
    }
}
