import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Collection;

import test.Test;

public class TaskTest extends Test {

    // Идентификаторы разных задач должны отличаться
    public void testThatDifferentTasksHaveDifferentId() {

        ArrayList<Task> tasks = createSampleTasks(2);
        Epic epic1 = createSampleEpic("epic 1", 2);
        Epic epic2 = createSampleEpic("epic 2", 1);
        ArrayList<Subtask> subtasks1 = epic1.getSubtasks();
        ArrayList<Subtask> subtasks2 = epic2.getSubtasks();

        // сохраняем задачи и эпики (у эпиков подзадачи сохранятся автоматически)
        TaskManager manager = new TaskManager();
        manager.saveTask(tasks.get(0));
        manager.saveTask(tasks.get(1));
        manager.saveTask(epic1);
        manager.saveTask(epic2);

        HashSet<Integer> ids = new HashSet<Integer>();

        ids.add( tasks.get(0).getId() );
        ids.add( tasks.get(1).getId() );
        ids.add( epic1.getId() );
        ids.add( epic2.getId() );
        ids.add( subtasks1.get(0).getId() );
        ids.add( subtasks1.get(1).getId() );
        ids.add( subtasks2.get(0).getId() );

        assertEquals(7, ids.size(), "идентификаторы разных задач должны отличаться");
    }

    // Возвращаются все обычные задачи
    public void testThatManagerReturnAllTasks() {
        ArrayList<Task> tasks = createSampleTasks(2);

        TaskManager manager = new TaskManager();
        manager.saveTask(tasks.get(0));
        manager.saveTask(tasks.get(1));

        boolean isEqual = areListEqualOrderIgnored(tasks, manager.getTasks());
        assertTrue(isEqual,"должны возвращаться все обычные задачи");
    }

    // Возвращаются все эпики
    public void testThatManagerReturnAllEpics() {

        Epic epic1 = createSampleEpic("epic 1", 2);
        Epic epic2 = createSampleEpic("epic 2", 1);

        TaskManager manager = new TaskManager();
        manager.saveTask(epic1);
        manager.saveTask(epic2);

        boolean isEqual = areListEqualOrderIgnored(List.of(epic1, epic2), manager.getEpics());
        assertTrue(isEqual, "должны возвращаться все эпики");
    }

    // Возвращаются все подзадачи, даже если принадлежат разным эпикам
    public void testThatManagerReturnAllSubtasks() {

        Epic epic1 = createSampleEpic("epic 1", 2);
        Epic epic2 = createSampleEpic("epic 2", 1);

        TaskManager manager = new TaskManager();
        manager.saveTask(epic1);
        manager.saveTask(epic2);

        ArrayList<Subtask> subtasks1 = epic1.getSubtasks();
        ArrayList<Subtask> subtasks2 = epic2.getSubtasks();
        List<Subtask> subtasks = List.of(subtasks1.get(0), subtasks1.get(1), subtasks2.get(0));

        boolean isEqual = areListEqualOrderIgnored(subtasks, manager.getSubtasks());
        assertTrue(isEqual, "должны возвращаться все подзадачи");
    }

    // Получение обычной задачи по идентификатору
    public void testThatManagerReturnTaskById() {
        ArrayList<Task> tasks = createSampleTasks(2);

        TaskManager manager = new TaskManager();
        manager.saveTask(tasks.get(0));
        manager.saveTask(tasks.get(1));

        int taskId1 = tasks.get(0).getId();
        Task task1 = manager.getTaskById(taskId1);
        assertEquals(tasks.get(0), task1, "должна вернуться первая задача по id = " + taskId1);

        int taskId2 = tasks.get(1).getId();
        Task task2 = manager.getTaskById(taskId2);
        assertEquals(tasks.get(1), task2, "должна вернуться вторая задача по id = " + taskId2);

        int taskId3 = -1;
        assertNull(manager.getTaskById(taskId3), "не должно вернуться задач по id = " + taskId3);
    }

    // Получение эпика по идентификатору
    public void testThatManagerReturnEpicById() {

        ArrayList<Task> tasks = createSampleTasks(2);
        Epic epic1 = createSampleEpic("epic 1", 1);
        Epic epic2 = createSampleEpic("epic 2", 2);

        TaskManager manager = new TaskManager();
        manager.saveTask(epic1);
        manager.saveTask(epic2);

        int id1 = epic1.getId();
        assertEquals(epic1, manager.getEpicById(id1), "должен вернуться первый эпик по id = " + id1);

        int id2 = epic2.getId();
        assertEquals(epic2, manager.getEpicById(id2), "должен вернуться второй эпик по id = " + id2);

        int id3 = -1;
        assertNull(manager.getEpicById(id3), "не должно вернуться эпиков по id = " + id3);
    }

    // Получение подзадач по идентификатору
    public void testThatManagerReturnSubtaskById() {

        Epic epic1 = createSampleEpic("epic 1", 2);
        Epic epic2 = createSampleEpic("epic 2", 1);

        TaskManager manager = new TaskManager();
        manager.saveTask(epic1);
        manager.saveTask(epic2);

        ArrayList<Subtask> subtasks1 = epic1.getSubtasks();
        ArrayList<Subtask> subtasks2 = epic2.getSubtasks();

        Subtask subtask11 = subtasks1.get(0);
        int id11 = subtask11.getId();
        assertEquals(subtask11, manager.getSubtaskById(id11), "должна вернуться первая подзадача первого эпика по id = " + id11);

        Subtask subtask12 = subtasks1.get(1);
        int id12 = subtask12.getId();
        assertEquals(subtask12, manager.getSubtaskById(id12), "должна вернуться вторая подзадача первого эпика по id = " + id12);

        Subtask subtask21 = subtasks2.get(0);
        int id21 = subtask21.getId();
        assertEquals(subtask21, manager.getSubtaskById(id21), "должна вернуться первая подзадача второго эпика по id = " + id21);

        int id = -1;
        assertNull(manager.getSubtaskById(id), "не должна вернуться подзадача по id = " + id);
    }

    public void testThatEpicReturnsSubtasks() {
        Epic epic = new Epic("epic1", "description of epic1");
        Subtask subtask1 = new Subtask("subtask1", "description of subtask1", epic);
        Subtask subtask2 = new Subtask("subtask2", "description of subtask2", epic);

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        ArrayList<Subtask> actualSubtasts = epic.getSubtasks();
        List<Subtask> expextedSubtasks = List.of(subtask1, subtask2);

        boolean isEqual = areListEqualOrderIgnored(expextedSubtasks, actualSubtasts);
        assertTrue(isEqual, "Эпик должен возвращать список подзадач");
    }

    private ArrayList<Task> createSampleTasks(int taskCount) {
        ArrayList<Task> tasks = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) {
            tasks.add(new Task("task " + i, "description of task " + i));
        }
        return tasks;
    }

    private Epic createSampleEpic(String epicName, int subtaskCount) {

        Epic epic = new Epic(epicName, "description of epic " + epicName);

        ArrayList<Subtask> subtasks = new ArrayList<>();
        for (int i = 0; i < subtaskCount; i++) {
            String subtaskName = epicName + ", subtask " + i;
            String subtaskDesc = "description of subtask " + i + " of epic " + epicName;
            Subtask subtask = new Subtask(subtaskName, subtaskDesc, epic);
            epic.addSubtask(subtask);
        }
        return epic;
    }

    private <T> boolean areListEqualOrderIgnored(Collection<T> list1, Collection<T> list2) {
        return list1.containsAll(list2) && list2.containsAll(list1);
    }

}