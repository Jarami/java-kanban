import java.lang.reflect.Method;
import java.util.*;

import test.Test;

public class TaskTest extends Test {

    public void testThatEpicStatusChangesAsSubtaskStatusChanges() {
        TaskManager taskManager = new TaskManager();

        Epic epic1 = new Epic("Эпик 1","Нужно сделать");
        taskManager.saveEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask1 создания", "Написать что то",epic1);
        taskManager.saveSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask2 создания", "Написать что то",epic1);
        taskManager.saveSubtask(subtask2);

        System.out.println(epic1);

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);

        System.out.println(epic1);

        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask2);

        System.out.println(epic1);

        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);

        System.out.println(epic1);

        assertEquals(TaskStatus.DONE, epic1.getStatus(), "должен быть статус DONE, а не " + epic1.getStatus());
    }

    // Проверить, что обычная задача создается.
    // У нее появляется статус NEW и id (после сохранения).
    public void testThatManagerCreateTaskWithIdAndStatus() {

        TaskManager manager = new TaskManager();

        Task task = new Task("t", "dt");
        assertNull(task.getId(), "До сохранения у задачи не должен быть определен id");
        assertEquals(task.getStatus(), TaskStatus.NEW, "После создания у задачи должен быть статус NEW");

        manager.saveTask(task);

        assertNotNull(task.getId(), "После сохранения у задачи должен быть определен id");
        assertEquals(task.getStatus(), TaskStatus.NEW, "После сохранения у задачи должен быть статус NEW");
    }

    // Проверить, что эпик создается.
    // У него появляется статус NEW и id (после сохранения).
    // У всех его подзадач также появляется статус NEW и id (после сохранения).
    public void testThatManagerCreateEpicWithIdAndStatus() {

        TaskManager manager = new TaskManager();

        Epic epic = createSampleEpic("e", 2);

        assertNull(epic.getId(), "До сохранения у эпика не должен быть определен id");
        assertEquals(epic.getStatus(), TaskStatus.NEW, "После создания у эпика должен быть статус NEW");

        manager.saveTask(epic);

        assertNotNull(epic.getId(), "После сохранения у эпика должен быть определен id");
        assertEquals(epic.getStatus(), TaskStatus.NEW, "После сохранения у эпика должен быть статус NEW");
    }

    // Проверить, что подзадачи эпика сохраняются при сохранении эпика.
    // У подзадач появляется id
    public void testThatManagerSavesSubtasksAsEpicSaved() {

        TaskManager manager = new TaskManager();

        Epic epic = createSampleEpic("e", 2);
        List<Subtask> subtasks = List.copyOf(epic.getSubtasks());

        for (Subtask subtask : subtasks) {
            assertNull(subtask.getId(),
                    String.format("До сохранения у подзадачи %s не должен быть определен id", subtask.getName()));

            assertEquals(subtask.getStatus(), TaskStatus.NEW,
                    String.format("После создания у подзадачи %s должен быть статус NEW", subtask.getName()));
        }

        manager.saveEpic(epic);

        for (Subtask subtask : subtasks) {
            assertNotNull(subtask.getId(),
                    String.format("После сохранения у подзадачи %s должен быть определен id", subtask.getName()));

            assertEquals(subtask.getStatus(), TaskStatus.NEW,
                    String.format("После создания у подзадачи %s должен быть статус NEW", subtask.getName()));
        }
    }

    // Проверить, что подзадача создается
    // У нее появляется статус NEW и id (после сохранения)
    public void testThatManagerCreateSubtaskWithIdAndStatus() {

        TaskManager manager = new TaskManager();

        Epic epic = new Epic("эпик", "описание эпика");
        Subtask subtask = new Subtask("подзадача", "описание подзадачи", epic);

        assertNull(subtask.getId(), "До сохранения у подзадачи не должен быть определен id");
        assertEquals(subtask.getStatus(), TaskStatus.NEW, "После создания у подзадачи должен быть статус NEW");

        manager.saveSubtask(subtask);

        assertNotNull(subtask.getId(), "После сохранения у подзадачи должен быть определен id");
        assertEquals(subtask.getStatus(), TaskStatus.NEW, "После сохранения у подзадачи должен быть статус NEW");
    }

    // Проверить, что идентификаторы разных задач отличаются
    public void testThatDifferentTasksHaveDifferentId() {

        TaskManager manager = new TaskManager();

        List<Task> tasks = createSampleTasks(2);
        Epic epic1 = createSampleEpic("epic 1", 2);
        Epic epic2 = createSampleEpic("epic 2", 1);
        List<Subtask> subtasks1 = epic1.getSubtasks();
        List<Subtask> subtasks2 = epic2.getSubtasks();

        // сохраняем задачи, эпики и подзадачи
        manager.saveTask(tasks.get(0));
        manager.saveTask(tasks.get(1));
        manager.saveEpic(epic1);
        manager.saveEpic(epic2);

        Set<Integer> ids = Set.of(
            tasks.get(0).getId(),
            tasks.get(1).getId(),
            epic1.getId(),
            epic2.getId(),
            subtasks1.get(0).getId(),
            subtasks1.get(1).getId(),
            subtasks2.get(0).getId()
        );

        assertEquals(7, ids.size(), "идентификаторы разных задач должны отличаться");
    }

    // Проверить, что возвращаются все обычные задачи
    public void testThatManagerReturnAllTasks() {
        // создаем 3 задачи, а сохраняем 2
        List<Task> tasks = createSampleTasks(3);

        TaskManager manager = new TaskManager();
        manager.saveTask(tasks.get(0));
        manager.saveTask(tasks.get(1));

        List<Task> expectedTasks = List.of(tasks.get(0), tasks.get(1));
        assertCollectionEquals(expectedTasks, manager.getTasks());
    }

    // Проверить, что возвращается обычная задача по идентификатору
    public void testThatManagerReturnTaskById() {

        TaskManager manager = new TaskManager();

        // создаем 3 задачи, а сохраняем 2
        List<Task> tasks = createSampleTasks(3);
        manager.saveTask(tasks.get(0));
        manager.saveTask(tasks.get(1));

        int id = tasks.get(0).getId();
        assertTaskEquals(tasks.get(0), manager.getTaskById(id));
    }

    // Проверить, что возвращаются все эпики
    public void testThatManagerReturnAllEpics() {

        TaskManager manager = new TaskManager();

        // Создаем 3 эпика, а сохраняем 2
        Epic epic1 = createSampleEpic("epic 1", 2);
        Epic epic2 = createSampleEpic("epic 2", 1);
        Epic epic3 = createSampleEpic("epic 3", 0);

        manager.saveEpic(epic1);
        manager.saveEpic(epic2);

        List<Epic> expectedEpics = List.of(epic1, epic2);
        assertCollectionEquals(expectedEpics, manager.getEpics());
    }

    // Проверить получение эпика по идентификатору
    public void testThatManagerReturnEpicById() {

        TaskManager manager = new TaskManager();

        // создаем 3 эпика, а сохраняем 2
        Epic epic1 = createSampleEpic("epic 1", 1);
        Epic epic2 = createSampleEpic("epic 2", 2);
        Epic epic3 = createSampleEpic("epic 2", 3);

        manager.saveEpic(epic1);
        manager.saveEpic(epic2);

        int id = epic1.getId();
        assertEpicEquals(epic1, manager.getEpicById(id));
    }

    // Проверить, что возвращаются все подзадачи, даже если принадлежат разным эпикам
    public void testThatManagerReturnAllSubtasks() {

        TaskManager manager = new TaskManager();

        Epic epic1 = createSampleEpic("epic 1", 2);
        Epic epic2 = createSampleEpic("epic 2", 1);

        manager.saveEpic(epic1);
        manager.saveEpic(epic2);

        List<Subtask> expectedSubtasks = new ArrayList<>();
        expectedSubtasks.addAll(epic1.getSubtasks());
        expectedSubtasks.addAll(epic2.getSubtasks());

        assertCollectionEquals(expectedSubtasks, manager.getSubtasks());
    }

    // Проверить получение подзадач по идентификатору
    public void testThatManagerReturnSubtaskById() {

        TaskManager manager = new TaskManager();

        Epic epic1 = createSampleEpic("эпик1", 2);
        Epic epic2 = createSampleEpic("эпик2", 1);

        manager.saveEpic(epic1);
        manager.saveEpic(epic2);

        List<Subtask> subtasks1 = epic1.getSubtasks();
        Subtask subtask11 = subtasks1.get(0);
        int id11 = subtask11.getId();

        assertSubtaskEquals(subtask11, manager.getSubtaskById(id11));
    }

    // Проверить, что менеджер возвращает подзадачи эпика
    public void testThatManagerReturnsEpicSubtasks() {

        TaskManager manager = new TaskManager();

        Epic epic0 = createSampleEpic("e0", 2);
        Epic epic1 = createSampleEpic("e1", 3);

        manager.saveEpic(epic0);
        manager.saveEpic(epic1);

        List<Subtask> actualSubtasks = manager.getSubtasksByEpic(epic0);
        List<Subtask> expectedSubtasks = epic0.getSubtasks();

        assertCollectionEquals(expectedSubtasks, actualSubtasks);
    }

    // Проверить, что обычная задача обновляется
    public void testThatManagerUpdateTask() {

        TaskManager manager = new TaskManager();

        Task task = new Task("t", "d");
        manager.saveTask(task);

        int id = task.getId();

        Task newTask = new Task(id, "nt", "nd");
        manager.updateTask(newTask);

        assertTaskEquals(newTask, manager.getTaskById(id));
    }

    // Проверить, что эпик обновляется
    public void testThatManagerUpdateEpic() {

        TaskManager manager = new TaskManager();

        Epic epic = new Epic("e", "d");
        manager.saveEpic(epic);

        int epicId = epic.getId();

        Epic newEpic = new Epic(epicId, "ne", "nd");
        manager.updateEpic(newEpic);

        assertEpicEquals(newEpic, manager.getEpicById(epicId));
    }

    // Проверить обновление подзадачи.
    // У родительского эпика новая подзадача тоже должна присутствовать.
    public void testThatManagerUpdateSubtask() {

        TaskManager manager = new TaskManager();

        Epic epic = new Epic("e", "old-d");
        Subtask subtask1 = new Subtask("s1", "old-s1", epic);
        Subtask subtask2 = new Subtask("s2", "old-s2", epic);
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        manager.saveEpic(epic);

        int id = subtask2.getId();

        Subtask newSubtask = new Subtask(id, "n", "d", epic);
        manager.updateSubtask(newSubtask);

        // проверяем репозиторий
        assertSubtaskEquals(newSubtask, manager.getSubtaskById(id));

        // проверяем подзадачи у конкретного эпика
        List<Subtask> actualSubtasks = manager.getSubtasksByEpic(epic);
        Subtask actualSubtask = findSubtaskById(actualSubtasks, id);

        assertSubtaskEquals(newSubtask, actualSubtask);
    }

    // Проверить, что статус эпика обновляется после обновления его подзадачи.
    public void testThatEpicStatusUpdatedAfterSubtaskUpdate() {
        TaskManager manager = new TaskManager();

        // создаем эпик с подзадачами
        Epic epic = new Epic("e", "d");
        Subtask subtask1 = new Subtask("s1", "ds1", epic);
        Subtask subtask2 = new Subtask("s2", "ds2", epic);
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        manager.saveEpic(epic);

        // берем первую подзадачу в работу
        int id1 = subtask1.getId();
        manager.updateSubtask(new Subtask(id1, "ns1", "nds1", TaskStatus.IN_PROGRESS, epic));

        // статус эпика должен стать IN_PROGRESS
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(),
                String.format("статус эпика должен быть IN_PROGRESS, а не %s", epic.getStatus()));

        // "выполняем" обе подзадачи
        int id2 = subtask2.getId();
        manager.updateSubtask(new Subtask(id1, "nns1", "nnds1", TaskStatus.DONE, epic));
        manager.updateSubtask(new Subtask(id2, "nns2", "nnds2", TaskStatus.DONE, epic));

        // статус эпика должен стать DONE
        assertEquals(TaskStatus.DONE, epic.getStatus(),
                String.format("статус эпика должен быть DONE, а не %s", epic.getStatus()));
    }

    // Проверить удаление всех обычных задач.
    public void testThatManagerRemoveAllTasks() {
        ArrayList<Task> tasks = createSampleTasks(2);

        TaskManager manager = new TaskManager();
        manager.saveTask(tasks.get(0));
        manager.saveTask(tasks.get(1));

        manager.removeTasks();

        assertEmpty(manager.getTasks(), "После удаления не должно быть задач");
    }

    // Проверить удаление обычной задачи по идентификатору.
    public void testThatManagerRemoveTaskById() {
        TaskManager manager = new TaskManager();

        List<Task> tasks = createSampleTasks(3);
        manager.saveTask(tasks.get(0));
        manager.saveTask(tasks.get(1));
        manager.saveTask(tasks.get(2));

        int id1 = tasks.get(1).getId();

        // удаляем вторую
        manager.removeTaskById(id1);

        // остальные должны сохраниться
        List<Task> expectedTasks = List.of(tasks.get(0), tasks.get(2));

        assertCollectionEquals(expectedTasks, manager.getTasks());
    }

    // Проверить удаление всех эпиков.
    // Все подзадачи также должны удалиться.
    public void testThatManagerRemoveAllEpics() {

        TaskManager manager = new TaskManager();

        Epic epic0 = createSampleEpic("epic0", 2);
        Epic epic1 = createSampleEpic("epic1", 3);

        manager.saveEpic(epic0);
        manager.saveEpic(epic1);

        manager.removeEpics();

        assertEmpty(manager.getEpics(), "После удаления не должно быть эпиков");
        assertEmpty(manager.getSubtasks(), "После удаления не должно быть подзадач");
    }

    // Проверить удаление эпика по идентификатору.
    // Его подзадачи тоже должны удалиться.
    public void testThatManagerRemoveEpicById() {

        TaskManager manager = new TaskManager();

        Epic epic0 = createSampleEpic("epic0", 2);
        Epic epic1 = createSampleEpic("epic1", 3);

        manager.saveEpic(epic0);
        manager.saveEpic(epic1);

        // сохраняем подзадачи первого эпика перед удалением
        List<Subtask> expectedSubtasks = List.copyOf(epic0.getSubtasks());

        // удаляем второй эпик
        manager.removeEpicById(epic1.getId());

        // проверяем эпики - должен остаться только первый
        assertCollectionEquals(List.of(epic0), manager.getEpics());

        // проверяем подзадачи - должны остаться только подзадачи первого эпика
        assertCollectionEquals(expectedSubtasks, manager.getSubtasks());
    }

    // Проверить удаление всех подзадач.
    // Эпики не должны быть удалены, потому что они могут существовать без подзадач,
    // но все подзадачи у эпиков должны быть удалены
    public void testThatManagerRemoveAllSubtasks() {
        TaskManager manager = new TaskManager();

        Epic epic0 = createSampleEpic("e0", 2);
        Epic epic1 = createSampleEpic("e1", 3);
        manager.saveEpic(epic0);
        manager.saveEpic(epic1);

        manager.removeSubtasks();

        assertEmpty(manager.getSubtasks(), "После удаления не должно быть подзадач");
        assertEmpty(manager.getSubtasksByEpic(epic0), "После удаления у первого эпика не должно быть подзадач");
        assertEmpty(manager.getSubtasksByEpic(epic1), "После удаления у второго эпика не должно быть подзадач");
    }

    // Удаление подзадачи по идентификатору.
    // После удаления подзадача не должна возвращаться из родительского эпика.
    public void testThatManagerRemoveSubtaskById() {

        TaskManager manager = new TaskManager();

        Epic epic = createSampleEpic("epic1", 3);
        manager.saveEpic(epic);

        List<Subtask> subtasks = epic.getSubtasks();
        List<Subtask> expectedSubtasks = List.of(subtasks.get(0), subtasks.get(2));

        // удаляем вторую
        manager.removeSubtaskById(subtasks.get(1).getId());

        // остальные должны сохраниться в общем списке ...
        assertCollectionEquals(expectedSubtasks, manager.getSubtasks());

        // ... и у эпика
        assertCollectionEquals(expectedSubtasks, manager.getSubtasksByEpic(epic));
    }

    // Проверить обновление статуса эпика после удаления всех его подзадач.
    // Должен стать NEW.
    public void testThatEpicStatusUpdatedAfterSubtaskRemoval() {

        TaskManager manager = new TaskManager();

        // У этого эпика статус DONE
        Epic epic1 = new Epic("e1", "de1");
        Subtask subtask11 = new Subtask("s11", "ds11", TaskStatus.DONE, epic1);
        epic1.addSubtask(subtask11);

        // У этого эпика статус IN_PROGRESS
        Epic epic2 = new Epic("e2", "de2");
        Subtask subtask21 = new Subtask("s21", "ds21", TaskStatus.IN_PROGRESS, epic2);
        epic2.addSubtask(subtask21);
        System.out.println();
        manager.saveEpic(epic1);
        manager.saveEpic(epic2);

        manager.removeSubtaskById(subtask11.getId());

        assertEquals(TaskStatus.NEW, epic1.getStatus(),
                String.format("статус первого эпика должен быть NEW, а не %s", epic1.getStatus()));

        assertEquals(TaskStatus.IN_PROGRESS, epic2.getStatus(),
                String.format("статус второго эпика должен быть IN_PROGRESS, а не %s", epic2.getStatus()));
    }

    private ArrayList<Task> createSampleTasks(int taskCount) {
        ArrayList<Task> tasks = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) {
            tasks.add(new Task("t" + i, "dt" + i));
        }
        return tasks;
    }

    private Epic createSampleEpic(String epicName, int subtaskCount) {

        Epic epic = new Epic(epicName, "d" + epicName);

        for (int i = 0; i < subtaskCount; i++) {
            String subtaskName = epicName + ":s" + i;
            String subtaskDesc = "ds" + i + " of " + epicName;
            Subtask subtask = new Subtask(subtaskName, subtaskDesc, epic);
            epic.addSubtask(subtask);
        }
        return epic;
    }

    private <T> void assertCollectionEquals(Collection<T> expected, Collection<T> actual) {
        assertEquals(expected.size(), actual.size(),
                String.format("размер должен быть %s, а не %s", expected.size(), actual.size()));

        assertTrue(expected.containsAll(actual), "ожидаемая коллекция не содержит всех элементов текущей");
        assertTrue(actual.containsAll(expected), "текущая коллекция не содержит всех элементов ожидаемой");
    }

    private void assertTaskEquals(Task expectedTask, Task actualTask) {

        assertNotNull(actualTask, "задача должна быть определена");

        assertEquals(expectedTask.getId(), actualTask.getId(),
                String.format("идентификатор должен быть %s, а не %s", expectedTask.getId(), actualTask.getId()));

        assertEquals(expectedTask.getName(), actualTask.getName(),
                String.format("имя должно быть %s, а не %s", expectedTask.getName(), actualTask.getName()));

        assertEquals(expectedTask.getDescription(), actualTask.getDescription(),
                String.format("описание должно быть %s, а не %s", expectedTask.getName(), actualTask.getName()));

        assertEquals(expectedTask.getStatus(), actualTask.getStatus(),
                String.format("статус должен быть %s, а не %s", expectedTask.getStatus(), actualTask.getStatus()));
    }

    private void assertSubtaskEquals(Subtask expectedSubtask, Subtask actualSubtask) {

        assertTaskEquals(expectedSubtask, actualSubtask);

        assertEquals(expectedSubtask.getEpic(), actualSubtask.getEpic(),
                String.format("эпик должен быть %s, а не %s", expectedSubtask.getEpic(), actualSubtask.getEpic()));
    }

    private void assertEpicEquals(Epic expectedEpic, Epic actualEpic) {
        assertTaskEquals(expectedEpic, actualEpic);

        List<Subtask> expectedSubtasks = expectedEpic.getSubtasks();
        List<Subtask> actualSubtasks = actualEpic.getSubtasks();
        assertEquals(expectedSubtasks.size(), actualSubtasks.size(), String.format(
                "количество подзадач должно быть %s, а не %s", expectedSubtasks.size(), actualSubtasks.size()));

        for (int i = 0; i < expectedSubtasks.size(); i++) {
            assertSubtaskEquals(expectedSubtasks.get(i), actualSubtasks.get(i));
        }
    }

    private Subtask findSubtaskById(List<Subtask> subtasks, int id) {
        for (Subtask subtask : subtasks) {
            if (subtask.getId() == id) {
                return subtask;
            }
        }
        return null;
    }

}