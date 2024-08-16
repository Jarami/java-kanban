package util;

import managers.TaskManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

public class Tasks {
    private Tasks(){}

    public static Task createAndSaveTask(TaskManager manager, String name, String desc) {
        Task task = new Task(name, desc);
        manager.saveTask(task);
        return task;
    }

    public static Epic createAndSaveEpic(TaskManager manager, String name, String desc) {
        Epic epic = new Epic(name, desc);
        manager.saveEpic(epic);
        return epic;
    }

    public static Subtask createAndSaveSubtask(TaskManager manager, String name, String desc, Epic epic) {
        Subtask sub = new Subtask(name, desc, epic);
        manager.saveSubtask(sub);
        return sub;
    }

    public static void createAndSaveEpicWithSubs(TaskManager manager, String name, String desc, int subtaskCount) {
        Epic epic = createAndSaveEpic(manager, name, desc);

        for (int i = 0; i < subtaskCount; i++) {
            createAndSaveSubtask(manager, "sub " + i + " of " + name, "sub " + i + " desc of " + name,
                    epic);
        }
    }

    public static void printTasks(TaskManager manager) {

        System.out.println("manager: " + manager);

        System.out.println("  tasks:");
        for (Task task : manager.getTasks()) {
            System.out.println("    " + task);
        }

        System.out.println("  epics:");
        for (Epic epic : manager.getEpics()) {
            System.out.println("    " + epic);
            for (Subtask sub : manager.getSubtasksOfEpic(epic)) {
                System.out.println("      " + sub);
            }
        }
    }
}
