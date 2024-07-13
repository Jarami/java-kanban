package managers;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private static int taskCounter;

    private final HashMap<Integer, Task> taskRepo;
    private final HashMap<Integer, Epic> epicRepo;
    private final HashMap<Integer, Subtask> subtaskRepo;
    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        taskRepo = new HashMap<>();
        epicRepo = new HashMap<>();
        subtaskRepo = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
    }

    private synchronized int generateTaskId() {
        return ++taskCounter;
    }

    // Сохранение
    public int saveTask(Task task) {

        int id = generateTaskId();

        task.setId(id);
        taskRepo.put(id, task);

        System.out.println("task created: " + task);

        return id;
    }

    public int saveEpic(Epic epic) {

        int id = generateTaskId();

        epic.setId(id);
        epicRepo.put(id, epic);

        System.out.println("epic created: " + epic);

        return id;
    }

    public int saveSubtask(Subtask subtask) {

        int id = generateTaskId();

        Epic epic = getEpicOfSubtask(subtask);
        if (epic != null) {

            subtask.setId(id);
            subtaskRepo.put(id, subtask);

            // добавляем id подзадачи эпику
            epic.addSubtaskIdIfAbsent(subtask);
            updateEpicStatus(epic);

            System.out.println("subtask created: " + subtask);

            return id;
        }

        return -1;
    }

    // Получение
    public List<Task> getTasks() {
        return List.copyOf(taskRepo.values());
    }

    public Task getTaskById(int id) {
        Task task = taskRepo.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    public List<Epic> getEpics() {
        return List.copyOf(epicRepo.values());
    }

    public Epic getEpicById(int id) {
        Epic epic = epicRepo.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    public List<Subtask> getSubtasks() {
        return List.copyOf(subtaskRepo.values());
    }

    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtaskRepo.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    public List<Subtask> getSubtasksOfEpic(Epic epic) {
        List<Subtask> subtasks = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtasksId()) {
            subtasks.add(subtaskRepo.get(subtaskId));
        }
        return subtasks;
    }

    public Epic getEpicOfSubtask(Subtask subtask) {
        return epicRepo.get(subtask.getEpicId());
    }

    // Обновление
    public void updateTask(Task task) {
        if (task.getId() == null || taskRepo.get(task.getId()) == null) {
            System.out.println("Обновить можно только ранее сохраненную задачу");
            return;
        }

        taskRepo.put(task.getId(), task);
    }

    public void updateEpic(Epic epic) {
        if (epic.getId() == null || epicRepo.get(epic.getId()) == null) {
            System.out.println("Обновить можно только ранее сохраненный эпик");
            return;
        }
        epicRepo.put(epic.getId(), epic);
        updateEpicStatus(epic);
    }

    // При обновлении подзадачи нужно обновить родительский эпик
    public void updateSubtask(Subtask subtask) {

        if (subtask.getId() == null){
            System.out.println("Изменить можно только сохраненную подзадачу");
            return;
        }

        Subtask oldSubtask = subtaskRepo.get(subtask.getId());
        if (oldSubtask == null) {
            System.out.println("Изменить можно только существующую подзадачу");
            return;
        }

        Epic epic = getEpicOfSubtask(subtask);
        Epic oldEpic = getEpicOfSubtask(oldSubtask);

        if (!oldEpic.equals(epic)) {
            System.out.println("Подзадача не может изменить свой эпик! Предыдущий эпик " + oldEpic +
                    ", новый " + epic);
            return;
        }

        subtaskRepo.put(subtask.getId(), subtask);
        updateEpicStatus(epic);
    }

    // Удаление
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

    // При удалении эпика все его подзадачи тоже удаляются
    public void removeEpicById(int id) {
        Epic epic = epicRepo.get(id);

        if (epic != null) {
            for (Integer subtaskId : epic.getSubtasksId()) {
                subtaskRepo.remove(subtaskId);
            }
            epicRepo.remove(id);
        }
    }

    // При удалении подзадач из хранилища также нужно удалить их у эпиков
    public void removeSubtasks() {

        subtaskRepo.clear();

        for (Map.Entry<Integer, Epic> entry : epicRepo.entrySet()) {
            Epic epic = entry.getValue();
            epic.removeSubtasks();
            updateEpicStatus(epic);
        }
    }

    // При удалении подзадачи нужно обновить родительский эпик
    public void removeSubtaskById(int id) {
        Subtask subtask = subtaskRepo.get(id);

        if (subtask != null) {
            Epic epic = getEpicOfSubtask(subtask);
            if (epic != null) {
                subtaskRepo.remove(id);
                epic.removeSubtask(subtask);
                updateEpicStatus(epic);
            }
        }
    }

    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void updateEpicStatus(Epic epic) {
        // обновляем статус
        List<Subtask> subtasks = getSubtasksOfEpic(epic);

        if (subtasks.isEmpty() || areAllSubtasksNew(subtasks)) {
            epic.setStatus(TaskStatus.NEW);

        } else if (areAllSubtasksDone(subtasks)) {
            epic.setStatus(TaskStatus.DONE);

        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    private boolean areAllSubtasksNew(List<Subtask> subtasks) {
        return areAllSubtasksHaveStatus(subtasks, TaskStatus.NEW);
    }

    private boolean areAllSubtasksDone(List<Subtask> subtasks) {
        return areAllSubtasksHaveStatus(subtasks, TaskStatus.DONE);
    }

    private boolean areAllSubtasksHaveStatus(List<Subtask> subtasks, TaskStatus taskStatus) {
        if (subtasks.isEmpty()) {
            return false;
        }

        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() != taskStatus) {
                return false;
            }
        }

        return true;
    }
}
