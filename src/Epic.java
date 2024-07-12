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

    public void addSubtaskIdIfAbsent(Integer subtaskId) {
        if (!subtasksId.contains(subtaskId)) {
            subtasksId.add(subtaskId);
        }
    }

    public List<Integer> getSubtasksId() {
        return subtasksId;
    }

    public void removeSubtaskId(Integer subtaskId) {
        subtasksId.remove(subtaskId);
    }

    public void removeSubtasksId() {
        subtasksId.clear();
    }
}
