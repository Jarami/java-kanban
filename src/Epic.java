import java.util.ArrayList;

public class Epic extends Task {

    private final ArrayList<Subtask> subtasks;

    public Epic(String name, String description) {
        super(name, description);
        this.subtasks = new ArrayList<>();
    }

    public void addSubtask(Subtask subtask) {
        subtasks.add(subtask);
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void removeSubtask(Subtask subtask) {
        subtasks.remove(subtask);
    }

    public void removeSubtasks() {
        subtasks.clear();
    }

    public void update() {
        // обновляем статус
        if (subtasks.isEmpty() || areAllSubtasksNew()) {
            this.status = TaskStatus.NEW;
        } else if (areAllSubtasksDone()) {
            this.status = TaskStatus.DONE;
        } else {
            this.status = TaskStatus.IN_PROGRESS;
        }
    }

    private boolean areAllSubtasksNew() {
        return areAllSubtasksHaveStatus(TaskStatus.NEW);
    }

    private boolean areAllSubtasksDone() {
        return areAllSubtasksHaveStatus(TaskStatus.DONE);
    }

    private boolean areAllSubtasksHaveStatus(TaskStatus taskStatus) {
        if (subtasks.isEmpty()) {
            return false;
        }

        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() != taskStatus) {
                return false;
            }
        }

        return true;
    }
}
