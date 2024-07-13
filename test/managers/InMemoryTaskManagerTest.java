package managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static lib.TestAssertions.*;
import static tasks.TaskStatus.*;

class InMemoryTaskManagerTest {

    TaskManager manager;

    @BeforeEach
    void setup() {
        manager = Managers.getDefault();
    }

    @Nested
    @DisplayName("При сохранении")
    class WhenSaving {
        @Test
        @DisplayName("обычная задача получает id и статус NEW")
        void testThatMangerSavesTask() {
            Task task = new Task("t", "dt");
            int id = manager.saveTask(task);

            assertEquals(id, task.getId(), "После сохранения id должен быть определен");
            assertEquals(task.getStatus(), NEW, "После сохранения должен остаться статус NEW");
        }

        @Test
        @DisplayName("эпик получает id и статус NEW")
        void testThatManagerSavesEpic() {
            Epic epic = new Epic("e", "d");
            int id = manager.saveEpic(epic);

            assertEquals(id, epic.getId(), "После сохранения id должен быть определен");
            assertEquals(epic.getStatus(), NEW, "После сохранения должен остаться статус NEW");
        }

        @Test
        @DisplayName("подзадача получает id и статус NEW")
        void testThatManagerSavesSubtask() {

            Epic epic = new Epic("epic", "desc of epic");
            manager.saveEpic(epic);

            Subtask subtask = new Subtask("sub", "desc of sub", epic);
            int id = manager.saveSubtask(subtask);

            assertEquals(id, subtask.getId(), "После сохранения id должен быть определен");
            assertEquals(subtask.getStatus(), NEW, "После сохранения должен остаться статус NEW");
        }

        @Test
        @DisplayName("id разных задач отличаются")
        void testThatDifferentTasksHaveDifferentId() {

            int id1 = manager.saveTask(new Task("task1", "desc of task1"));
            int id2 = manager.saveTask(new Task("task2", "desc of task2"));

            Epic epic1 = new Epic("epic1", "desc of epic1");
            int id3 = manager.saveEpic(epic1);
            Epic epic2 = new Epic("epic2", "desc of epic2");
            int id4 = manager.saveEpic(epic2);

            int id5 = manager.saveSubtask(new Subtask("sub1 of epic1", "d11", epic1));
            int id6 = manager.saveSubtask(new Subtask("sub2 of epic1", "d12", epic1));
            int id7 = manager.saveSubtask(new Subtask("sub1 of epic2", "d21", epic2));

            Set<Integer> ids = Set.of(id1, id2, id3, id4, id5, id6, id7);

            assertEquals(7, ids.size(), "идентификаторы разных задач должны отличаться");
        }

        @Test
        @DisplayName("статус у эпика с подзадачами будет NEW")
        public void testThatEpicWithSubtasksIsNewAfterSaving() {

            // создаем эпик с подзадачами
            Epic epic = new Epic("e", "d");
            manager.saveEpic(epic);

            Subtask sub1 = new Subtask("s1", "ds1", epic);
            int id1 = manager.saveSubtask(sub1);

            Subtask sub2 = new Subtask("s2", "ds2", epic);
            manager.saveSubtask(sub2);

            assertEquals(NEW, epic.getStatus());
        }
    }

    @Nested
    @DisplayName("При получении")
    class WhenGetting {

        @Test
        @DisplayName("возвращаются все сохраненные обычные задачи")
        void testThatManagerReturnsAllTasks() {

            Task task1 = new Task("task1", "desc of task1");
            manager.saveTask(task1);

            Task task2 = new Task("task2", "desc of task2");
            manager.saveTask(task2);

            Set<Task> expectedTasks = Set.of(task1, task2);
            Set<Task> actualTasks = Set.copyOf(manager.getTasks());

            assertEquals(expectedTasks, actualTasks);
        }

        @Test
        @DisplayName("возвращается обычная задача по id")
        void testThatManagerReturnsTaskById() {

            Task task1 = new Task("task1", "desc of task1");
            int id1 = manager.saveTask(task1);

            Task task2 = new Task("task2", "desc of task2");
            int id2 = manager.saveTask(task2);

            assertEquals(task1, manager.getTaskById(id1), "Должна вернуться первая задача");
            assertEquals(task2, manager.getTaskById(id2), "Должна вернуться вторая задача");
        }

