package kanban.tasks;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {

    private final Integer epicId;

    public Subtask(String name, String description, Epic epic, LocalDateTime startTime, Duration duration) {
        this(null, name, description, TaskStatus.NEW, epic, startTime, duration);
    }

    public Subtask(Integer id, String name, String description, Epic epic, LocalDateTime startTime, Duration duration) {
        this(id, name, description, TaskStatus.NEW, epic, startTime, duration);
    }

    public Subtask(String name, String description, TaskStatus status, Epic epic, LocalDateTime startTime,
                   Duration duration) {

        this(null, name, description, status, epic, startTime, duration);
    }

    public Subtask(Integer id, String name, String description, TaskStatus status, Epic epic, LocalDateTime startTime,
                   Duration duration) {

        super(id, name, description, status, startTime, duration);
        this.epicId = epic.getId();
    }

    public Subtask(Integer id, String name, String description, TaskStatus status, int epicId, LocalDateTime startTime,
                   Duration duration) {

        super(id, name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public Integer getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "tasks.Subtask{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", epicId=" + epicId +
                ", start=" + startTime +
                ", duration=" + duration +
                '}';
    }
}
