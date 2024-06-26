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
        manager.saveEpic(epic1);
        manager.saveEpic(epic2);

        HashSet<Integer> ids = new HashSet<>();

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
        ArrayList<Task> tasks = createSampleTasks(3);

        TaskManager manager = new TaskManager();
        manager.saveTask(tasks.get(0));
        manager.saveTask(tasks.get(1));
        manager.saveTask(tasks.get(2));

        boolean isEqual = areListEqualOrderIgnored(tasks, manager.getTasks());
        assertTrue(isEqual,"должны возвращаться все обычные задачи");
    }

    // Возвращаются все эпики
    public void testThatManagerReturnAllEpics() {

        Epic epic1 = createSampleEpic("epic 1", 2);
        Epic epic2 = createSampleEpic("epic 2", 1);

        TaskManager manager = new TaskManager();
        manager.saveEpic(epic1);
        manager.saveEpic(epic2);

        boolean isEqual = areListEqualOrderIgnored(List.of(epic1, epic2), manager.getEpics());
        assertTrue(isEqual, "должны возвращаться все эпики");
    }

    // Возвращаются все подзадачи, даже если принадлежат разным эпикам
    public void testThatManagerReturnAllSubtasks() {

        Epic epic1 = createSampleEpic("epic 1", 2);
        Epic epic2 = createSampleEpic("epic 2", 1);

        TaskManager manager = new TaskManager();
        manager.saveEpic(epic1);
        manager.saveEpic(epic2);

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

        Epic epic1 = createSampleEpic("epic 1", 1);
        Epic epic2 = createSampleEpic("epic 2", 2);

        TaskManager manager = new TaskManager();
        manager.saveEpic(epic1);
        manager.saveEpic(epic2);

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
        manager.saveEpic(epic1);
        manager.saveEpic(epic2);

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

    // Эпик возвращает подзадачи
    public void testThatEpicReturnsSubtasks() {
        Epic epic = new Epic("epic1", "description of epic1");
        Subtask subtask1 = new Subtask("subtask1", "description of subtask1", epic);
        Subtask subtask2 = new Subtask("subtask2", "description of subtask2", epic);

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        ArrayList<Subtask> actualSubtasks = epic.getSubtasks();
        List<Subtask> expectedSubtasks = List.of(subtask1, subtask2);

        boolean isEqual = areListEqualOrderIgnored(expectedSubtasks, actualSubtasks);
        assertTrue(isEqual, "Эпик должен возвращать список подзадач");
    }

    // Менеджер возвращает подзадачи эпика
    public void testThatManagerReturnsEpicSubtasks() {
        Epic epic0 = createSampleEpic("epic0", 2);
        Epic epic1 = createSampleEpic("epic1", 3);

        TaskManager manager = new TaskManager();
        manager.saveEpic(epic0);
        manager.saveEpic(epic1);

        ArrayList<Subtask> actualSubtasks = manager.getSubtasksByEpic(epic0);
        ArrayList<Subtask> expectedSubtasks = epic0.getSubtasks();
        boolean areSubtasksEqual = areListEqualOrderIgnored(expectedSubtasks, actualSubtasks);
        assertTrue(areSubtasksEqual, "менеджер должен возвращать подзадачи эпика");
    }

    // Удаление обычной задачи по идентификатору
    public void testThatManagerRemoveTaskById() {
        ArrayList<Task> tasks = createSampleTasks(3);

        TaskManager manager = new TaskManager();
        manager.saveTask(tasks.get(0));
        manager.saveTask(tasks.get(1));
        manager.saveTask(tasks.get(2));

        assertEquals(3, manager.getTasks().size(), "До удаления должно быть 3 задачи");

        int id1 = tasks.get(1).getId();

        // удаляем вторую
        manager.removeTaskById(id1);

        // остальные должны сохраниться
        Collection<Task> actualTasks = manager.getTasks();
        assertEquals(2, actualTasks.size(), "Должно быть 2 задачи после удаления");
        assertTrue(actualTasks.contains(tasks.get(0)), "Первая задача должна сохраниться");
        assertTrue(actualTasks.contains(tasks.get(2)), "Третья задача должна сохраниться");

    }

    // Удаление эпика по идентификатору
    public void testThatManagerRemoveEpicById() {

        Epic epic0 = createSampleEpic("epic0", 2);
        Epic epic1 = createSampleEpic("epic1", 3);
        Epic epic2 = createSampleEpic("epic2", 4);

        TaskManager manager = new TaskManager();
        manager.saveEpic(epic0);
        manager.saveEpic(epic1);
        manager.saveEpic(epic2);

        assertEquals(3, manager.getEpics().size(), "До удаления должно быть 3 эпика");
        assertEquals(9, manager.getSubtasks().size(), "До удаления должно быть 9 подзадач");

        // сохраняем подзадачи первого и третьего эпиков перед удалением
        ArrayList<Subtask> subtasks0 = new ArrayList<>(epic0.getSubtasks());
        ArrayList<Subtask> subtasks2 = new ArrayList<>(epic2.getSubtasks());

        // удаляем второй эпик
        manager.removeEpicById(epic1.getId());

        // проверяем эпики - второй должен удалиться
        Collection<Epic> actualEpics = manager.getEpics();
        assertEquals(2, actualEpics.size(), "После удаления должно быть 2 эпика");
        assertTrue(actualEpics.contains(epic0),"Первый эпик должен сохраниться");
        assertTrue(actualEpics.contains(epic2),"Третий эпик должен сохраниться");

        // проверяем подзадачи - подзадачи второго эпика должны удалиться
        Collection<Subtask> actualSubtasks = manager.getSubtasks();
        assertTrue(actualSubtasks.containsAll(subtasks0),"Подзадачи первого эпика должны сохраниться");
        assertTrue(actualSubtasks.containsAll(subtasks2),"Подзадачи второго эпика должны сохраниться");

        int expectedSubtaskCount = subtasks0.size() + subtasks2.size();
        assertEquals(expectedSubtaskCount, actualSubtasks.size(), "Подзадачи второго эпика должны удалиться");
    }

    // Удаление подзадачи по идентификатору:
    // после удаления подзадача не должна возвращаться из родительского эпика
    public void testThatManagerRemoveSubtaskById() {
        Epic epic = createSampleEpic("epic1", 3);

        TaskManager manager = new TaskManager();
        manager.saveEpic(epic);

        assertEquals(3, manager.getSubtasks().size(), "До удаления должно быть 3 подзадачи");

        ArrayList<Subtask> subtasks = new ArrayList<>(epic.getSubtasks());

        // удаляем вторую
        manager.removeSubtaskById(subtasks.get(1).getId());

        // остальные должны сохраниться в общем списке ...
        Collection<Subtask> actualSubtasks = manager.getSubtasks();
        assertEquals(2, actualSubtasks.size(), "Должно быть 2 подзадачи после удаления");
        assertTrue(actualSubtasks.contains(subtasks.get(0)), "Первая подзадача должна сохраниться");
        assertTrue(actualSubtasks.contains(subtasks.get(2)), "Третья подзадача должна сохраниться");

        // ... и у эпика
        ArrayList<Subtask> actualEpicSubtasks = epic.getSubtasks();
        assertEquals(2, actualEpicSubtasks.size(), "У эпика должно быть 2 подзадачи после удаления");
        assertTrue(actualEpicSubtasks.contains(subtasks.get(0)), "Первая подзадача у эпика должна сохраниться");
        assertTrue(actualEpicSubtasks.contains(subtasks.get(2)), "Третья подзадача у эпика должна сохраниться");

    }

    // удаление всех обычных задач
    public void testThatManagerRemoveAllTasks() {
        ArrayList<Task> tasks = createSampleTasks(3);

        TaskManager manager = new TaskManager();
        manager.saveTask(tasks.get(0));
        manager.saveTask(tasks.get(1));
        manager.saveTask(tasks.get(2));

        assertEquals(3, manager.getTasks().size(), "До удаления должно быть 3 задачи");

        manager.removeTasks();

        assertEquals(0, manager.getTasks().size(), "После удаления не должно быть задач");
    }

    // удаление всех обычных эпиков
    public void testThatManagerRemoveAllEpics() {
        Epic epic0 = createSampleEpic("epic0", 2);
        Epic epic1 = createSampleEpic("epic1", 3);
        Epic epic2 = createSampleEpic("epic2", 4);

        TaskManager manager = new TaskManager();
        manager.saveEpic(epic0);
        manager.saveEpic(epic1);
        manager.saveEpic(epic2);

        assertEquals(3, manager.getEpics().size(), "До удаления должно быть 3 эпика");
        assertEquals(9, manager.getSubtasks().size(), "До удаления должно быть 9 подзадач");

        manager.removeEpics();

        assertEquals(0, manager.getEpics().size(), "После удаления не должно быть эпиков");
        assertEquals(0, manager.getSubtasks().size(), "После удаления не должно быть подзадач");
    }

    // удаление всех подзадач
    // эпики не должны быть удалены, потому что они могут существовать без подзадач,
    // но все подзадачи у эпиков должны быть удалены
    public void testThatManagerRemoveAllSubtasks() {
        Epic epic0 = createSampleEpic("epic0", 2);
        Epic epic1 = createSampleEpic("epic1", 3);

        TaskManager manager = new TaskManager();
        manager.saveEpic(epic0);
        manager.saveEpic(epic1);

        assertEquals(2, manager.getEpics().size(), "До удаления должно быть 3 эпика");
        assertEquals(5, manager.getSubtasks().size(), "До удаления должно быть 9 подзадач");

        manager.removeSubtasks();

        assertEquals(2, manager.getEpics().size(), "После удаления не должно быть эпиков");
        assertEquals(0, manager.getSubtasks().size(), "После удаления не должно быть подзадач");

        assertEmpty(epic0.getSubtasks(), "После удаления у первого эпика не должно быть подзадач");
        assertEmpty(epic1.getSubtasks(), "После удаления у второго эпика не должно быть подзадач");
    }

    // обновление обычной задачи
    public void testThatManagerUpdateTask() {
        Task task = new Task("some new task", "some description of new task");

        TaskManager manager = new TaskManager();
        manager.saveTask(task);
        int taskId = task.getId();

        assertEquals(task, manager.getTaskById(taskId), "до обновления задача должна быть старой");

        Task newTask = new Task("some new task", "some description of new task");
        newTask.setId(taskId);

        manager.updateTask(newTask);

        assertTrue(task != manager.getTaskById(taskId), "после обновления задача не должна быть старой");
        assertTrue(newTask == manager.getTaskById(taskId), "после обновления задача должна быть новой");
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