        @Test
        @DisplayName("возвращаются все эпики")
        void testThatManagerReturnAllEpics() {

            Epic epic1 = new Epic("e1", "d1");
            manager.saveEpic(epic1);

            Epic epic2 = new Epic("e2", "d2");
            manager.saveEpic(epic2);

            Set<Epic> expectedEpics = Set.of(epic1, epic2);
            Set<Epic> actualEpics = Set.copyOf(manager.getEpics());

            assertEquals(expectedEpics, actualEpics);
        }

        @Test
        @DisplayName("возвращается эпик по id")
        public void testThatManagerReturnEpicById() {

            Epic epic1 = new Epic("e1", "d1");
            int id1 = manager.saveEpic(epic1);

            Epic epic2 = new Epic("e2", "d2");
            int id2 = manager.saveEpic(epic2);

            assertEquals(epic1, manager.getEpicById(id1), "должен вернуться первый эпик");
            assertEquals(epic2, manager.getEpicById(id2), "должен вернуться второй эпик");
        }

        @Test
        @DisplayName("возвращаются все подзадачи")
        public void testThatManagerReturnAllSubtasks() {

            manager.saveEpic(new Epic("epic1", "desc of epic1"));
            manager.saveEpic(new Epic("epic2", "desc of epic2"));
            List<Epic> epics = manager.getEpics();

            Subtask sub1 = new Subtask("sub1 of epic1", "d11", epics.get(0));
            manager.saveSubtask(sub1);

            Subtask sub2 = new Subtask("sub2 of epic1", "d12", epics.get(0));
            manager.saveSubtask(sub2);

            Subtask sub3 = new Subtask("sub1 of epic2", "d21", epics.get(1));
            manager.saveSubtask(sub3);

            Set<Subtask> expectedSubtasks = Set.of(sub1, sub2, sub3);
            Set<Subtask> actualSubtask = Set.copyOf(manager.getSubtasks());

            assertEquals(expectedSubtasks, actualSubtask);
        }

        @Test
        @DisplayName("возвращается подзадача по id")
        public void testThatManagerReturnsSubtaskById() {

            manager.saveEpic(new Epic("epic1", "desc of epic1"));
            manager.saveEpic(new Epic("epic2", "desc of epic2"));
            List<Epic> epics = manager.getEpics();

            Subtask sub1 = new Subtask("sub1 of epic1", "d11", epics.get(0));
            int id1 = manager.saveSubtask(sub1);

            Subtask sub2 = new Subtask("sub2 of epic1", "d12", epics.get(0));
            int id2 = manager.saveSubtask(sub2);

            Subtask sub3 = new Subtask("sub1 of epic2", "d21", epics.get(1));
            int id3 = manager.saveSubtask(sub3);

            assertEquals(sub1, manager.getSubtaskById(id1), "должна вернуться первая подзадача");
            assertEquals(sub2, manager.getSubtaskById(id2), "должна вернуться вторая подзадача");
            assertEquals(sub3, manager.getSubtaskById(id3), "должна вернуться третья подзадача");
        }

        @Test
        @DisplayName("возвращается эпик для определенной подзадачи")
        void testThatManagerReturnsEpicOfSubtask() {
            Epic epic1 = new Epic("epic1", "desc of epic1");
            manager.saveEpic(epic1);

            Epic epic2 = new Epic("epic2", "desc of epic2");
            manager.saveEpic(epic2);

            Subtask sub1 = new Subtask("sub1 of epic1", "d11", epic1);
            manager.saveSubtask(sub1);

            Subtask sub2 = new Subtask("sub2 of epic1", "d12", epic1);
            manager.saveSubtask(sub2);

            Subtask sub3 = new Subtask("sub1 of epic2", "d21", epic2);
            manager.saveSubtask(sub3);

            assertEquals(epic1, manager.getEpicOfSubtask(sub1), "должен вернуться первый эпик для первой подзадачи");
            assertEquals(epic1, manager.getEpicOfSubtask(sub2), "должен вернуться первый эпик для второй подзадачи");
            assertEquals(epic2, manager.getEpicOfSubtask(sub3), "должен вернуться второй эпик для третьей подзадачи");
        }

