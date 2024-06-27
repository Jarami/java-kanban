import java.util.*;

import test.Test;

public class TaskTest extends Test {

    // Проверить, что обычная задача создается
    // У нее появляется статус NEW и id (после сохранения)
    private void testThatManagerCreateTaskWithIdAndStatus() {
        Task task = new Task("задача", "описание задачи");
        assertNull(task.getId(), "До сохранения у задачи не должен быть определен id");
        assertEquals(task.getStatus(), TaskStatus.NEW, "После создания у задачи должен быть статус NEW");

        TaskManager manager = new TaskManager();
        manager.saveTask(task);

        assertNotNull(task.getId(), "После сохранения у задачи должен быть определен id");
        assertEquals(task.getStatus(), TaskStatus.NEW, "После сохранения у задачи должен быть статус NEW");
    }

    // Проверить, что эпик создается
    // У него появляется статус NEW и id (после сохранения)
    // У всех его подзадач также появляется статус NEW и id (после сохранения)
    private void testThatManagerCreateEpicWithIdAndStatus() {
        Epic epic = createSampleEpic("эпик", 2);
        assertNull(epic.getId(), "До сохранения у эпика не должен быть определен id");
        assertEquals(epic.getStatus(), TaskStatus.NEW, "После создания у эпика должен быть статус NEW");

        List<Subtask> subtasks = epic.getSubtasks();
        assertEquals(2, subtasks.size(), "У эпика должно быть 2 подзадачи");

        for (Subtask subtask : subtasks) {
            assertNull(subtask.getId(),
                    String.format("До сохранения у подзадачи %s не должен быть определен id", subtask.getName()));

            assertEquals(subtask.getStatus(), TaskStatus.NEW,
                    String.format("После создания у подзадачи %s должен быть статус NEW", subtask.getName()));
        }

        TaskManager manager = new TaskManager();
        manager.saveTask(epic);

        assertNotNull(epic.getId(), "После сохранения у эпика должен быть определен id");
        assertEquals(epic.getStatus(), TaskStatus.NEW, "После сохранения у эпика должен быть статус NEW");

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
        Epic epic = new Epic("эпик", "описание эпика");
        Subtask subtask = new Subtask("подзадача", "описание подзадачи", epic);
        assertNull(subtask.getId(), "До сохранения у подзадачи не должен быть определен id");
        assertEquals(subtask.getStatus(), TaskStatus.NEW, "После создания у подзадачи должен быть статус NEW");

        TaskManager manager = new TaskManager();
        manager.saveSubtask(subtask);

        assertNotNull(subtask.getId(), "После сохранения у подзадачи должен быть определен id");
        assertEquals(subtask.getStatus(), TaskStatus.NEW, "После сохранения у подзадачи должен быть статус NEW");
    }

    // Проверить, что идентификаторы разных задач отличаются
    public void testThatDifferentTasksHaveDifferentId() {

        List<Task> tasks = createSampleTasks(2);
        Epic epic1 = createSampleEpic("epic 1", 2);
        Epic epic2 = createSampleEpic("epic 2", 1);
        List<Subtask> subtasks1 = epic1.getSubtasks();
        List<Subtask> subtasks2 = epic2.getSubtasks();

        // сохраняем задачи и эпики (у эпиков подзадачи сохранятся автоматически)
        TaskManager manager = new TaskManager();
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
        boolean isEqual = areListEqualOrderIgnored(expectedTasks, manager.getTasks());
        assertTrue(isEqual,"должны возвращаться все обычные задачи");
    }

    // Проверить, что возвращается обычная задача по идентификатору
    public void testThatManagerReturnTaskById() {
        // создаем 3 задачи, а сохраняем 2
        List<Task> tasks = createSampleTasks(3);

        TaskManager manager = new TaskManager();
        manager.saveTask(tasks.get(0));
        manager.saveTask(tasks.get(1));

        int taskId1 = tasks.get(0).getId();
        Task task1 = manager.getTaskById(taskId1);
        assertEquals(tasks.get(0), task1, "должна вернуться первая задача по id = " + taskId1);

        int taskId2 = tasks.get(1).getId();
        Task task2 = manager.getTaskById(taskId2);
        assertEquals(tasks.get(1), task2, "должна вернуться вторая задача по id = " + taskId2);

        assertEquals(2, manager.getTasks().size(), "должно быть всего 2 задачи");
    }

