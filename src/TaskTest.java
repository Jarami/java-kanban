import java.util.*;

import test.Test;

public class TaskTest extends Test {

//    protected List<Method> getTestMethods() {
//        try {
//            return List.of(getClass().getMethod("testThatManagerReturnsCorrectHistoryAfterTaskWatching"));
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//            return List.of();
//        }
//    }

    // Проверить, что до просмотра задач история просмотров пуста
    public void testThatHistoryIsEmptyBeforeTaskWatching() {

        TaskManager manager = Managers.getDefault();

        Epic epic1 = new Epic("Эпик 1","Описание эпика 1");
        manager.saveEpic(epic1);

        Subtask subtask1 = new Subtask("Подзадача 1 эпика 1", "Описание подзадачи 1 эпика 1", epic1);
        manager.saveSubtask(subtask1);

        Subtask subtask2 = new Subtask("Подзадача 2 эпика 1", "Описание подзадачи 2 эпика 1", epic1);
        manager.saveSubtask(subtask2);

        Task task = new Task("Обычная задача", "Описание обычной задачи");
        manager.saveTask(task);

        List<Task> history = manager.getHistory();
        assertEmpty(history, "история просмотров должна быть пустой");
    }

    // Проверить, что после просмотра задач разного типа возвращается история просмотров
    public void testThatManagerReturnsCorrectHistoryAfterTaskWatching() {
        TaskManager manager = Managers.getDefault();

        Epic epic1 = new Epic("Эпик 1","Описание эпика 1");
        manager.saveEpic(epic1);

        Subtask subtask1 = new Subtask("Подзадача 1 эпика 1", "Описание подзадачи 1 эпика 1", epic1);
        Subtask subtask2 = new Subtask("Подзадача 2 эпика 1", "Описание подзадачи 2 эпика 1", epic1);
        saveTasks(manager, subtask1, subtask2);

        Task task = new Task("Обычная задача", "Описание обычной задачи");
        manager.saveTask(task);

        List<Task> history;
        List<Task> expectedHistory;

        manager.getTaskById(task.getId());
        history = manager.getHistory();
        expectedHistory = List.of(task);
        assertCollectionEquals(expectedHistory, history);

        manager.getSubtaskById(subtask1.getId());
        history = manager.getHistory();
        expectedHistory = List.of(task, subtask1);
        assertCollectionEquals(expectedHistory, history);

        manager.getEpicById(epic1.getId());
        history = manager.getHistory();
        expectedHistory = List.of(task, subtask1, epic1);
        assertCollectionEquals(expectedHistory, history);

        manager.getSubtaskById(subtask2.getId());
        history = manager.getHistory();
        expectedHistory = List.of(task, subtask1, epic1, subtask2);
        assertCollectionEquals(expectedHistory, history);
    }

    // Проверить, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
    public void testThatEvenAfterTaskUpdateHistoryHoldsOldStateOfTask() {
        TaskManager manager = Managers.getDefault();

        Task task1 = new Task("задача1", "Описание задачи1");
        Task task2 = new Task("задача2", "Описание задачи2");
        Task task3 = new Task("задача3", "Описание задачи3");
        saveTasks(manager, task1, task2, task3);

        watchTasks(manager, task1, task2, task3);

        // сохраняем старые значения
        int taskId2 = task2.getId();
        String oldName = task2.getName();
        String oldDescription = task2.getDescription();
        TaskStatus oldStatus = task2.getStatus();

        // меняем task2
        Task newTask2 = new Task(taskId2, "другая задача2", "описание другой задачи2", TaskStatus.DONE);
        manager.updateTask(newTask2);

        // смотрим ее
        watchTasks(manager, newTask2);

        List<Task> history = manager.getHistory();
        Task oldTask2 = history.get(1);

        assertEquals(oldStatus, oldTask2.getStatus(),
                String.format("статус должен быть %s, а не %s", oldStatus, oldTask2.getStatus()));

        assertEquals(oldName, oldTask2.getName(),
                String.format("имя должно быть %s, а не %s", oldName, oldTask2.getName()));

        assertEquals(oldDescription, oldTask2.getDescription(),
                String.format("имя должно быть %s, а не %s", oldDescription, oldTask2.getDescription()));

        assertEquals(taskId2, oldTask2.getId(),
                String.format("id должно быть %s, а не %s", taskId2, oldTask2.getDescription()));
    }