        @Test
        @DisplayName("возвращаются все подзадачи у эпика")
        public void testThatManagerReturnsEpicSubtasks() {

            Epic epic1 = new Epic("epic1", "desc of epic1");
            manager.saveEpic(epic1);

            Epic epic2 = new Epic("epic2", "desc of epic2");
            manager.saveEpic(epic2);

            Subtask sub1 = new Subtask("sub1 of epic1", "d11", epic1);
            manager.saveSubtask(sub1);

            Subtask sub2 = new Subtask("sub2 of epic1", "d12", epic1);
            manager.saveSubtask(sub2);

            Subtask sub3 = new Subtask("sub1 of epic2", "d21", epic2);
            manager.saveSubtask(sub3);

            List<Subtask> actualSubtasks = manager.getSubtasksOfEpic(epic1);
            List<Subtask> expectedSubtasks = List.of(sub1, sub2);

            assertIterableEquals(expectedSubtasks, actualSubtasks,
                    String.format("у первого эпика подзадачи должны быть %s, а не %s", expectedSubtasks, actualSubtasks));

            actualSubtasks = manager.getSubtasksOfEpic(epic2);
            expectedSubtasks = List.of(sub3);

            assertIterableEquals(expectedSubtasks, actualSubtasks,
                    String.format("у второго эпика подзадачи должны быть %s, а не %s", expectedSubtasks, actualSubtasks));
        }

    }

    @Nested
    @DisplayName("При обновлении")
    class WhenUpdating{

        @Test
        @DisplayName("обычная задача может изменить имя, описание и статус")
        public void testThatManagerUpdateTask() {

            int id = manager.saveTask(new Task("task", "desc", NEW));

            Task newTask = new Task(id, "updated task", "updated desc", DONE);
            manager.updateTask(newTask);

            Task actTask = manager.getTaskById(id);
            assertEquals(newTask.getName(), actTask.getName(), String.format(
                    "имя обновленной задачи должно быть %s, а не %s", newTask.getName(), actTask.getName()));

            assertEquals(newTask.getDescription(), actTask.getDescription(), String.format(
                    "описание обновленной задачи должно быть %s, а не %s", newTask.getDescription(), actTask.getDescription()));

            assertEquals(newTask.getStatus(), actTask.getStatus(), String.format(
                    "статус обновленной задачи должен быть %s, а не %s", newTask.getStatus(), actTask.getStatus()));
        }

        @Test
        @DisplayName("можно обновить только существующую задачу")
        void testThatOnlyExistingTaskCanBeUpdated() {
            Task newTask = new Task(1, "updated task", "updated desc");
            manager.updateTask(newTask);

            assertNull(manager.getTaskById(1));
        }

        @Test
        @DisplayName("эпик может изменить имя, описание, статус и список подзадач")
        public void testThatManagerUpdateEpic() {

            Epic epic = new Epic("epic", "desc");
            int id = manager.saveEpic(epic);
            manager.saveSubtask(new Subtask("sub1", "desc of sub1", DONE, epic));
            manager.saveSubtask(new Subtask("sub2", "desc of sub1", DONE, epic));

            Epic newEpic = new Epic(id, "new epic", "new desc");
            manager.saveSubtask(new Subtask("new sub1", "desc of new sub1", newEpic));
            manager.saveSubtask(new Subtask("new sub2", "desc of new sub2", newEpic));
            manager.saveSubtask(new Subtask("new sub3", "desc of new sub3", newEpic));
            manager.updateEpic(newEpic);

            Epic actEpic = manager.getEpicById(id);

            assertEquals(newEpic.getName(), actEpic.getName(), String.format(
                    "имя обновленного эпика должно быть %s, а не %s", newEpic.getName(), actEpic.getName()));

            assertEquals(newEpic.getDescription(), actEpic.getDescription(), String.format(
                    "описание обновленного эпика должно быть %s, а не %s", newEpic.getDescription(), actEpic.getDescription()));

            assertEquals(newEpic.getStatus(), actEpic.getStatus(), String.format(
                    "статус обновленного эпика должен быть %s, а не %s", newEpic.getStatus(), actEpic.getStatus()));

            assertIterableEquals(newEpic.getSubtasksId(), actEpic.getSubtasksId(), String.format(
                    "подзадачи обновленного эпика должны быть %s, а не %s", newEpic.getSubtasksId(), actEpic.getSubtasksId()));
        }

