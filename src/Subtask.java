public class Subtask extends Task {

    private final Integer epicId;

    public Subtask(String name, String description, Epic epic) {
        this(null, name, description, TaskStatus.NEW, epic);
    }

    public Subtask(Integer id, String name, String description, Epic epic) {
        this(id, name, description, TaskStatus.NEW, epic);
    }

    public Subtask(String name, String description, TaskStatus status, Epic epic) {
        this(null, name, description, status, epic);
    }

    public Subtask(Integer id, String name, String description, TaskStatus status, Epic epic) {
        super(id, name, description, status);
        this.epicId = epic.getId();
    }

    public Integer getEpicId() {
        return epicId;
    }
}
