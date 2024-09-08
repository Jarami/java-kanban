package managers;

import exceptions.ManagerSaveException;
import repo.InMemoryRepo;
import repo.TaskRepo;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private static int taskCounter;

    private final TaskRepo<Task> taskRepo;
    private final TaskRepo<Epic> epicRepo;
    private final TaskRepo<Subtask> subtaskRepo;
    private final HistoryManager historyManager;
    private final Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));

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

        checkDurationBeforeSaving(task);

        if (isIntercepted(task)) {
            throw new ManagerSaveException("Задача не должна пересекаться с другими!");
        }

        if (task.getId() == null) {
            int id = generateTaskId();
            task.setId(id);
        } else {
            setGeneratedId(task.getId());
        }

        prioritize(task);
        taskRepo.save(task);

        System.out.println("task created: " + task);

        return task.getId();
    }

    @Override
    public int saveEpic(Epic epic) {

        checkDurationBeforeSaving(epic);

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

        checkDurationBeforeSaving(subtask);

        if (isIntercepted(subtask)) {
            throw new ManagerSaveException("Подзадача не должна пересекаться с другими!");
        }

        Epic epic = getEpicOfSubtask(subtask);
        if (epic != null) {

            if (subtask.getId() == null) {
                int id = generateTaskId();
                subtask.setId(id);
            } else {
                setGeneratedId(subtask.getId());
            }

            prioritize(subtask);
            subtaskRepo.save(subtask);

            epic.addSubtaskIdIfAbsent(subtask);
            updateEpicProperties(epic);

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

        if (isIntercepted(task)) {
            throw new ManagerSaveException("Подзадача не должна пересекаться с другими!");
        }

        deprioritize(taskRepo.findById(task.getId()));
        prioritize(task);

        taskRepo.save(task);

    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic.getId() == null || epicRepo.findById(epic.getId()) == null) {
            System.out.println("Обновить можно только ранее сохраненный эпик");
            return;
        }
        epicRepo.save(epic);
        updateEpicProperties(epic);
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

        if (isIntercepted(subtask)) {
            throw new ManagerSaveException("Подзадача не должна пересекаться с другими!");
        }

        deprioritize(subtaskRepo.findById(subtask.getId()));
        prioritize(subtask);

        subtaskRepo.save(subtask);

        updateEpicProperties(epic);
    }

    // Удаление
    @Override
    public void removeTasks() {
        taskRepo.findAll().forEach(task -> {
            deprioritize(task);
            historyManager.remove(task.getId());
        });
        taskRepo.delete();
    }

    @Override
    public void removeTaskById(int id) {
        Task task = taskRepo.findById(id);
        if (task != null) {
            deprioritize(task);
            historyManager.remove(id);
            taskRepo.deleteById(id);
        }
    }

    @Override
    public void removeEpics() {
        subtaskRepo.findAll().forEach(task -> {
            deprioritize(task);
            historyManager.remove(task.getId());
        });
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
                deprioritize(subtaskRepo.findById(subtaskId));
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
        subtaskRepo.findAll().forEach(task -> {
            deprioritize(task);
            historyManager.remove(task.getId());
        });
        subtaskRepo.delete();

        for (Epic epic : epicRepo.findAll()) {
            epic.removeSubtasks();
            updateEpicProperties(epic);
        }
    }

    // При удалении подзадачи нужно обновить родительский эпик
    @Override
    public void removeSubtaskById(int id) {
        Subtask subtask = subtaskRepo.findById(id);

        if (subtask != null) {
            Epic epic = getEpicOfSubtask(subtask);
            if (epic != null) {
                deprioritize(subtask);
                historyManager.remove(id);
                subtaskRepo.deleteById(id);
                epic.removeSubtask(subtask);
                updateEpicProperties(epic);
            }
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private void checkDurationBeforeSaving(Task task) {
        if (task.getDuration() != null && task.getDuration().toMinutes() < 0) {
            throw new ManagerSaveException("Продолжительность выполнения задачи должна быть положительной!");
        }
    }

    private boolean isIntercepted(Task task) {
        if (task.getDuration() == null || task.getStartTime() == null) {
            return false;
        }

        LocalDateTime startTime = task.getStartTime();
        LocalDateTime endTime = startTime.plus(task.getDuration());

        Optional<Task> maybeTask = getPrioritizedTasks().stream()
                .filter(t -> {
                    if (t.equals(task)) {
                        return false;
                    }

                    if (t.getDuration() == null) {
                        return false;
                    }

                    LocalDateTime startTime1 = t.getStartTime();
                    LocalDateTime endTime1 = startTime1.plus(t.getDuration());

                    return startTime1.isBefore(endTime) && endTime1.isAfter(startTime);
                })
                .findAny();

        return maybeTask.isPresent();
    }

    private void updateEpicProperties(Epic epic) {
        List<Subtask> subtasks = getSubtasksOfEpic(epic);
        epic.update(subtasks);
    }

    private void prioritize(Task task) {
        if (task != null && task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void deprioritize(Task task) {
        if (task != null && task.getStartTime() != null) {
            prioritizedTasks.remove(task);
        }
    }

}
