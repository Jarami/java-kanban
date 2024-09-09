package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static tasks.TaskStatus.*;

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

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void addSubtaskIdIfAbsent(Subtask subtask) {
        if (!subtasksId.contains(subtask.getId())) {
            subtasksId.add(subtask.getId());
        }
    }

    public void addSubtaskIdIfAbsent(Integer subtaskId) {
        if (!subtasksId.contains(subtaskId)) {
            subtasksId.add(subtaskId);
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

    public void update(List<Subtask> subtasks) {
        status = calculateStatus(subtasks);
        duration = calculateDuration(subtasks);
        startTime = calculateStartTime(subtasks);
        endTime = calculateEndTime(subtasks);
    }

    private TaskStatus calculateStatus(List<Subtask> subtasks) {
        boolean areAllSubsNew = true;
        boolean areAllSubsDone = true;

        for (Subtask sub : subtasks) {
            if (!sub.getStatus().equals(NEW)) areAllSubsNew = false;
            if (!sub.getStatus().equals(DONE)) areAllSubsDone = false;
        }

        if (areAllSubsNew) return NEW;
        if (areAllSubsDone) return DONE;
        return IN_PROGRESS;
    }

    private Duration calculateDuration(List<Subtask> subtasks) {

         Duration totalDuration = subtasks.stream()
                .map(Task::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

         if (totalDuration.equals(Duration.ZERO)) {
             return null;
         } else {
             return totalDuration;
         }
    }

    private LocalDateTime calculateStartTime(List<Subtask> subtasks) {

        return subtasks.stream()
                .map(Task::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    private LocalDateTime calculateEndTime(List<Subtask> subtasks) {

        return subtasks.stream()
                .filter(s -> s.getStartTime() != null && s.getDuration() != null)
                .map(s -> s.getStartTime().plus(s.getDuration()))
                .max(LocalDateTime::compareTo)
                .orElse(null);
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