    public void testThatEpicStatusChangesAsSubtaskStatusChanges() {
        TaskManager taskManager = new InMemoryTaskManager();

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

    // Проверить, что эпик создается с разными параметрами
    public void testThatTaskCanBeCreatedWithVariousParams() {
        Task task1 = new Task("task1", "desc1");
        assertNotNull(task1, "должна существовать задача 1");

        Task task2 = new Task(2, "task2", "desc2");
        assertNotNull(task2, "должна существовать задача 1");

        Task task3 = new Task("task3", "desc3", TaskStatus.DONE);
        assertNotNull(task2, "должна существовать задача 3");

        Task task4 = new Task(4, "task4", "desc4", TaskStatus.IN_PROGRESS);
        assertNotNull(task2, "должна существовать задача 4");
    }

    // Проверить, что обычная задача создается.
    // У нее появляется статус NEW и id (после сохранения).
    public void testThatManagerCreateTaskWithIdAndStatus() {

        TaskManager manager = Managers.getDefault();

        Task task = new Task("t", "dt");
        assertNull(task.getId(), "До сохранения у задачи не должен быть определен id");
        assertEquals(task.getStatus(), TaskStatus.NEW, "После создания у задачи должен быть статус NEW");

        int id = manager.saveTask(task);

        assertEquals(id, task.getId(), "После сохранения у задачи должен быть определен id");
        assertEquals(task.getStatus(), TaskStatus.NEW, "После сохранения у задачи должен быть статус NEW");
    }

    // Проверить, что эпик создается с разными параметрами
    public void testThatEpicCanBeCreatedWithVariousParams() {
        Epic epic1 = new Epic("epic1", "desc1");
        assertNotNull(epic1, "должен существовать эпик 1");

        Epic epic2 = new Epic(1, "epic2", "desc2");
        assertNotNull(epic2, "должен существовать эпик 2");
    }

    // Проверить, что эпик создается.
    // У него появляется статус NEW и id.
    public void testThatManagerCreateEpicWithIdAndStatus() {

        TaskManager manager = Managers.getDefault();

        Epic epic = new Epic("e", "d");
        int id = manager.saveEpic(epic);

        assertEquals(id, epic.getId(), "После сохранения у эпика должен быть определен id");
        assertEquals(epic.getStatus(), TaskStatus.NEW, "После сохранения у эпика должен быть статус NEW");
    }

    // Проверить, что подзадача создается с разными параметрами
    public void testThatSubtaskCanBeCreatedWithVariousParams() {

        Epic epic = new Epic("epic", "desc");

        Subtask sub1 = new Subtask("sub1", "desc1", epic);
        assertNotNull(sub1, "должна существовать подзадача 1");

        Subtask sub2 = new Subtask(1, "sub1", "desc1", epic);
        assertNotNull(sub2, "должна существовать подзадача 2");

        Subtask sub3 = new Subtask("sub1", "desc1", TaskStatus.DONE, epic);
        assertNotNull(sub3, "должна существовать подзадача 3");

        Subtask sub4 = new Subtask(2, "sub1", "desc1", TaskStatus.DONE, epic);
        assertNotNull(sub4, "должна существовать подзадача 4");
    }

    // Проверить, что подзадача создается
    // У нее появляется статус NEW и id (после сохранения)
    public void testThatManagerCreateSubtaskWithIdAndStatus() {

        TaskManager manager = Managers.getDefault();

        Epic epic = new Epic("эпик", "описание эпика");
        manager.saveEpic(epic);

        Subtask subtask = new Subtask("подзадача", "описание подзадачи", epic);

        assertNull(subtask.getId(), "До сохранения у подзадачи не должен быть определен id");
        assertEquals(subtask.getStatus(), TaskStatus.NEW, "После создания у подзадачи должен быть статус NEW");

        int id = manager.saveSubtask(subtask);

        assertEquals(id, subtask.getId(), "После сохранения у подзадачи должен быть определен id");
        assertEquals(subtask.getStatus(), TaskStatus.NEW, "После сохранения у подзадачи должен быть статус NEW");
    }

    // Проверить, что идентификаторы разных задач отличаются
    public void testThatDifferentTasksHaveDifferentId() {

        TaskManager manager = Managers.getDefault();

        List<Task> tasks = createSampleTasks(2);
        manager.saveTask(tasks.get(0));
        manager.saveTask(tasks.get(1));

        Epic epic1 = saveEpicWithSubtasks(manager, "epic 1", 2);
        Epic epic2 = saveEpicWithSubtasks(manager, "epic 2", 1);

        List<Subtask> subtasks1 = manager.getSubtasksOfEpic(epic1);
        List<Subtask> subtasks2 = manager.getSubtasksOfEpic(epic2);

        // сохраняем задачи, эпики и подзадачи

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

        TaskManager manager = Managers.getDefault();
        manager.saveTask(tasks.get(0));
        manager.saveTask(tasks.get(1));

        List<Task> expectedTasks = List.of(tasks.get(0), tasks.get(1));
        assertCollectionEquals(expectedTasks, manager.getTasks());
    }

    // Проверить, что возвращается обычная задача по идентификатору
    public void testThatManagerReturnTaskById() {

        TaskManager manager = Managers.getDefault();

        // создаем 3 задачи, а сохраняем 2
        List<Task> tasks = createSampleTasks(3);
        manager.saveTask(tasks.get(0));
        manager.saveTask(tasks.get(1));

        int id = tasks.get(0).getId();
        assertTaskEquals(tasks.get(0), manager.getTaskById(id));
    }

    // Проверить, что возвращаются все эпики
    public void testThatManagerReturnAllEpics() {

        TaskManager manager = Managers.getDefault();

        // Создаем 3 эпика, а сохраняем 2
        Epic epic1 = new Epic("e1", "d1");
        Epic epic2 = new Epic("e2", "d2");
        Epic epic3 = new Epic("e3", "d3");

        manager.saveEpic(epic1);
        manager.saveEpic(epic2);

        List<Epic> expectedEpics = List.of(epic1, epic2);
        assertCollectionEquals(expectedEpics, manager.getEpics());
    }

    // Проверить получение эпика по идентификатору
    public void testThatManagerReturnEpicById() {

        TaskManager manager = Managers.getDefault();

        // создаем 3 эпика, а сохраняем 2
        Epic epic1 = new Epic("e1", "d1");
        Epic epic2 = new Epic("e2", "d2");
        Epic epic3 = new Epic("e3", "d3");

        manager.saveEpic(epic1);
        manager.saveEpic(epic2);

        int id = epic1.getId();
        assertEpicEquals(epic1, manager.getEpicById(id));
    }

    // Проверить, что возвращаются все подзадачи, даже если принадлежат разным эпикам
    public void testThatManagerReturnAllSubtasks() {

        TaskManager manager = Managers.getDefault();

        Epic epic1 = saveEpicWithSubtasks(manager, "e1", 2);
        Epic epic2 = saveEpicWithSubtasks(manager, "e2", 1);

        List<Subtask> expectedSubtasks = new ArrayList<>();
        expectedSubtasks.addAll(manager.getSubtasksOfEpic(epic1));
        expectedSubtasks.addAll(manager.getSubtasksOfEpic(epic2));

        assertCollectionEquals(expectedSubtasks, manager.getSubtasks());
    }

    // Проверить получение подзадач по идентификатору
    public void testThatManagerReturnSubtaskById() {

        TaskManager manager = Managers.getDefault();

        Epic epic1 = saveEpicWithSubtasks(manager, "эпик1", 2);
        Epic epic2 = saveEpicWithSubtasks(manager, "эпик2", 1);

        List<Subtask> subtasks1 = manager.getSubtasksOfEpic(epic1);
        Subtask subtask11 = subtasks1.get(0);
        int id11 = subtask11.getId();

        assertSubtaskEquals(subtask11, manager.getSubtaskById(id11));
    }

    // Проверить, что менеджер возвращает подзадачи эпика
    public void testThatManagerReturnsEpicSubtasks() {

        TaskManager manager = Managers.getDefault();

        Epic epic = new Epic("e", "d");
        manager.saveEpic(epic);

        Subtask subtask1 = new Subtask("s1", "d1", epic);
        manager.saveSubtask(subtask1);

        Subtask subtask2 = new Subtask("s2", "d2", epic);
        manager.saveSubtask(subtask2);

        List<Subtask> actualSubtasks = manager.getSubtasksOfEpic(epic);
        List<Subtask> expectedSubtasks = List.of(subtask1, subtask2);

        assertCollectionEquals(expectedSubtasks, actualSubtasks);
    }

    // Проверить, что обычная задача обновляется
    public void testThatManagerUpdateTask() {

        TaskManager manager = Managers.getDefault();

        Task task = new Task("t", "d");
        manager.saveTask(task);

        int id = task.getId();

        Task newTask = new Task(id, "nt", "nd");
        manager.updateTask(newTask);

        assertTaskEquals(newTask, manager.getTaskById(id));
    }

    // Проверить, что эпик обновляется
    public void testThatManagerUpdateEpic() {

        TaskManager manager = Managers.getDefault();

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

        TaskManager manager = Managers.getDefault();

        Epic epic = new Epic("e", "old-d");
        manager.saveEpic(epic);

        Subtask subtask1 = new Subtask("s1", "old-s1", epic);
        manager.saveSubtask(subtask1);

        Subtask subtask2 = new Subtask("s2", "old-s2", epic);
        manager.saveSubtask(subtask2);

        int id = subtask2.getId();

        // обновляем вторую подзадачу
        Subtask newSubtask = new Subtask(id, "n", "d", epic);
        manager.updateSubtask(newSubtask);

        // проверяем репозиторий
        assertSubtaskEquals(newSubtask, manager.getSubtaskById(id));

        // проверяем подзадачи у конкретного эпика
        List<Subtask> actualSubtasks = manager.getSubtasksOfEpic(epic);
        Subtask actualSubtask = findSubtaskById(actualSubtasks, id);

        assertSubtaskEquals(newSubtask, actualSubtask);
    }

    // Проверить, что статус эпика обновляется после обновления его подзадачи.
    public void testThatEpicStatusUpdatedAfterSubtaskUpdate() {
        TaskManager manager = Managers.getDefault();

        // создаем эпик с подзадачами
        Epic epic = new Epic("e", "d");
        manager.saveEpic(epic);

        Subtask subtask1 = new Subtask("s1", "ds1", epic);
        manager.saveSubtask(subtask1);

        Subtask subtask2 = new Subtask("s2", "ds2", epic);
        manager.saveSubtask(subtask2);

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
        List<Task> tasks = createSampleTasks(2);

        TaskManager manager = Managers.getDefault();
        manager.saveTask(tasks.get(0));
        manager.saveTask(tasks.get(1));

        manager.removeTasks();

        assertEmpty(manager.getTasks(), "После удаления не должно быть задач");
    }

    // Проверить удаление обычной задачи по идентификатору.
    public void testThatManagerRemoveTaskById() {
        TaskManager manager = Managers.getDefault();

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

        TaskManager manager = Managers.getDefault();

        Epic epic0 = saveEpicWithSubtasks(manager, "e1", 2);
        Epic epic1 = saveEpicWithSubtasks(manager, "e2", 3);

        List<Epic> epicsBeforeRemove = List.of(epic0, epic1);
        assertCollectionEquals(epicsBeforeRemove, manager.getEpics());

        manager.removeEpics();

        assertEmpty(manager.getEpics(), "После удаления не должно быть эпиков");
        assertEmpty(manager.getSubtasks(), "После удаления не должно быть подзадач");
    }

    // Проверить удаление эпика по идентификатору.
    // Его подзадачи тоже должны удалиться.
    public void testThatManagerRemoveEpicById() {

        TaskManager manager = Managers.getDefault();

        Epic epic0 = saveEpicWithSubtasks(manager, "e0", 2);
        Epic epic1 = saveEpicWithSubtasks(manager, "e1", 3);

        // сохраняем подзадачи первого эпика перед удалением
        List<Subtask> expectedSubtasks = List.copyOf(manager.getSubtasksOfEpic(epic0));

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
        TaskManager manager = Managers.getDefault();

        Epic epic0 = saveEpicWithSubtasks(manager, "e0", 2);
        Epic epic1 = saveEpicWithSubtasks(manager, "e1", 3);

        manager.removeSubtasks();

        assertEmpty(manager.getSubtasks(), "После удаления не должно быть подзадач");
        assertEmpty(manager.getSubtasksOfEpic(epic0), "После удаления у первого эпика не должно быть подзадач");
        assertEmpty(manager.getSubtasksOfEpic(epic1), "После удаления у второго эпика не должно быть подзадач");
    }

    // Удаление подзадачи по идентификатору.
    // После удаления подзадача не должна возвращаться из родительского эпика.
    public void testThatManagerRemoveSubtaskById() {

        TaskManager manager = Managers.getDefault();

        Epic epic = saveEpicWithSubtasks(manager,"epic1", 3);

        List<Subtask> subtasks = manager.getSubtasksOfEpic(epic);
        List<Subtask> expectedSubtasks = List.of(subtasks.get(0), subtasks.get(2));

        // удаляем вторую
        manager.removeSubtaskById(subtasks.get(1).getId());

        // остальные должны сохраниться в общем списке ...
        assertCollectionEquals(expectedSubtasks, manager.getSubtasks());

        // ... и у эпика
        assertCollectionEquals(expectedSubtasks, manager.getSubtasksOfEpic(epic));
    }

    // Проверить обновление статуса эпика после удаления всех его подзадач.
    // Должен стать NEW.
    public void testThatEpicStatusUpdatedAfterSubtaskRemoval() {

        TaskManager manager = Managers.getDefault();

        // У этого эпика статус DONE
        Epic epic1 = new Epic("e1", "de1");
        manager.saveEpic(epic1);

        Subtask subtask11 = new Subtask("s11", "ds11", TaskStatus.DONE, epic1);
        manager.saveSubtask(subtask11);

        // У этого эпика статус IN_PROGRESS
        Epic epic2 = new Epic("e2", "de2");
        manager.saveEpic(epic2);

        Subtask subtask21 = new Subtask("s21", "ds21", TaskStatus.IN_PROGRESS, epic2);
        manager.saveSubtask(subtask21);

        // удаляем единственную подзадачу эпика e1
        manager.removeSubtaskById(subtask11.getId());

        assertEquals(TaskStatus.NEW, epic1.getStatus(),
                String.format("статус первого эпика должен быть NEW, а не %s", epic1.getStatus()));

        assertEquals(TaskStatus.IN_PROGRESS, epic2.getStatus(),
                String.format("статус второго эпика должен быть IN_PROGRESS, а не %s", epic2.getStatus()));
    }

    // Проверить, что подзадача удаленного эпика не сохраняется
    public void testThatSavingSubtaskOfDeletedEpicIsIgnored() {
        TaskManager manager = Managers.getDefault();

        Epic epic = new Epic("epic", "desc");
        manager.saveEpic(epic);

        Subtask subtask = new Subtask("sub1", "desc1", epic);
        manager.removeEpics();

        manager.saveSubtask(subtask);

        assertEmpty(manager.getSubtasks(), "подзадач не должно существовать");
    }

    // Проверить, что подзадача удаленного эпика не обновляется
    public void testThatUpdatingSubtaskOfDeletedEpicIsIgnored() {
        TaskManager manager = Managers.getDefault();

        Epic epic = new Epic("epic", "desc");
        manager.saveEpic(epic);

        Subtask subtask = new Subtask("sub1", "desc1", epic);
        manager.removeEpics();

        Subtask newSubtask = new Subtask(subtask.getId(), "sub2", "desc2", epic);
        manager.updateSubtask(newSubtask);

        assertEmpty(manager.getSubtasks(), "подзадач не должно существовать");
    }

    // Проверить, что удаление подзадачи удаленного эпика игнорируется
    public void testThatDeletingSubtaskOfDeletedEpicIsIgnored() {
        TaskManager manager = Managers.getDefault();

        Epic epic = new Epic("epic", "desc");
        manager.saveEpic(epic);

        Subtask subtask = new Subtask("sub1", "desc1", epic);
        manager.saveSubtask(subtask);
        manager.removeEpics();

        manager.removeSubtaskById(subtask.getId());
        assertEmpty(manager.getSubtasks(), "подзадач не должно существовать");
    }

    private List<Task> createSampleTasks(int taskCount) {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) {
            tasks.add(new Task("t" + i, "dt" + i));
        }
        return tasks;
    }

