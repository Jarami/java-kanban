import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TaskManager {

    private int taskCounter;

    private final HashMap<Integer, Task> taskRepo;
    private final HashMap<Integer, Epic> epicRepo;
    private final HashMap<Integer, Subtask> subtaskRepo;

    public TaskManager() {
        taskCounter = 0;
        taskRepo = new HashMap<>();
        epicRepo = new HashMap<>();
        subtaskRepo = new HashMap<>();
    }

    private int generateTaskId() {
        return ++taskCounter;
    }

    // Сохранение
    public void saveTask(Task task) {

        if (taskRepo.get(task.getId()) != null) {
            System.out.println("Такая задача уже существует!");
            return;
        }

        task.setId(generateTaskId());
        taskRepo.put(task.getId(), task);
        System.out.println("task created: " + task);
    }

    // Подумать: нужно ли проверять, что статус у эпика соответствует статусам подзадач?
    // Подумать: можно ли создать эпик/подзадачу сразу со статусом IN_PROGRESS/DONE?
    public void saveEpic(Epic epic) {

        if (epicRepo.get(epic.getId()) != null) {
            System.out.println("Такой эпик уже существует!");
            return;
        }

        epic.setId(generateTaskId());
        epicRepo.put(epic.getId(), epic);
        System.out.println("epic created: " + epic);

        for (Subtask subtask : epic.getSubtasks()) {
            saveSubtask(subtask);
        }
    }

    // Подумать: нужен ли публичный метод создания подзадачи, как будто ее можно создать без эпика?
    public void saveSubtask(Subtask subtask) {

        if (subtaskRepo.get(subtask.getId()) != null) {
            System.out.println("Такая подзадача уже существует!");
            return;
        }

        subtask.setId(generateTaskId());
        subtaskRepo.put(subtask.getId(), subtask);
        System.out.println("subtask created: " + subtask);
    }

    // Получение
    public Collection<Task> getTasks() {
        return taskRepo.values();
    }

    public Task getTaskById(int id) {
        return taskRepo.get(id);
    }

    public Collection<Epic> getEpics() {
        return epicRepo.values();
    }

    public Epic getEpicById(int id) {
        return epicRepo.get(id);
    }

    public Collection<Subtask> getSubtasks() {
        return subtaskRepo.values();
    }

    public Subtask getSubtaskById(int id) {
        return subtaskRepo.get(id);
    }

    public ArrayList<Subtask> getSubtasksByEpic(Epic epic) {
        return epic.getSubtasks();
    }

    // Обновление
    public void updateTask(Task task) {
        if (task.getId() == null) {
            System.out.println("Обновить можно только ранее сохраненную задачу");
            return;
        }
        taskRepo.put(task.getId(), task);
    }

    // Подумать: нужно ли проверять, что статус у эпика соответствует статусам подзадач?
    public void updateEpic(Epic epic) {
        if (epic.getId() == null) {
            System.out.println("Обновить можно только ранее сохраненный эпик");
            return;
        }
        epicRepo.put(epic.getId(), epic);
    }

    // При обновлении подзадачи нужно обновить родительский эпик
    public void updateSubtask(Subtask subtask) {
        subtaskRepo.put(subtask.getId(), subtask);
        subtask.getEpic().replaceSubtask(subtask);
    }

    // Удаление
    // todo: проверка удаления всех задач
    public void removeTasks() {
        taskRepo.clear();
    }

    // todo: проверка удаления задачи по id (другие задачи не должны быть удалены)
    public void removeTaskById(int id) {
        taskRepo.remove(id);
    }

    // todo: проверка удаления всех эпиков (все подзадачи тоже должны быть удалены)
    public void removeEpics() {
        subtaskRepo.clear();
        epicRepo.clear();
    }

    // При удалении эпика все его подзадачи тоже удаляются
    // todo: проверка удаления эпика и всех его подзадач (другие эпики и подзадачи должны остаться)
    public void removeEpicById(int id) {
        Epic epic = epicRepo.get(id);

        if (epic != null) {
            for (Subtask subtask : epic.getSubtasks()) {
                subtaskRepo.remove(subtask.getId());
            }
            epicRepo.remove(id);
        }
    }

    // При удалении подзадач из хранилища также нужно удалить их у эпиков
    // todo: проверка удаления всех подзадач (у эпиков подзадачи тоже должны быть удалены)
    public void removeSubtasks() {
        for (Map.Entry<Integer, Epic> entry : epicRepo.entrySet()) {
            Epic epic = entry.getValue();
            epic.removeSubtasks();
        }
        subtaskRepo.clear();
    }

    // При удалении подзадачи нужно обновить родительский эпик
    // todo: проверка удаления подзадачи (у эпика она тоже должна быть удалена)
    // todo: проверка обновления статуса эпика после удаления подзадачи
    public void removeSubtaskById(int id) {
        Subtask subtask = subtaskRepo.get(id);

        if (subtask != null) {
            Epic epic = subtask.getEpic();
            epic.removeSubtask(subtask);
            subtaskRepo.remove(id);
        }
    }
}