    // Проверить, что возвращаются все эпики
    public void testThatManagerReturnAllEpics() {
        // Создаем 3 эпика, а сохраняем 2
        Epic epic1 = createSampleEpic("epic 1", 2);
        Epic epic2 = createSampleEpic("epic 2", 1);
        Epic epic3 = createSampleEpic("epic 3", 0);

        TaskManager manager = new TaskManager();
        manager.saveEpic(epic1);
        manager.saveEpic(epic2);

        List<Epic> expectedEpics = List.of(epic1, epic2);
        boolean isEqual = areListEqualOrderIgnored(expectedEpics, manager.getEpics());
        assertTrue(isEqual, "должны возвращаться все эпики");
    }

    // Проверить получение эпика по идентификатору
    public void testThatManagerReturnEpicById() {
        // создаем 3 эпика, а сохраняем 2
        Epic epic1 = createSampleEpic("epic 1", 1);
        Epic epic2 = createSampleEpic("epic 2", 2);
        Epic epic3 = createSampleEpic("epic 2", 3);

        TaskManager manager = new TaskManager();
        manager.saveEpic(epic1);
        manager.saveEpic(epic2);

        int id1 = epic1.getId();
        assertEquals(epic1, manager.getEpicById(id1), "должен вернуться первый эпик по id = " + id1);

        int id2 = epic2.getId();
        assertEquals(epic2, manager.getEpicById(id2), "должен вернуться второй эпик по id = " + id2);

        assertEquals(2, manager.getEpics().size(), "должно быть всего 2 эпика");
    }

    // Проверить, что возвращаются все подзадачи, даже если принадлежат разным эпикам
    public void testThatManagerReturnAllSubtasks() {

        Epic epic1 = createSampleEpic("epic 1", 2);
        Epic epic2 = createSampleEpic("epic 2", 1);

        TaskManager manager = new TaskManager();
        manager.saveEpic(epic1);
        manager.saveEpic(epic2);

        List<Subtask> expectedSubtasks = new ArrayList<>();
        expectedSubtasks.addAll(epic1.getSubtasks());
        expectedSubtasks.addAll(epic2.getSubtasks());

        boolean isEqual = areListEqualOrderIgnored(expectedSubtasks, manager.getSubtasks());
        assertTrue(isEqual, "должны возвращаться все подзадачи");
    }

    // Проверить получение подзадач по идентификатору
    public void testThatManagerReturnSubtaskById() {

        Epic epic1 = createSampleEpic("эпик1", 2);
        Epic epic2 = createSampleEpic("эпик2", 1);

        TaskManager manager = new TaskManager();
        manager.saveEpic(epic1);
        manager.saveEpic(epic2);

        List<Subtask> subtasks1 = epic1.getSubtasks();
        List<Subtask> subtasks2 = epic2.getSubtasks();

        Subtask subtask11 = subtasks1.get(0);
        int id11 = subtask11.getId();
        assertEquals(subtask11, manager.getSubtaskById(id11), "должна вернуться первая подзадача первого эпика по id = " + id11);

        Subtask subtask12 = subtasks1.get(1);
        int id12 = subtask12.getId();
        assertEquals(subtask12, manager.getSubtaskById(id12), "должна вернуться вторая подзадача первого эпика по id = " + id12);

        Subtask subtask21 = subtasks2.get(0);
        int id21 = subtask21.getId();
        assertEquals(subtask21, manager.getSubtaskById(id21), "должна вернуться первая подзадача второго эпика по id = " + id21);

        assertEquals(3, manager.getSubtasks().size(), "должно быть только 3 подзадачи");
    }

    // Проверить, что эпик возвращает подзадачи
    public void testThatEpicReturnsSubtasks() {
        Epic epic = new Epic("эпик1", "");
        Subtask subtask1 = new Subtask("подзадача1", "", epic);
        Subtask subtask2 = new Subtask("подзадача2", "", epic);

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        List<Subtask> expectedSubtasks = List.of(subtask1, subtask2);
        boolean isEqual = areListEqualOrderIgnored(expectedSubtasks, epic.getSubtasks());
        assertTrue(isEqual, "Эпик должен возвращать свои подзадачи");
    }

    // Проверить, что менеджер возвращает подзадачи эпика
    public void testThatManagerReturnsEpicSubtasks() {
        Epic epic0 = createSampleEpic("epic0", 2);
        Epic epic1 = createSampleEpic("epic1", 3);

        TaskManager manager = new TaskManager();
        manager.saveEpic(epic0);
        manager.saveEpic(epic1);

        List<Subtask> actualSubtasks = manager.getSubtasksByEpic(epic0);
        List<Subtask> expectedSubtasks = epic0.getSubtasks();
        boolean areSubtasksEqual = areListEqualOrderIgnored(expectedSubtasks, actualSubtasks);
        assertTrue(areSubtasksEqual, "менеджер должен возвращать подзадачи эпика");
    }