    private Epic saveEpicWithSubtasks(TaskManager manager, String epicName, int subtaskCount) {

        Epic epic = new Epic(epicName, "d" + epicName);
        manager.saveEpic(epic);

        for (int i = 0; i < subtaskCount; i++) {
            String subtaskName = epicName + ":s" + i;
            String subtaskDesc = "ds" + i + " of " + epicName;
            Subtask subtask = new Subtask(subtaskName, subtaskDesc, epic);
            manager.saveSubtask(subtask);
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

        assertEquals(expectedSubtask.getEpicId(), actualSubtask.getEpicId(),
                String.format("эпик должен быть %s, а не %s", expectedSubtask.getEpicId(), actualSubtask.getEpicId()));
    }

    private void assertEpicEquals(Epic expectedEpic, Epic actualEpic) {
        assertTaskEquals(expectedEpic, actualEpic);

        List<Integer> expectedSubtasks = expectedEpic.getSubtasksId();
        List<Integer> actualSubtasks = actualEpic.getSubtasksId();
        assertCollectionEquals(expectedSubtasks, actualSubtasks);
    }

    private Subtask findSubtaskById(List<Subtask> subtasks, int id) {
        for (Subtask subtask : subtasks) {
            if (subtask.getId() == id) {
                return subtask;
            }
        }
        return null;
    }

    private void watchTasks(TaskManager manager, Task ...tasks){
        for (Task task : tasks) {
            if (task instanceof Epic) {
                manager.getEpicById(task.getId());
            } else if (task instanceof Subtask) {
                manager.getSubtaskById(task.getId());
            } else {
                manager.getTaskById(task.getId());
            }
        }
    }

    // С сохранением есть нюанс - эпики должны быть сохранены перед тем,
    // как создавать (именно создавать, а не сохранять) подзадачи,
    // потому что подзадачи запоминают не сам эпик, а его id. А id у эпика
    // появляется только после сохранения.
    private void saveTasks(TaskManager manager, Task ...tasks){
        for (Task task : tasks) {
            if (task instanceof Epic) {
                manager.saveEpic((Epic)task);
            } else if (task instanceof Subtask) {
                manager.saveSubtask((Subtask)task);
            } else {
                manager.saveTask(task);
            }
        }
    }

}