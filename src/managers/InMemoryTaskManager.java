package managers;

import repo.InMemoryRepo;
import repo.TaskRepo;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;
import static tasks.TaskStatus.*;

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

    private synchronized void setGeneratedId(int id) {
        if (taskCounter < id) {
            taskCounter = id;
        }
    }

    // Сохранение
    @Override
    public int saveTask(Task task) {

        if (task.getId() == null) {
            int id = generateTaskId();
            task.setId(id);
        } else {
            setGeneratedId(task.getId());
        }

        taskRepo.save(task);

        System.out.println("task created: " + task);

        return task.getId();
    }

    @Override
    public int saveEpic(Epic epic) {

        if (epic.getId() == null) {
            int id = generateTaskId();
            epic.setId(id);
        } else {
            setGeneratedId(epic.getId());
        }

        epicRepo.save(epic);

        System.out.println("epic created: " + epic);

        return epic.getId();
    }

    @Override
    public int saveSubtask(Subtask subtask) {

        Epic epic = getEpicOfSubtask(subtask);
        if (epic != null) {

            if (subtask.getId() == null) {
                int id = generateTaskId();
                subtask.setId(id);
            } else {
                setGeneratedId(subtask.getId());
            }

            subtaskRepo.save(subtask);

            // добавляем id подзадачи эпику
            epic.addSubtaskIdIfAbsent(subtask);
            updateEpicStatus(epic);

            System.out.println("subtask created: " + subtask);

            return subtask.getId();
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

        if (subtask.getId() == null) {
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
        taskRepo.findAll().forEach(task -> historyManager.remove(task.getId()));
        taskRepo.delete();
    }

    @Override
    public void removeTaskById(int id) {
        historyManager.remove(id);
        taskRepo.deleteById(id);
    }

    @Override
    public void removeEpics() {
        subtaskRepo.findAll().forEach(task -> historyManager.remove(task.getId()));
        subtaskRepo.delete();
        epicRepo.findAll().forEach(task -> historyManager.remove(task.getId()));
        epicRepo.delete();
    }

    // При удалении эпика все его подзадачи тоже удаляются
    @Override
    public void removeEpicById(int id) {
        Epic epic = epicRepo.findById(id);

        if (epic != null) {
            for (Integer subtaskId : epic.getSubtasksId()) {
                historyManager.remove(subtaskId);
                subtaskRepo.deleteById(subtaskId);
            }
            historyManager.remove(id);
            epicRepo.deleteById(id);
        }
    }

    // При удалении подзадач из хранилища также нужно удалить их у эпиков
    @Override
    public void removeSubtasks() {
        subtaskRepo.findAll().forEach(task -> historyManager.remove(task.getId()));
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
                historyManager.remove(id);
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

    private TaskStatus getEpicStatus(List<Subtask> subtasks) {

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

    private void updateEpicStatus(Epic epic) {
        List<Subtask> subtasks = getSubtasksOfEpic(epic);
        epic.setStatus(getEpicStatus(subtasks));
    }
}
