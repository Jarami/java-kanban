package managers;

import exceptions.ManagerSaveException;
import tasks.*;
import util.Tasks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static tasks.TaskType.*;

public class FileBackedTaskManager extends InMemoryTaskManager {

    public static final String SEPARATOR = "\t";
    private final Path taskFile;

    public FileBackedTaskManager(Path taskFile) {
        super();
        this.taskFile = taskFile;
    }

    public static void main(String[] args) throws IOException {

        System.out.println("Поехали!");

        Path taskFile = Files.createTempFile("tasks", ".csv");
        System.out.println("Сохраняем задачи в " + taskFile.toAbsolutePath());

        TaskManager manager = new FileBackedTaskManager(taskFile);

        Tasks.createAndSaveTask(manager, "task1", "desc1");
        Tasks.createAndSaveTask(manager, "task2", "desc2");
        Tasks.createAndSaveEpicWithSubs(manager, "epic1", "epic desc1", 0);
        Tasks.createAndSaveEpicWithSubs(manager, "epic2", "epic desc2", 3);

        TaskManager managerFromFile = loadFromFile(taskFile);

        Tasks.printTasks(managerFromFile);
    }

    public static FileBackedTaskManager loadFromFile(Path path) throws IOException {

        FileBackedTaskManager manager = new FileBackedTaskManager(path);

        for (Task task : loadTasksFromFile(path)) {
            if (task instanceof Epic) {
                manager.saveEpic((Epic)task);
            } else if (task instanceof Subtask) {
                manager.saveSubtask((Subtask)task);
            } else {
                manager.saveTask(task);
            }
        }

        return manager;
    }

    private static List<Task> loadTasksFromFile(Path path) throws IOException {

        return Files.readAllLines(path, StandardCharsets.UTF_8)
                .stream()
                .skip(1)
                .map(FileBackedTaskManager::fromString)
                .toList();
    }

    private static Task fromString(String line) {
        String[] chunks = line.split(SEPARATOR);
        int id = Integer.parseInt(chunks[0]);
        TaskType type = TaskType.valueOf(chunks[1]);
        String name = chunks[2];
        TaskStatus status = TaskStatus.valueOf(chunks[3]);
        String desc = chunks[4];

        return switch (type) {
            case TASK -> new Task(id, name, desc, status);
            case EPIC -> new Epic(id, name, desc);
            case SUBTASK -> new Subtask(id, name, desc, status, Integer.parseInt(chunks[5]));
            default -> throw new ManagerSaveException("Неизвестный тип задачи " + type);
        };
    }

    @Override
    public int saveTask(Task task) {
        int id = super.saveTask(task);
        save();
        return id;
    }

    @Override
    public int saveEpic(Epic epic) {
        int id = super.saveEpic(epic);
        save();
        return id;
    }

    @Override
    public int saveSubtask(Subtask subtask) {
        int id = super.saveSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void removeTasks() {
        super.removeTasks();
        save();
    }

    @Override
    public void removeTaskById(int id) {
        super.removeTaskById(id);
        save();
    }

    @Override
    public void removeEpics() {
        super.removeEpics();
        save();
    }

    @Override
    public void removeEpicById(int id) {
        super.removeEpicById(id);
        save();
    }

    @Override
    public void removeSubtasks() {
        super.removeSubtasks();
        save();
    }

    @Override
    public void removeSubtaskById(int id) {
        super.removeSubtaskById(id);
        save();
    }

    private void save() {

        try (TaskFileWriter writer = new TaskFileWriter(taskFile)) {

            writer.println(getHeader());

            for (Task task : getTasks()) {
                writer.println(taskToString(task));
            }
            for (Epic epic : getEpics()) {
                writer.println(taskToString(epic));
            }
            for (Subtask subtask : getSubtasks()) {
                writer.println(taskToString(subtask));
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить задачи", e);
        }
    }

    private String getHeader() {
        return join("id", "type", "name", "status", "description", "epic");
    }

    private String taskToString(Task task) {
        return join(task.getId(), TASK, task.getName(), task.getStatus(), task.getDescription());
    }

    private String taskToString(Epic epic) {
        return join(epic.getId(), EPIC, epic.getName(), epic.getStatus(), epic.getDescription());
    }

    private String taskToString(Subtask subtask) {
        return join(subtask.getId(), SUBTASK, subtask.getName(), subtask.getStatus(), subtask.getDescription(),
                subtask.getEpicId());
    }

    private String join(Object... objects) {
        return Arrays.stream(objects)
                .map(String::valueOf)
                .collect(Collectors.joining(SEPARATOR));
    }
}
