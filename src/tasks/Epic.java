package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final List<Integer> subtasksId;
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        this(null, name, description, TaskStatus.NEW, null, null);
    }

    public Epic(String name, String description, LocalDateTime startTime, Duration duration) {
        this(null, name, description, TaskStatus.NEW, startTime, duration);
    }

    public Epic(Integer id, String name, String description) {
        this(id, name, description, TaskStatus.NEW, null, null);
    }

    public Epic(Integer id, String name, String description, TaskStatus status) {
        super(id, name, description, status, null, null);
        this.subtasksId = new ArrayList<>();
    }

    public Epic(Integer id, String name, String description, TaskStatus status, LocalDateTime startTime,
                Duration duration) {
        super(id, name, description, status, startTime, duration);
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

    @Override
    public String toString() {
        return "tasks.Epic{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", start=" + startTime +
                ", duration=" + duration +
                '}';
    }
}
