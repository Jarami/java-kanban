import managers.Managers;
import managers.TaskManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import util.Tasks;

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
        for (int i = 0; i < 1000; i++) {
            Task randomTask = getRandomTask();

            // просматриваем задачу
            watch(randomTask);

            // После каждого запроса выведите историю и убедитесь, что в ней нет повторов.
            checkHistoryUniqueness();
        }

        // выводим статистику просмотров
        printWatchingStat();

        // Удалите задачу, которая есть в истории, и проверьте, что при печати она не будет выводиться.
        checkThatDeletedTaskIsRemovedFromHistory();

        // Удалите эпик с тремя подзадачами и убедитесь, что из истории удалился как сам эпик, так и все его подзадачи.
        checkThatDeletedEpicIsRemovedFromHistoryWithAllItSubtasks();

    }

    private static void createTasks() {
        createAndSaveTask("task1;desc1;NEW;2024-01-01 00:00:00;120");
        createAndSaveTask("task2;desc2;NEW;2024-01-02 00:00:00;120");

        Epic epic = createAndSaveEpic("epic1;desc3");
        createAndSaveSubtask("sub1;desc4;NEW;" +  epic.getId() + ";2024-01-03 00:00:00;120");
        createAndSaveSubtask("sub2;desc5;NEW;" +  epic.getId() + ";2024-01-04 00:00:00;120");
        createAndSaveSubtask("sub3;desc6;NEW;" +  epic.getId() + ";2024-01-05 00:00:00;120");

        createAndSaveEpic("epic2;desc3");
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

        history.forEach(task -> {
            int count = historyStat.getOrDefault(task, 0);
            historyStat.put(task, count + 1);
        });

        Set<Integer> doubles = new HashSet<>();
        historyStat.forEach((task, count) -> {
            if (count > 1) {
                doubles.add(task.getId());
            }
        });

        return String.format("doubles are " + doubles);

    }

    private static void printWatchingStat() {
        System.out.println("======== WATCHING STATS ===============");
        watchingStat.forEach((task, count) -> {
            System.out.println("task \"" + task.getName() + "\" has been watched " + count + " times");
        });
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

    private static Task createAndSaveTask(String formattedTask) {
        Task task = Tasks.createTask(formattedTask);
        manager.saveTask(task);
        tasks.add(task);
        return task;
    }

    private static Epic createAndSaveEpic(String formattedEpic) {
        Epic epic = Tasks.createEpic(formattedEpic);
        manager.saveEpic(epic);
        tasks.add(epic);
        return epic;
    }

    private static Subtask createAndSaveSubtask(String formattedSubtask) {
        Subtask sub = Tasks.createSubtask(formattedSubtask);
        manager.saveSubtask(sub);
        tasks.add(sub);
        return sub;
    }
}
