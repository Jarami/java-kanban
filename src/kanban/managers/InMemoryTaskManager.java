package kanban.managers;

import kanban.exceptions.ManagerSaveException;
import kanban.repo.InMemoryRepo;
import kanban.repo.TaskRepo;
import kanban.tasks.Epic;
import kanban.tasks.Subtask;
import kanban.tasks.Task;

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
    public Optional<Task> getTaskById(int id) {
        Optional<Task> task = taskRepo.findById(id);
        task.ifPresent(historyManager::add);
        return task;
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epicRepo.findAll());
    }

    @Override
    public Optional<Epic> getEpicById(int id) {
        Optional<Epic> epic = epicRepo.findById(id);
        epic.ifPresent(historyManager::add);
        return epic;
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtaskRepo.findAll());
    }

    @Override
    public Optional<Subtask> getSubtaskById(int id) {
        Optional<Subtask> sub = subtaskRepo.findById(id);
        sub.ifPresent(historyManager::add);
        return sub;
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(Epic epic) {
        List<Subtask> subtasks = new ArrayList<>();
        epic.getSubtasksId().forEach(subtaskId ->
            subtaskRepo.findById(subtaskId).ifPresent(subtasks::add));
        return subtasks;
    }

    @Override
    public Epic getEpicOfSubtask(Subtask subtask) {
        return epicRepo.findById(subtask.getEpicId()).orElse(null);
    }

    // Обновление
    @Override
    public void updateTask(Task task) {
        if (task.getId() == null || taskRepo.findById(task.getId()).isEmpty()) {
            System.out.println("Обновить можно только ранее сохраненную задачу");
            return;
        }

        if (isIntercepted(task)) {
            throw new ManagerSaveException("Подзадача не должна пересекаться с другими!");
        }

        taskRepo.findById(task.getId()).ifPresent(this::deprioritize);
        prioritize(task);

        taskRepo.save(task);

    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic.getId() == null || epicRepo.findById(epic.getId()).isEmpty()) {
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

        Optional<Subtask> oldSubtask = subtaskRepo.findById(subtask.getId());
        if (oldSubtask.isEmpty()) {
            System.out.println("Изменить можно только существующую подзадачу");
            return;
        }

        Epic epic = getEpicOfSubtask(subtask);
        Epic oldEpic = getEpicOfSubtask(oldSubtask.get());

        if (!oldEpic.equals(epic)) {
            System.out.println("Подзадача не может изменить свой эпик! Предыдущий эпик " + oldEpic +
                    ", новый " + epic);
            return;
        }

        if (isIntercepted(subtask)) {
            throw new ManagerSaveException("Подзадача не должна пересекаться с другими!");
        }

        subtaskRepo.findById(subtask.getId()).ifPresent(this::deprioritize);
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
        taskRepo.findById(id)
                .ifPresent(task -> {
                    deprioritize(task);
                    historyManager.remove(id);
                    taskRepo.deleteById(id);
                });
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

        epicRepo.findById(id).ifPresent(epic -> {
            epic.getSubtasksId().forEach(subtaskId -> {
                subtaskRepo.findById(subtaskId).ifPresent(this::deprioritize);
                historyManager.remove(subtaskId);
                subtaskRepo.deleteById(subtaskId);
            });
            historyManager.remove(id);
            epicRepo.deleteById(id);
        });
    }

    // При удалении подзадач из хранилища также нужно удалить их у эпиков
    @Override
    public void removeSubtasks() {
        subtaskRepo.findAll().forEach(task -> {
            deprioritize(task);
            historyManager.remove(task.getId());
        });
        subtaskRepo.delete();

        epicRepo.findAll().forEach(epic -> {
            epic.removeSubtasks();
            updateEpicProperties(epic);
        });
    }

    // При удалении подзадачи нужно обновить родительский эпик
    @Override
    public void removeSubtaskById(int id) {
        subtaskRepo.findById(id).ifPresent(subtask -> {
            Epic epic = getEpicOfSubtask(subtask);
            if (epic != null) {
                deprioritize(subtask);
                historyManager.remove(id);
                subtaskRepo.deleteById(id);
                epic.removeSubtask(subtask);
                updateEpicProperties(epic);
            }
        });
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

        Optional<Task> interceptingTask = getPrioritizedTasks().stream()
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

        return interceptingTask.isPresent();
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
