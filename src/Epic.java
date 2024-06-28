import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<Subtask> subtasks;

    public Epic(String name, String description) {
        super(name, description);
        this.subtasks = new ArrayList<>();
    }

    public Epic(Integer id, String name, String description) {
        super(id, name, description);
        this.subtasks = new ArrayList<>();
    }

    public void addSubtask(Subtask subtask) {
        if (!subtasks.contains(subtask)) {
            subtasks.add(subtask);
            update();
        }
    }

    public void replaceSubtask(Subtask subtask) {
        if (subtask == null || subtask.getId() == null) {
            return;
        }

        int id = subtask.getId();

        for (int i = 0; i < subtasks.size(); i++) {
            if (subtasks.get(i) != null && subtasks.get(i).getId() == id) {
                subtasks.set(i, subtask);
                update();
                return;
            }
        }

        System.out.println("Не удалось заменить подзадачу по id = " + id);
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void removeSubtask(Subtask subtask) {
        subtasks.remove(subtask);
        update();
    }

    public void removeSubtasks() {
        subtasks.clear();
        update();
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
