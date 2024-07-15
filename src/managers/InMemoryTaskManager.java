package managers;

import repo.InMemoryRepo;
import repo.TaskRepo;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.util.List;
import java.util.ArrayList;

public class InMemoryTaskManager implements TaskManager {

    private static int taskCounter;

    private final TaskRepo<Task> taskRepo;
    private final TaskRepo<Epic> epicRepo;
    private final TaskRepo<Subtask> subtaskRepo;
    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        taskRepo = new InMemoryRepo<>();
        epicRepo = new InMemoryRepo<>();
        subtaskRepo = new InMemoryRepo<>();
        historyManager = Managers.getDefaultHistory();
    }

    private synchronized int generateTaskId() {
        return ++taskCounter;
    }

    // Сохранение
    @Override
    public int saveTask(Task task) {

        int id = generateTaskId();

        task.setId(id);
        taskRepo.save(task);

        System.out.println("task created: " + task);

        return id;
    }

    @Override
    public int saveEpic(Epic epic) {

        int id = generateTaskId();

        epic.setId(id);
        epicRepo.save(epic);

        System.out.println("epic created: " + epic);

        return id;
    }

    @Override
    public int saveSubtask(Subtask subtask) {

        int id = generateTaskId();

        Epic epic = getEpicOfSubtask(subtask);
        if (epic != null) {

            subtask.setId(id);
            subtaskRepo.save(subtask);

            // добавляем id подзадачи эпику
            epic.addSubtaskIdIfAbsent(subtask);
            updateEpicStatus(epic);

            System.out.println("subtask created: " + subtask);

            return id;
        }

        return -1;
    }

    // Получение
    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(taskRepo.findAll());
    }

    @Override
    public Task getTaskById(int id) {
        Task task = taskRepo.findById(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epicRepo.findAll());
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epicRepo.findById(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtaskRepo.findAll());
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtaskRepo.findById(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(Epic epic) {
        List<Subtask> subtasks = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtasksId()) {
            subtasks.add(subtaskRepo.findById(subtaskId));
        }
        return subtasks;
    }

    @Override
    public Epic getEpicOfSubtask(Subtask subtask) {
        return epicRepo.findById(subtask.getEpicId());
    }

    // Обновление
    @Override
    public void updateTask(Task task) {
        if (task.getId() == null || taskRepo.findById(task.getId()) == null) {
            System.out.println("Обновить можно только ранее сохраненную задачу");
            return;
        }

        taskRepo.save(task);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic.getId() == null || epicRepo.findById(epic.getId()) == null) {
            System.out.println("Обновить можно только ранее сохраненный эпик");
            return;
        }
        epicRepo.save(epic);
        updateEpicStatus(epic);
    }

    // При обновлении подзадачи нужно обновить родительский эпик
    @Override
    public void updateSubtask(Subtask subtask) {

        if (subtask.getId() == null){
            System.out.println("Изменить можно только сохраненную подзадачу");
            return;
        }

        Subtask oldSubtask = subtaskRepo.findById(subtask.getId());
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

        subtaskRepo.save(subtask);
        updateEpicStatus(epic);
    }

    // Удаление
    @Override
    public void removeTasks() {
        taskRepo.delete();
    }

    @Override
    public void removeTaskById(int id) {
        taskRepo.deleteById(id);
    }

    @Override
    public void removeEpics() {
        subtaskRepo.delete();
        epicRepo.delete();
    }

    // При удалении эпика все его подзадачи тоже удаляются
    @Override
    public void removeEpicById(int id) {
        Epic epic = epicRepo.findById(id);

        if (epic != null) {
            for (Integer subtaskId : epic.getSubtasksId()) {
                subtaskRepo.deleteById(subtaskId);
            }
            epicRepo.deleteById(id);
        }
    }

    // При удалении подзадач из хранилища также нужно удалить их у эпиков
    @Override
    public void removeSubtasks() {

        subtaskRepo.delete();

        for (Epic epic : epicRepo.findAll()) {
            epic.removeSubtasks();
            updateEpicStatus(epic);
        }
    }

    // При удалении подзадачи нужно обновить родительский эпик
    @Override
    public void removeSubtaskById(int id) {
        Subtask subtask = subtaskRepo.findById(id);

        if (subtask != null) {
            Epic epic = getEpicOfSubtask(subtask);
            if (epic != null) {
                subtaskRepo.deleteById(id);
                epic.removeSubtask(subtask);
                updateEpicStatus(epic);
            }
        }
    }

    @Override
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
