import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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

    public void saveTask(Epic epic) {

        if (epicRepo.get(epic.getId()) != null) {
            System.out.println("Такой эпик уже существует!");
            return;
        }

        epic.setId(++taskCounter);
        epicRepo.put(epic.getId(), epic);
        System.out.println("epic created: " + epic);

        for (Subtask subtask : epic.getSubtasks()) {
            saveTask(subtask);
        }
    }

    public void saveTask(Subtask subtask) {

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
        epic.setStatus(calculateEpicStatus(epic));
    }

    public void updateSubtask(Subtask subtask) {
        subtaskRepo.put(subtask.getId(), subtask);

        Epic epic = subtask.getEpic();
        epic.setStatus(calculateEpicStatus(epic));
    }

    // delete
    public void removeTasks() {
        taskRepo.clear();
    }

    public void removeTaskById(int id) {
        taskRepo.remove(id);
    }

    public void removeEpics() {
        removeSubtasks();
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

    // эпики могут существовать без подзадач
    public void removeSubtasks() {
        subtaskRepo.clear();
    }

    public void removeSubtaskById(int id) {
        Subtask subtask = subtaskRepo.get(id);

        if (subtask != null) {
            Epic epic = subtask.getEpic();
            epic.removeSubtask(subtask);
            subtaskRepo.remove(id);
            // TODO: после удаление подзадачи надо обновить статус у родительского эпика
        }
    }

    private TaskStatus calculateEpicStatus(Epic epic) {

        ArrayList<Subtask> subtasks = epic.getSubtasks();

        if (subtasks.isEmpty() || hasStatusNew(subtasks)) {
            return TaskStatus.NEW;
        }

        // на пустоту уже проверили
        if (hasStatusDone(subtasks)) {
            return TaskStatus.DONE;
        }

        return TaskStatus.IN_PROGRESS;
    }

    private boolean hasStatusNew(ArrayList<Subtask> subtasks) {
        return hasStatus(subtasks, TaskStatus.NEW);
    }

    private boolean hasStatusDone(ArrayList<Subtask> subtasks) {
        return hasStatus(subtasks, TaskStatus.DONE);
    }

    private boolean hasStatus(ArrayList<Subtask> subtasks, TaskStatus status) {
        if (subtasks.isEmpty()) {
            return false;
        }

        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() != status) {
                return false;
            }
        }

        return true;
    }

}
