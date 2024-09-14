package managers;

import exceptions.ManagerSaveException;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import util.CSVFormat;
import util.Tasks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileBackedTaskManager extends InMemoryTaskManager {

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

        Tasks.createAndSaveTask(manager, "task1", "desc1", null, null);
        Tasks.createAndSaveTask(manager, "task2", "desc2", null, null);
        Tasks.createAndSaveEpicWithSubs(manager, "epic1", "epic desc1", 0);
        Tasks.createAndSaveEpicWithSubs(manager, "epic2", "epic desc2", 3);

        TaskManager managerFromFile = loadFromFile(taskFile);

        Tasks.printTasks(managerFromFile);
    }

    public static FileBackedTaskManager loadFromFile(Path path) throws IOException {

        FileBackedTaskManager manager = new FileBackedTaskManager(path);

        CSVFormat.loadTasksFromFile(path).forEach(task -> {
            if (task instanceof Epic) {
                manager.saveEpic((Epic)task);
            } else if (task instanceof Subtask) {
                manager.saveSubtask((Subtask)task);
            } else {
                manager.saveTask(task);
            }
        });

        return manager;
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

        try (CSVFormat.TaskFileWriter writer = CSVFormat.writer(taskFile)) {

            getTasks().forEach(writer::println);
            getEpics().forEach(writer::println);
            getSubtasks().forEach(writer::println);

        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить задачи", e);
        }
    }
}