        @Test
        @DisplayName("можно обновить только существующий эпик")
        void testThatOnlyExistingEpicCanBeUpdated() {
            Epic epic = new Epic(1, "updated epic", "updated epic");
            manager.updateEpic(epic);

            assertNull(manager.getEpicById(1));
        }

        @Test
        @DisplayName("подзадача может изменить имя, описание и статус")
        void updateSubtask() {
            Epic epic = new Epic("epic", "desc");
            int id = manager.saveEpic(epic);
            int subId = manager.saveSubtask(new Subtask("sub", "desc of sub", NEW, epic));

            Subtask newSub = new Subtask(subId, "new sub", "new desc", NEW, epic);
            manager.updateSubtask(newSub);

            Subtask actSub = manager.getSubtaskById(subId);

            assertEquals(newSub.getName(), actSub.getName(), String.format(
                    "имя обновленного эпика должно быть %s, а не %s", newSub.getName(), actSub.getName()));

            assertEquals(newSub.getDescription(), actSub.getDescription(), String.format(
                    "описание обновленного эпика должно быть %s, а не %s", newSub.getDescription(), actSub.getDescription()));

            assertEquals(newSub.getStatus(), actSub.getStatus(), String.format(
                    "статус обновленного эпика должен быть %s, а не %s", newSub.getStatus(), actSub.getStatus()));
        }

        @Test
        @DisplayName("можно обновить только существующую подзадачу")
        void testThatOnlyExistingSubtaskCanBeUpdated() {
            Epic epic = new Epic("epic", "desc");
            Subtask sub = new Subtask(1, "updated sub", "desc of updated sub", epic);
            manager.updateSubtask(sub);

            assertNull(manager.getSubtaskById(1));
        }

        @Test
        @DisplayName("можно обновить только подзадачу с существующим эпиком")
        void testThatOnlySubtaskWithExistingEpicCanBeUpdated() {
            Epic epic = new Epic("epic", "desc");
            manager.saveEpic(epic);

            Subtask sub = new Subtask("sub", "desc of sub", epic);
            manager.saveSubtask(sub);

            manager.removeEpics();

            Subtask newSub = new Subtask(sub.getId(), "new sub", "desc of new sub", epic);
            manager.updateSubtask(sub);

            assertNull(manager.getSubtaskById(sub.getId()));
        }

        @Test
        @DisplayName("подзадача не может изменить своего эпика")
        void testThatSubtaskCannotChangeItEpic() {
            Epic epic = new Epic("epic", "desc");
            manager.saveEpic(epic);

            Subtask sub = new Subtask("sub", "desc of sub", epic);
            manager.saveSubtask(sub);

            Epic newEpic = new Epic("new epic", "new desc");
            manager.saveEpic(newEpic);

            Subtask newSub = new Subtask(sub.getId(), sub.getName(), sub.getDescription(), newEpic);
            manager.updateSubtask(sub);

            Subtask actSubtask = manager.getSubtaskById(sub.getId());
            Epic actEpic = manager.getEpicOfSubtask(actSubtask);

            assertEquals(epic, actEpic);
        }

        @Test
        @DisplayName("у эпика возвращается обновленная версия подзадачи")
        public void testThatEpicContainsUpdatedSubtask() {
            Epic epic = new Epic("epic", "desc");
            int id = manager.saveEpic(epic);
            int subId = manager.saveSubtask(new Subtask("sub", "desc of sub", NEW, epic));

            Subtask newSub = new Subtask(subId, "new sub", "new desc", NEW, epic);
            manager.updateSubtask(newSub);

            List<Subtask> subtasks = manager.getSubtasksOfEpic(epic);
            Subtask actSub = subtasks.getFirst();

            assertEquals(newSub.getName(), actSub.getName(), String.format(
                    "имя обновленного эпика должно быть %s, а не %s", newSub.getName(), actSub.getName()));

            assertEquals(newSub.getDescription(), actSub.getDescription(), String.format(
                    "описание обновленного эпика должно быть %s, а не %s", newSub.getDescription(), actSub.getDescription()));

            assertEquals(newSub.getStatus(), actSub.getStatus(), String.format(
                    "статус обновленного эпика должен быть %s, а не %s", newSub.getStatus(), actSub.getStatus()));
        }

