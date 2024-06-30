public class Subtask extends Task {

    private final Integer epicId;

    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.epicId = epic.getId();
    }

    public Subtask(Integer id, String name, String description, Epic epic) {
        super(id, name, description);
        this.epicId = epic.getId();
    }

    public Subtask(String name, String description, TaskStatus status, Epic epic) {
        super(name, description, status);
        this.epicId = epic.getId();
    }

    public Subtask(Integer id, String name, String description, TaskStatus status, Epic epic) {
        super(id, name, description, status);
        this.epicId = epic.getId();
    }

    public Integer getEpicId() {
        return epicId;
    }
}
