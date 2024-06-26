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

    // create
    public void saveTask(Task task) {
        task.setId(++taskCounter);
        taskRepo.put(task.getId(), task);
        System.out.println("task created: " + task);
    }

    public void saveEpic(Epic epic) {

        if (epicRepo.get(epic.getId()) != null) {
            System.out.println("Такой эпик уже существует!");
            return;
        }

        epic.setId(++taskCounter);
        epicRepo.put(epic.getId(), epic);
        System.out.println("epic created: " + epic);

        for (Subtask subtask : epic.getSubtasks()) {
            saveSubtask(subtask);
        }
    }

    public void saveSubtask(Subtask subtask) {

        if (subtaskRepo.get(subtask.getId()) != null) {
            System.out.println("Такая подзадача уже существует!");
            return;
        }

        subtask.setId(++taskCounter);
        subtaskRepo.put(subtask.getId(), subtask);
        System.out.println("subtask created: " + subtask);
    }

    // read
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

    // update
    public void updateTask(Task task) {
        taskRepo.put(task.getId(), task);
    }

    public void updateEpic(Epic epic) {
        epicRepo.put(epic.getId(), epic);
        epic.update();
    }

    // при обновлении подзадачи нужно обновить родительский эпик
    public void updateSubtask(Subtask subtask) {
        subtaskRepo.put(subtask.getId(), subtask);
        subtask.getEpic().update();
    }

    // delete
    public void removeTasks() {
        taskRepo.clear();
    }

    public void removeTaskById(int id) {
        taskRepo.remove(id);
    }

    public void removeEpics() {
        subtaskRepo.clear();
        epicRepo.clear();
    }

    // при удалении эпика все его подзадачи тоже удаляются
    public void removeEpicById(int id) {
        Epic epic = epicRepo.get(id);

        if (epic != null) {
            for (Subtask subtask : epic.getSubtasks()) {
                subtaskRepo.remove(subtask.getId());
            }
            epicRepo.remove(id);
        }
    }

    // эпики могут существовать без подзадач,
    // но все подзадачи у эпиков должны быть удалены
    public void removeSubtasks() {
        for (Map.Entry<Integer, Subtask> entry : subtaskRepo.entrySet()) {
            Subtask subtask = entry.getValue();
            Epic epic = subtask.getEpic();
            epic.removeSubtasks();
            epic.update();
        }
        subtaskRepo.clear();
    }

    // при удалении подзадачи нужно обновить родительский эпик
    public void removeSubtaskById(int id) {
        Subtask subtask = subtaskRepo.get(id);

        if (subtask != null) {
            Epic epic = subtask.getEpic();
            epic.removeSubtask(subtask);
            epic.update();
            subtaskRepo.remove(id);
        }
    }
}