        @Test
        @DisplayName("подзадач у их эпика, его статус обновляется")
        public void testThatEpicStatusUpdatedAfterSubtaskUpdate() {

            Epic epic = createAndSaveEpicWithSubtasks(2);
            List<Subtask> subtasks = manager.getSubtasksOfEpic(epic);
            Subtask sub1 = subtasks.get(0);
            Subtask sub2 = subtasks.get(1);

            // берем в работу
            sub1.setStatus(IN_PROGRESS);
            manager.updateSubtask(sub1);

            assertEquals(IN_PROGRESS, epic.getStatus(),
                    String.format("статус эпика должен быть IN_PROGRESS, а не %s", epic.getStatus()));

            // отмечаем выполненными
            sub1.setStatus(DONE);
            sub2.setStatus(DONE);
            manager.updateSubtask(sub1);
            manager.updateSubtask(sub2);

            assertEquals(DONE, epic.getStatus(),
                    String.format("статус эпика должен быть DONE, а не %s", epic.getStatus()));
        }


    }

    @Nested
    @DisplayName("При удалении")
    class WhenDeleting {

        @Test
        @DisplayName("обычных задач, менеджер их больше не возвращает")
        public void testThatManagerRemoveAllTasks() {
            manager.saveTask(new Task("task1", "desc1"));
            manager.saveTask(new Task("task2", "desc2"));

            manager.removeTasks();

            assertEmpty(manager.getTasks());
        }

        @Test
        @DisplayName("обычной задачи по id, менеджер ее больше не возвращает")
        void removeTaskById() {
            Task task1 = new Task("task1", "desc1");
            manager.saveTask(task1);

            Task task2 = new Task("task2", "desc2");
            int id2 = manager.saveTask(task2);

            manager.removeTaskById(id2);

            assertIterableEquals(List.of(task1), manager.getTasks());
        }

        @Test
        @DisplayName("всех эпиков, менеджер больше не возвращает эпиков и подзадач")
        public void testThatManagerRemoveAllEpics() {

            createAndSaveEpicWithSubtasks(2);
            createAndSaveEpicWithSubtasks(3);

            manager.removeEpics();

            assertEmpty(manager.getEpics(), "После удаления не должно быть эпиков");
            assertEmpty(manager.getSubtasks(), "После удаления не должно быть подзадач");
        }

        @Test
        @DisplayName("эпика по id, менеджер больше не возвращает его и его подзадачи")
        public void testThatManagerRemoveEpicById() {

            Epic epic1 = createAndSaveEpicWithSubtasks(2);
            List<Subtask> subtasks1 = manager.getSubtasksOfEpic(epic1);

            Epic epic2 = createAndSaveEpicWithSubtasks(3);
            List<Subtask> subtasks2 = manager.getSubtasksOfEpic(epic2);

            Set<Epic> expectedEpics = Set.of(epic1);
            Set<Subtask> expectedSubtasks = Set.copyOf(subtasks1);

            manager.removeEpicById(epic2.getId());

            Set<Epic> actualEpics = Set.copyOf(manager.getEpics());
            Set<Subtask> actualSubtasks = Set.copyOf(manager.getSubtasks());

            assertIterableEquals(expectedEpics, actualEpics,
        "должен остаться только первый эпик");

            assertIterableEquals(expectedSubtasks, actualSubtasks,
        "должны остаться только подзадачи первого эпика");
        }

        @Test
        @DisplayName("всех подзадач, менеджер больше их не возвращает")
        public void testThatManagerRemoveAllSubtasks() {
            Epic epic1 = createAndSaveEpicWithSubtasks(2);
            Epic epic2 = createAndSaveEpicWithSubtasks(3);

            manager.removeSubtasks();

            assertEmpty(manager.getSubtasks(), "После удаления не должно быть подзадач");
            assertEmpty(epic1.getSubtasksId(), "После удаления у первого эпика не должно быть подзадач");
            assertEmpty(epic2.getSubtasksId(), "После удаления у второго эпика не должно быть подзадач");
        }