    // Проверить, что обычная задача обновляется
    public void testThatManagerUpdateTask() {
        Task task = new Task("задача", "");

        TaskManager manager = new TaskManager();
        manager.saveTask(task);

        int taskId = task.getId();

        assertTrue(task == manager.getTaskById(taskId), "до обновления задача должна быть старой");

        Task newTask = new Task(taskId, "новая задача", "");

        manager.updateTask(newTask);

        assertTrue(newTask == manager.getTaskById(taskId),
                "после обновления задача должна быть новой");
    }

    // Проверить, что обновляется только ранее сохраненная задача
    public void testThatManagerUpdateOnlyExistingTask() {
        Task task = new Task("задача", "");

        TaskManager manager = new TaskManager();
        manager.saveTask(task);

        int taskId = task.getId();

        Task newTask = new Task("новая задача", "");
        manager.updateTask(newTask);

        assertTrue(task == manager.getTaskById(taskId),
                "после обновления задача должна остаться старой");

        assertEquals(1, manager.getTasks().size(),
                "должна быть одна задача, а не " + manager.getTasks().size());
    }

    // Проверить, что эпик обновляется
    public void testThatManagerUpdateEpic() {
        Epic epic = new Epic("e", "d");

        TaskManager manager = new TaskManager();
        manager.saveEpic(epic);
        int epicId = epic.getId();

        Epic newEpic = new Epic(epicId, "ne", "nd");
        manager.updateEpic(newEpic);

        assertTaskEquals(newEpic, manager.getEpicById(epicId));
    }

    // Проверить, что обновляется только ранее сохраненный эпик
    public void testThatManagerUpdateOnlyExistingEpic() {
        Epic epic = new Epic("e", "d");

        TaskManager manager = new TaskManager();
        manager.saveEpic(epic);

        int epicId = epic.getId();

        Epic newEpic = new Epic("ne", "nd");
        manager.updateEpic(newEpic);

        assertTaskEquals(epic, manager.getEpicById(epicId));

        assertEquals(1, manager.getEpics().size(),
                "должен быть один эпик, а не " + manager.getEpics().size());
    }

    // Проверить обновление подзадачи
    // У родительского эпика новая подзадача тоже должна присутствовать
    public void testThatManagerUpdateSubtask() {
        Epic epic = new Epic("e", "old-d");
        Subtask subtask1 = new Subtask("s1", "old-s1", epic);
        Subtask subtask2 = new Subtask("s2", "old-s2", epic);
        Subtask subtask3 = new Subtask("s3", "old-s3", epic);
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        epic.addSubtask(subtask3);

        TaskManager manager = new TaskManager();
        manager.saveEpic(epic);

        int subtaskId = subtask2.getId();

        Subtask newSubtask = new Subtask(subtaskId, "n", "d", epic);
        manager.updateSubtask(newSubtask);

        // проверяем репозиторий
        assertTaskEquals(newSubtask, manager.getSubtaskById(subtaskId));

        // проверяем подзадачи у конкретного эпика
        List<Subtask> actualSubtasks = manager.getSubtasksByEpic(epic);
        Subtask actualSubtask = findSubtaskById(actualSubtasks, subtaskId);
        System.out.println("actualSubtask = " + actualSubtask);
        assertTaskEquals(newSubtask, actualSubtask);
    }

