import managers.Managers;
import managers.TaskManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.*;

public class Main {

    static TaskManager manager = Managers.getDefault();
    static Random random = new Random();
    static List<Task> tasks = new ArrayList<>();
    static Map<Task, Integer> watchingStat = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("Поехали!");

        // Создайте две задачи, эпик с тремя подзадачами и эпик без подзадач.
        createTasks();

        // Запросите созданные задачи несколько раз в разном порядке.
        for(int i = 0; i < 1000; i++) {
            Task randomTask = getRandomTask();
            watch(randomTask);
            // После каждого запроса выведите историю и убедитесь, что в ней нет повторов.
            checkHistoryUniqueness();
        }

        printWatchingStat();

        // Удалите задачу, которая есть в истории, и проверьте, что при печати она не будет выводиться.
        checkThatDeletedTaskIsRemovedFromHistory();

        // Удалите эпик с тремя подзадачами и убедитесь, что из истории удалился как сам эпик, так и все его подзадачи.
        checkThatDeletedEpicIsRemovedFromHistoryWithAllItSubtasks();

    }

    private static void createTasks() {
        createAndSaveTask("task1", "desc of task1");
        createAndSaveTask("task2", "desc of task2");
        createAndSaveEpicWithSubs("epic1", "desc of epic1", 3);
        createAndSaveEpicWithSubs("epic2", "desc of epic2", 0);
    }

    private static void createAndSaveTask(String name, String desc) {
        Task task = new Task(name, desc);
        manager.saveTask(task);
        tasks.add(task);
    }

    private static void createAndSaveEpicWithSubs(String name, String desc, int subtaskCount) {
        Epic epic = new Epic(name, desc);
        manager.saveEpic(epic);
        tasks.add(epic);

        if (subtaskCount > 0) {
            for(int i = 0; i < subtaskCount; i++) {
                Subtask sub = new Subtask("sub " + i + " of " + name, "sub " + i + " desc of " + name,
                        epic);
                manager.saveSubtask(sub);
                tasks.add(sub);
            }
        }
    }

    private static Task getRandomTask() {
        return tasks.get(random.nextInt(tasks.size()));
    }

    private static void watch(Task task) {
        if (task instanceof Epic) {
            manager.getEpicById(task.getId());
        } else if (task instanceof Subtask) {
            manager.getSubtaskById(task.getId());
        } else {
            manager.getTaskById(task.getId());
        }
        int value = watchingStat.getOrDefault(task, 0);
        watchingStat.put(task, value + 1);
    }

    private static void checkHistoryUniqueness() {
        List<Task> history = manager.getHistory();
        Set<Task> uniqueHistory = Set.copyOf(history);
        if (history.size() != uniqueHistory.size()) {
            throw new RuntimeException(getFailUniquenessMessage(history));
        }
    }

    private static String getFailUniquenessMessage(List<Task> history) {
        Map<Task, Integer> historyStat = new HashMap<>();
        for(Task task : history) {
            int count = historyStat.getOrDefault(task, 0);
            historyStat.put(task, count + 1);
        }
        Set<Integer> doubles = new HashSet<>();
        for(Map.Entry<Task, Integer> entry : historyStat.entrySet()) {
            if (entry.getValue() > 1) {
                doubles.add(entry.getKey().getId());
            }
        }
        return String.format("doubles are " + doubles);

    }

    private static void printWatchingStat() {
        System.out.println("======== WATCHING STATS ===============");
        for(Map.Entry<Task, Integer> entry : watchingStat.entrySet()) {
            System.out.println("task \"" + entry.getKey().getName() + "\" has been watched " + entry.getValue() +
                    " times");
        }
        System.out.println("=======================================");
    }

    private static void checkThatDeletedTaskIsRemovedFromHistory() {

        Task taskToDelete = manager.getTasks().getFirst();

        removeTask(taskToDelete);

        List<Task> history = manager.getHistory();
        if (history.contains(taskToDelete)) {
            throw new RuntimeException("задача " + taskToDelete.getName() + " не должна выводится в истории просмотров");
        } else {
            System.out.println("задача " + taskToDelete.getName() + " успешно удалилась из истории просмотров");
        }
    }

    private static void checkThatDeletedEpicIsRemovedFromHistoryWithAllItSubtasks() {
        Epic epicToDelete = manager.getEpics()
                .stream()
                .filter(e -> !manager.getSubtasksOfEpic(e).isEmpty())
                .findFirst()
                .orElseThrow();
        List<Subtask> subtasks = manager.getSubtasksOfEpic(epicToDelete);

        removeTask(epicToDelete);

        List<Task> history = manager.getHistory();
        if (history.contains(epicToDelete)) {
            throw new RuntimeException("эпик \"" + epicToDelete.getName() +
                    "\" не должен выводится в истории просмотров");
        } else {
            System.out.println("эпик \"" + epicToDelete.getName() + "\" успешно удалился из истории просмотров");
        }

        subtasks.forEach(sub -> {
            if (history.contains(sub)) {
                throw new RuntimeException("подзадача \"" + sub.getName() +
                        "\" не должна выводится в истории просмотров");
            } else {
                System.out.println("подзадача \"" + sub.getName() + "\" успешно удалилась из истории просмотров");
            }
        });
    }

    private static void removeTask(Task task) {
        if (task instanceof Epic) {
            manager.removeEpicById(task.getId());
        } else if (task instanceof Subtask) {
            manager.removeSubtaskById(task.getId());
        } else {
            manager.removeTaskById(task.getId());
        }
    }
}