        @Test
        @DisplayName("подзадачи по id, менеджер ее больше не возвращает")
        public void testThatManagerRemoveSubtaskById() {

            Epic epic1 = createAndSaveEpicWithSubtasks(2);
            Epic epic2 = createAndSaveEpicWithSubtasks(3);

            List<Subtask> subtasks1 = manager.getSubtasksOfEpic(epic1);
            List<Subtask> subtasks2 = manager.getSubtasksOfEpic(epic2);

            Set<Subtask> expectedSubtasks = Set.of(subtasks1.get(0), subtasks1.get(1),
                    subtasks2.get(0), subtasks2.get(2));

            // удаляем вторую подзадачу второго эпика
            manager.removeSubtaskById(subtasks2.get(1).getId());

            Set<Subtask> actualSubtasks = Set.copyOf(manager.getSubtasks());

            // остальные должны сохраниться в общем списке ...
            assertIterableEquals(expectedSubtasks, actualSubtasks);

            // ... и у эпика
            assertIterableEquals(List.of(subtasks2.get(0), subtasks2.get(2)),
                    manager.getSubtasksOfEpic(epic2));
        }

        @Test
        @DisplayName("подзадачи у эпика, его статус должен обновиться")
        public void testThatEpicStatusUpdatedAfterSubtaskRemoval() {

            Epic epic = createAndSaveEpicWithSubtasks(2);
            List<Subtask> subtasks = manager.getSubtasksOfEpic(epic);
            Subtask sub1 = subtasks.get(0);
            Subtask sub2 = subtasks.get(1);

            sub1.setStatus(IN_PROGRESS);
            sub2.setStatus(DONE);
            manager.updateSubtask(sub1);
            manager.updateSubtask(sub2);

            assertEquals(IN_PROGRESS, epic.getStatus(),
                String.format("до удаления подзадач статус эпика должен быть IN_PROGRESS, а не %s", epic.getStatus()));

            manager.removeSubtaskById(sub1.getId());

            assertEquals(DONE, epic.getStatus(), String.format(
                "когда остались только подзадачи со статусом DONE, у эпика статус должен быть DONE, а не %s",
                    epic.getStatus()));

            manager.removeSubtaskById(sub2.getId());

            assertEquals(NEW, epic.getStatus(), String.format(
                    "после удаления всех подзадач, у эпика статус должен быть NEW, а не %s", epic.getStatus()));

        }

        @Test
        @DisplayName("всех подзадач у эпика, его статус должен стать NEW")
        public void testThatEpicStatusIsNewAfterAllSubtaskRemoval() {

            // у этого эпика статус DONE
            Epic epic = createAndSaveEpicWithSubtasks(1);
            manager.getSubtasksOfEpic(epic).get(0).setStatus(DONE);

            manager.removeSubtasks();

            assertEquals(TaskStatus.NEW, epic.getStatus(),
                    String.format("статус эпика должен быть NEW, а не %s", epic.getStatus()));
        }

        @Test
        @DisplayName("эпика, попытка сохранить его подзадачу игнорируется")
        public void testThatSavingSubtaskOfDeletedEpicIsIgnored() {

            Epic epic = new Epic("epic", "desc");
            manager.saveEpic(epic);

            Subtask sub = new Subtask("sub", "desc of sub", epic);

            manager.removeEpics();
            manager.saveSubtask(sub);

            assertEmpty(manager.getSubtasks(), "подзадач не должно существовать");
        }

        @Test
        @DisplayName("эпика, попытка обновить его подзадачу игнорируется")
        public void testThatUpdatingSubtaskOfDeletedEpicIsIgnored() {

            Epic epic = createAndSaveEpicWithSubtasks(1);
            Subtask sub = manager.getSubtasksOfEpic(epic).get(0);

            manager.removeEpics();

            Subtask newSub = new Subtask(sub.getId(), "sub2", "desc2", epic);
            manager.updateSubtask(newSub);

            assertEmpty(manager.getSubtasks(), "подзадач не должно существовать");
        }