    // Проверить, что статус эпика обновляется после обновления его подзадачи
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
                String.format("статус эпика должен быть %s, а не %s", TaskStatus.IN_PROGRESS, epic.getStatus()));

        // "выполняем" обе подзадачи
        int id2 = subtask2.getId();
        manager.updateSubtask(new Subtask(id1, "nns1", "nnds1", TaskStatus.DONE, epic));
        manager.updateSubtask(new Subtask(id2, "nns2", "nnds2", TaskStatus.DONE, epic));

        // статус эпика должен стать DONE
        assertEquals(TaskStatus.DONE, epic.getStatus(),
                String.format("статус эпика должен быть %s, а не %s", TaskStatus.DONE, epic.getStatus()));
    }

    // удаление всех обычных задач
    public void testThatManagerRemoveAllTasks() {
        ArrayList<Task> tasks = createSampleTasks(3);

        TaskManager manager = new TaskManager();
        manager.saveTask(tasks.get(0));
        manager.saveTask(tasks.get(1));
        manager.saveTask(tasks.get(2));

        manager.removeTasks();

        assertEquals(0, manager.getTasks().size(), "После удаления не должно быть задач");
    }

    // Удаление обычной задачи по идентификатору
    public void testThatManagerRemoveTaskById() {
        List<Task> tasks = createSampleTasks(3);

        TaskManager manager = new TaskManager();
        manager.saveTask(tasks.get(0));
        manager.saveTask(tasks.get(1));
        manager.saveTask(tasks.get(2));

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



    // Удаление всех эпиков
    // Все подзадачи также должны удалиться
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

    // После удаления всех подзадач у всех эпиков должен обновиться статус на NEW
    public void testThatEpicStatusChangesToNewAfterAllSubtasksRemoval() {
        // У этого эпика статус DONE
        Epic epic1 = new Epic("эпик1", "");
        Subtask subtask11 = new Subtask("подзадача11", "", TaskStatus.DONE, epic1);
        Subtask subtask12 = new Subtask("подзадача12", "", TaskStatus.DONE, epic1);
        epic1.addSubtask(subtask11);
        epic1.addSubtask(subtask12);

        // У этого эпика статус IN_PROGRESS
        Epic epic2 = new Epic("эпик2", "");
        Subtask subtask21 = new Subtask("подзадача21", "", TaskStatus.IN_PROGRESS, epic2);
        epic1.addSubtask(subtask21);

        // У этого эпика статус NEW
        Epic epic3 = new Epic("эпик1", "");

        TaskManager manager = new TaskManager();
        manager.saveEpic(epic1);
        manager.saveEpic(epic2);
        manager.saveEpic(epic3);

        assertEquals(3, manager.getEpics().size(), "Должно сохраниться 3 эпика");

        manager.removeSubtasks();

        assertEquals(TaskStatus.NEW, epic1.getStatus(), "У эпика 1 должен быть статус NEW");
        assertEquals(TaskStatus.NEW, epic2.getStatus(), "У эпика 2 должен быть статус NEW");
        assertEquals(TaskStatus.NEW, epic3.getStatus(), "У эпика 3 должен быть статус NEW");

    }

    // Обновление статуса эпика после удаления всех его подзадач
    // Должен стать NEW
    private void testThatEpicStatusUpdatedAfterSubtaskRemoval() {

    }

    private ArrayList<Task> createSampleTasks(int taskCount) {
        ArrayList<Task> tasks = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) {
            tasks.add(new Task("задача " + i, "описание задачи " + i));
        }
        return tasks;
    }

    private Epic createSampleEpic(String epicName, int subtaskCount) {

        Epic epic = new Epic(epicName, "описание " + epicName);

        for (int i = 0; i < subtaskCount; i++) {
            String subtaskName = epicName + ", подзадача " + i;
            String subtaskDesc = "описание подзадачи " + i + " эпика " + epicName;
            Subtask subtask = new Subtask(subtaskName, subtaskDesc, epic);
            epic.addSubtask(subtask);
        }
        return epic;
    }

    private <T> boolean areListEqualOrderIgnored(Collection<T> list1, Collection<T> list2) {
        return list1.containsAll(list2) && list2.containsAll(list1);
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

    private void assertTaskEquals(Subtask expectedSubtask, Subtask actualSubtask) {
        assertTaskEquals((Task)expectedSubtask, (Task)actualSubtask);

        assertEquals(expectedSubtask.getEpic(), actualSubtask.getEpic(),
                String.format("эпик должен быть %s, а не %s", expectedSubtask.getEpic(), actualSubtask.getEpic()));
    }

    private void assertTaskEquals(Epic expectedEpic, Epic actualEpic) {
        assertTaskEquals((Task)expectedEpic, (Task)actualEpic);

        List<Subtask> expectedSubtasks = expectedEpic.getSubtasks();
        List<Subtask> actualSubtasks = actualEpic.getSubtasks();
        assertEquals(expectedSubtasks.size(), actualSubtasks.size(), String.format(
                "количество подзадач должно быть %s, а не %s", expectedSubtasks.size(), actualSubtasks.size()));

        for (int i = 0; i < expectedSubtasks.size(); i++) {
            assertTaskEquals(expectedSubtasks.get(i), actualSubtasks.get(i));
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