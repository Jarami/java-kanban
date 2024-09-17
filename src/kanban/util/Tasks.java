package kanban.util;

import kanban.managers.TaskManager;
import kanban.tasks.Epic;
import kanban.tasks.Subtask;
import kanban.tasks.Task;
import kanban.tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Tasks {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Tasks() {

    }

    public static Task createAndSaveTask(TaskManager manager, String name, String desc, LocalDateTime startTime,
                                         Duration duration) {

        Task task = new Task(name, desc, startTime, duration);
        manager.saveTask(task);
        return task;
    }

    // name;description;status;startTime;duration
    public static Task createTask(String formattedTask) {
        String[] chunks = formattedTask.split(";");
        return new Task(
                chunks[0], // name
                chunks[1], // description
                TaskStatus.valueOf(chunks[2]), // status
                parseTime(chunks[3]), // startTime
                parseDuration(chunks[4]) // duration
        );
    }

    public static Epic createAndSaveEpic(TaskManager manager, String name, String desc) {
        Epic epic = new Epic(name, desc);
        manager.saveEpic(epic);
        return epic;
    }

    public static Epic createEpic(String formattedEpic) {
        String[] chunks = formattedEpic.split(";");
        return new Epic(
                chunks[0], // name
                chunks[1] // description
        );
    }

    public static Subtask createAndSaveSubtask(TaskManager manager, String name, String desc, Epic epic,
                                               LocalDateTime startTime, Duration duration) {
        Subtask sub = new Subtask(name, desc, epic, startTime, duration);
        manager.saveSubtask(sub);
        return sub;
    }

    // name;description;status;epicId;startTime;duration
    public static Subtask createSubtask(String formattedSubtask) {
        String[] chunks = formattedSubtask.split(";");
        return new Subtask(
                null, // id
                chunks[0], // name
                chunks[1], // description
                TaskStatus.valueOf(chunks[2]), // status
                Integer.parseInt(chunks[3]), // epicId
                parseTime(chunks[4]), // startTime
                parseDuration(chunks[5]) // duration
        );
    }

    public static void createAndSaveEpicWithSubs(TaskManager manager, String name, String desc, int subtaskCount) {
        Epic epic = createAndSaveEpic(manager, name, desc);

        for (int i = 0; i < subtaskCount; i++) {
            createAndSaveSubtask(manager, "sub " + i + " of " + name, "sub " + i + " desc of " + name,
                    epic, null, null);
        }
    }

    public static void printTasks(TaskManager manager) {

        System.out.println("manager: " + manager);

        System.out.println("  tasks:");
        manager.getTasks()
                .forEach(task -> System.out.println("    " + task));

        System.out.println("  epics:");
        manager.getEpics().forEach(epic -> {
            System.out.println("    " + epic);
            manager.getSubtasksOfEpic(epic).forEach(sub ->
                System.out.println("      " + sub));
        });
    }

    public static Task copy(Task task) {
        return new Task(task.getId(), task.getName(), task.getDescription(), task.getStatus(), task.getStartTime(),
                task.getDuration());
    }

    public static Epic copy(Epic epic) {
        Epic newEpic = new Epic(epic.getId(), epic.getName(), epic.getDescription(), epic.getStatus(), epic.getStartTime(),
                epic.getDuration());

        epic.getSubtasksId().forEach(newEpic::addSubtaskIdIfAbsent);

        return newEpic;
    }

    public static Subtask copy(Subtask sub) {
        return new Subtask(sub.getId(), sub.getName(), sub.getDescription(), sub.getStatus(), sub.getEpicId(),
                sub.getStartTime(), sub.getDuration());
    }

    public static LocalDateTime parseTime(String formattedTime) {
        return formattedTime.equals("null") ? null : LocalDateTime.parse(formattedTime, DATE_TIME_FORMATTER);
    }

    public static Duration parseDuration(String formattedDuration) {
        return formattedDuration.equals("null") ? null : Duration.ofMinutes(Long.parseLong(formattedDuration));
    }
}