        @Test
        @DisplayName("эпика, попытка удалить его подзадачу игнорируется")
        public void testThatDeletingSubtaskOfDeletedEpicIsIgnored() {
            Epic epic = createAndSaveEpicWithSubtasks(1);
            Subtask sub = manager.getSubtasksOfEpic(epic).get(0);

            manager.removeEpics();

            manager.removeSubtaskById(sub.getId());
            assertEmpty(manager.getSubtasks(), "подзадач не должно существовать");
        }
    }

    @Nested
    @DisplayName("При получении истории")
    class WhenGetHistory {

        @Test
        @DisplayName("до просмотра задач, история должна быть пустой")
        public void testThatHistoryIsEmptyBeforeTaskWatching() {

            createAndSaveEpicWithSubtasks(2);
            createAndSaveTask("Обычная задача", "Описание обычной задачи");

            List<Task> history = manager.getHistory();
            assertEmpty(history, "история просмотров должна быть пустой");
        }

        // Проверить, что после просмотра задач разного типа возвращается история просмотров
        @Test
        @DisplayName("после просмотра разных задач, должна вернуться история в порядке просмотров")
        public void testThatManagerReturnsCorrectHistoryAfterTaskWatching() {
            Epic epic = createAndSaveEpicWithSubtasks(2);
            List<Subtask> subtasks = manager.getSubtasksOfEpic(epic);
            Task task = createAndSaveTask("Обычная задача", "Описание обычной задачи");

            List<Task> history;
            List<Task> expectedHistory = new ArrayList<>();

            // смотрим обычную задачу
            manager.getTaskById(task.getId());
            expectedHistory.add(task);

            history = manager.getHistory();

            assertIterableEquals(expectedHistory, history, String.format(
                "история должна быть %s, а не %s", expectedHistory, history));

            // смотрим первую подзадачу
            manager.getSubtaskById(subtasks.get(0).getId());
            expectedHistory.add(subtasks.get(0));

            history = manager.getHistory();

            assertIterableEquals(expectedHistory, history, String.format(
                "история должна быть %s, а не %s", expectedHistory, history));

            // смотрим эпик
            manager.getEpicById(epic.getId());
            expectedHistory.add(epic);

            history = manager.getHistory();

            assertIterableEquals(expectedHistory, history, String.format(
                "история должна быть %s, а не %s", expectedHistory, history));

            // смотрим вторую подзадачу
            manager.getSubtaskById(subtasks.get(1).getId());
            history = manager.getHistory();

            expectedHistory.add(subtasks.get(1));

            assertIterableEquals(expectedHistory, history, String.format(
                "история должна быть %s, а не %s", expectedHistory, history));
        }

        @Test
        @DisplayName("задачи сохраняют предыдущую версию задачи и ее данных")
        public void testThatEvenAfterTaskUpdateHistoryHoldsOldStateOfTask() {

            Task task = createAndSaveTask("task", "desc");

            manager.getTaskById(task.getId());

            int id = task.getId();
            String oldName = task.getName();
            String oldDescription = task.getDescription();
            TaskStatus oldStatus = task.getStatus();

            // меняем task
            Task newTask = new Task(id, "new task", "new desc", TaskStatus.DONE);
            manager.updateTask(newTask);

            Task historyTask = manager.getHistory().get(0);

            assertEquals(oldStatus, historyTask.getStatus(),
                    String.format("статус должен быть %s, а не %s", oldStatus, historyTask.getStatus()));

            assertEquals(oldName, historyTask.getName(),
                    String.format("имя должно быть %s, а не %s", oldName, historyTask.getName()));

            assertEquals(oldDescription, historyTask.getDescription(),
                    String.format("описание должно быть %s, а не %s", oldDescription, historyTask.getDescription()));

            assertEquals(id, historyTask.getId(),
                    String.format("id должно быть %s, а не %s", id, historyTask.getDescription()));
        }
    }

    private Task createAndSaveTask(String name, String desc) {
        Task task = new Task(name, desc);
        manager.saveTask(task);
        return task;
    }

    private Epic createAndSaveEpicWithSubtasks(int subtaskCount) {

        Epic epic = new Epic("e", "d");
        manager.saveEpic(epic);

        for (int i = 0; i < subtaskCount; i++) {
            Subtask sub = new Subtask("sub of e " + i, "desc of sub " + i, epic);
            manager.saveSubtask(sub);
        }

        return epic;
    }


}