package managers;

import exceptions.ManagerSaveException;
import org.junit.jupiter.api.*;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static lib.TestAssertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static tasks.TaskStatus.*;

class InMemoryTaskManagerTest {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    TaskManager manager;

    @BeforeEach
    void setup() {
        manager = Managers.getDefault();
    }

    @Nested
    @DisplayName("При сохранении")
    class WhenSaving {

        @Test
        @DisplayName("обычная задача не сохраняется, если duration не положителный")
        public void testThatManagerDoesNotSaveTaskWithNonPositiveDuration(){
            Task task = new Task("task", "desc", parseTime("2024-01-01 01:02:03"), Duration.ofMinutes(-1));
            assertThrows(ManagerSaveException.class, () -> manager.saveTask(task));
        }

        @Test
        @DisplayName("обычная задача получает id и статус NEW")
        public void testThatMangerSavesTask() {
            Task task = new Task("t", "dt", null, null);
            int id = manager.saveTask(task);

            assertEquals(id, task.getId(), "После сохранения id должен быть определен");
            assertEquals(task.getStatus(), NEW, "После сохранения должен остаться статус NEW");
        }

        @Test
        @DisplayName("обычная задача не изменяет имени и описания")
        public void testThatTaskDoesNotChangeNameAndDescAfterSaving() {
            Task task = new Task("task", "desc", null, null);
            manager.saveTask(task);

            Task actTask = manager.getTaskById(task.getId());

            assertEquals("task", actTask.getName(), "После сохранения имя задачи не должно меняться");
            assertEquals("desc", actTask.getDescription(), "После сохранения описание задачи не должно меняться");
        }

        @Test
        @DisplayName("эпик получает id и статус NEW")
        public void testThatManagerSavesEpic() {
            Epic epic = new Epic("e", "d");
            int id = manager.saveEpic(epic);

            assertEquals(id, epic.getId(), "После сохранения id должен быть определен");
            assertEquals(epic.getStatus(), NEW, "После сохранения должен остаться статус NEW");
        }

        @Test
        @DisplayName("эпик не изменяет имени, описания")
        public void testThatEpicDoesNotChangeNameAndDescAfterSaving() {
            int id = manager.saveEpic(new Epic("epic", "desc"));

            Epic actEpic = manager.getEpicById(id);

            assertEquals("epic", actEpic.getName(), "После сохранения имя задачи не должно меняться");
            assertEquals("desc", actEpic.getDescription(), "После сохранения описание задачи не должно меняться");
        }

        @Test
        @DisplayName("эпик не сохраняется, если duration не положителный")
        public void testThatManagerDoesNotSaveEpicWithNonPositiveDuration(){
            Epic epic = new Epic("epic", "desc", parseTime("2024-01-01 01:02:03"), Duration.ofMinutes(-1));
            assertThrows(ManagerSaveException.class, () -> manager.saveEpic(epic));
        }

        @Test
        @DisplayName("подзадача получает id и статус NEW")
        public void testThatManagerSavesSubtask() {
            Epic epic = new Epic("epic", "desc of epic");
            manager.saveEpic(epic);

            Subtask subtask = new Subtask("sub", "desc of sub", epic, null, null);
            int id = manager.saveSubtask(subtask);

            assertEquals(id, subtask.getId(), "После сохранения id должен быть определен");
            assertEquals(subtask.getStatus(), NEW, "После сохранения должен остаться статус NEW");
        }

        @Test
        @DisplayName("подзадача не изменяет имени и описания")
        public void testThatSubtaskDoesNotChangeNameAndDescAfterSaving() {
            Epic epic = new Epic("epic", "desc");
            manager.saveEpic(epic);

            Subtask sub = new Subtask("sub", "desc of sub", epic, null, null);
            manager.saveSubtask(sub);

            Subtask actSub = manager.getSubtaskById(sub.getId());

            assertEquals("sub", actSub.getName(), "После сохранения имя подзадачи не должно меняться");
            assertEquals("desc of sub", actSub.getDescription(), "После сохранения описания подзадачи не должно меняться");
        }

        @Test
        @DisplayName("подзадача не сохраняется, если duration не положителный")
        public void testThatManagerDoesNotSaveSubtaskWithNonPositiveDuration(){
            Epic epic = new Epic("epic", "desc");
            manager.saveEpic(epic);
            Subtask sub = new Subtask("sub", "desc", epic, parseTime("2024-01-01 01:02:03"), Duration.ofMinutes(-1));
            assertThrows(ManagerSaveException.class, () -> manager.saveSubtask(sub));
        }

        @Test
        @DisplayName("id разных задач отличаются")
        public void testThatDifferentTasksHaveDifferentId() {

            int id1 = manager.saveTask(new Task("task1", "desc of task1", null, null));
            int id2 = manager.saveTask(new Task("task2", "desc of task2", null, null));

            Epic epic1 = new Epic("epic1", "desc of epic1");
            int id3 = manager.saveEpic(epic1);
            Epic epic2 = new Epic("epic2", "desc of epic2");
            int id4 = manager.saveEpic(epic2);

            int id5 = manager.saveSubtask(new Subtask("sub1 of epic1", "d11", epic1, null, null));
            int id6 = manager.saveSubtask(new Subtask("sub2 of epic1", "d12", epic1, null, null));
            int id7 = manager.saveSubtask(new Subtask("sub1 of epic2", "d21", epic2, null, null));

            Set<Integer> ids = Set.of(id1, id2, id3, id4, id5, id6, id7);

            assertEquals(7, ids.size(), "идентификаторы разных задач должны отличаться");
        }

        @Test
        @DisplayName("статус у нового эпика с подзадачами будет NEW")
        public void testThatEpicWithSubtasksIsNewAfterSaving() {
            Epic epic = createAndSaveEpicWithSubtasks(2);

            assertEquals(NEW, epic.getStatus());
        }

        @Test
        @DisplayName("задачи и подзадачи возвращаются в getPrioritizedTasks")
        public void testThatTasksAndSubtasksArePrioritized() {
            // name;description;status;startTime;duration
            Task task1 = createAndSaveTask("task;desc1;NEW;2024-01-10 01:02:03;123");
            Task task2 = createAndSaveTask("task;desc2;NEW;2024-01-09 02:03:04;234");

            // name;description
            Epic epic1 = createAndSaveEpic("epic;desc3");
            Epic epic2 = createAndSaveEpic("epic;desc4");

            // name;description;status;epicId;startTime;duration
            Subtask sub1 = createAndSaveSubtask("sub1;desc5;NEW;" + epic1.getId() + ";2024-01-08 03:04:05;345");
            Subtask sub2 = createAndSaveSubtask("sub2;desc6;NEW;" + epic1.getId() + ";2024-01-07 04:05:06;456");
            Subtask sub3 = createAndSaveSubtask("sub3;desc7;NEW;" + epic1.getId() + ";null;456");

            List<Task> actualTasks = manager.getPrioritizedTasks();
            List<Task> expectedTasks = List.of(sub2, sub1, task2, task1);

            assertIterableEquals(expectedTasks, actualTasks);
        }
    }

    @Nested
    @DisplayName("При получении")
    class WhenGetting {

        @Test
        @DisplayName("возвращаются все сохраненные обычные задачи")
        void testThatManagerReturnsAllTasks() {

            Task task1 = new Task("task1", "desc of task1", null, null);
            manager.saveTask(task1);

            Task task2 = new Task("task2", "desc of task2", null, null);
            manager.saveTask(task2);

            Set<Task> expectedTasks = Set.of(task1, task2);
            Set<Task> actualTasks = Set.copyOf(manager.getTasks());

            assertEquals(expectedTasks, actualTasks);
        }

        @Test
        @DisplayName("возвращается обычная задача по id")
        void testThatManagerReturnsTaskById() {

            Task task1 = new Task("task1", "desc of task1", null, null);
            int id1 = manager.saveTask(task1);

            Task task2 = new Task("task2", "desc of task2", null, null);
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

            Epic epic1 = createAndSaveEpic();
            Epic epic2 = createAndSaveEpic();

            assertEquals(epic1, manager.getEpicById(epic1.getId()), "должен вернуться первый эпик");
            assertEquals(epic2, manager.getEpicById(epic2.getId()), "должен вернуться второй эпик");
        }

        @Test
        @DisplayName("возвращаются все подзадачи")
        public void testThatManagerReturnAllSubtasks() {

            Epic epic1 = createAndSaveEpic();
            Epic epic2 = createAndSaveEpic();
            Subtask sub1 = createAndSaveSubtask(epic1);
            Subtask sub2 = createAndSaveSubtask(epic1);
            Subtask sub3 = createAndSaveSubtask(epic2);

            Set<Subtask> expectedSubtasks = Set.of(sub1, sub2, sub3);
            Set<Subtask> actualSubtask = Set.copyOf(manager.getSubtasks());

            assertEquals(expectedSubtasks, actualSubtask);
        }

        @Test
        @DisplayName("возвращается подзадача по id")
        public void testThatManagerReturnsSubtaskById() {

            Epic epic1 = createAndSaveEpic();
            Epic epic2 = createAndSaveEpic();
            Subtask sub1 = createAndSaveSubtask(epic1);
            Subtask sub2 = createAndSaveSubtask(epic1);
            Subtask sub3 = createAndSaveSubtask(epic2);

            assertEquals(sub1, manager.getSubtaskById(sub1.getId()), "должна вернуться первая подзадача");
            assertEquals(sub2, manager.getSubtaskById(sub2.getId()), "должна вернуться вторая подзадача");
            assertEquals(sub3, manager.getSubtaskById(sub3.getId()), "должна вернуться третья подзадача");
        }

        @Test
        @DisplayName("возвращается эпик для определенной подзадачи")
        void testThatManagerReturnsEpicOfSubtask() {
            Epic epic1 = createAndSaveEpic();
            Epic epic2 = createAndSaveEpic();
            Subtask sub1 = createAndSaveSubtask(epic1);
            Subtask sub2 = createAndSaveSubtask(epic1);
            Subtask sub3 = createAndSaveSubtask(epic2);

            assertEquals(epic1, manager.getEpicOfSubtask(sub1), "должен вернуться первый эпик для первой подзадачи");
            assertEquals(epic1, manager.getEpicOfSubtask(sub2), "должен вернуться первый эпик для второй подзадачи");
            assertEquals(epic2, manager.getEpicOfSubtask(sub3), "должен вернуться второй эпик для третьей подзадачи");
        }

        @Test
        @DisplayName("возвращаются все подзадачи у эпика")
        public void testThatManagerReturnsEpicSubtasks() {

            Epic epic1 = createAndSaveEpic();
            Epic epic2 = createAndSaveEpic();
            Subtask sub1 = createAndSaveSubtask(epic1);
            Subtask sub2 = createAndSaveSubtask(epic1);
            Subtask sub3 = createAndSaveSubtask(epic2);

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

            int id = manager.saveTask(new Task("task", "desc", NEW, null, null));

            Task newTask = new Task(id, "updated task", "updated desc", DONE, null, null);
            manager.updateTask(newTask);

            Task actualTask = manager.getTaskById(id);

            assertTaskEquals(newTask, actualTask);
        }

        @Test
        @DisplayName("можно обновить только сохраненную задачу")
        void testThatOnlySavedTaskCanBeUpdated() {
            manager.updateTask(new Task("updated task", "updated desc", null, null));

            assertEmpty(manager.getTasks());
        }

        @Test
        @DisplayName("можно обновить только существующую задачу")
        void testThatOnlyExistingTaskCanBeUpdated() {
            manager.updateTask(new Task(1, "updated task", "updated desc", null, null));

            assertNull(manager.getTaskById(1));
        }

        @Test
        @DisplayName("эпик может изменить имя, описание, статус и список подзадач")
        public void testThatManagerUpdateEpic() {

            Epic epic = createAndSaveEpic();
            createAndSaveSubtask(DONE, epic);
            createAndSaveSubtask(DONE, epic);

            Epic newEpic = new Epic(epic.getId(), "new epic", "new desc");
            createAndSaveSubtask(newEpic);
            createAndSaveSubtask(newEpic);
            createAndSaveSubtask(newEpic);
            manager.updateEpic(newEpic);

            Epic actEpic = manager.getEpicById(epic.getId());

            assertEpicEquals(newEpic, actEpic);
        }

        @Test
        @DisplayName("можно обновить только сохраненный эпик")
        void testThatOnlySavedEpicCanBeUpdated() {
            Epic epic = new Epic("updated epic", "updated epic");
            manager.updateEpic(epic);

            assertEmpty(manager.getEpics());
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
            Epic epic = createAndSaveEpic();
            Subtask sub = createAndSaveSubtask(NEW, epic);

            Subtask newSub = new Subtask(sub.getId(), "new sub", "new desc", DONE, epic, null, null);
            manager.updateSubtask(newSub);

            Subtask actualSub = manager.getSubtaskById(sub.getId());
            assertSubtaskEquals(newSub, actualSub);
        }

        @Test
        @DisplayName("можно обновить только сохраненную подзадачу")
        void testThatOnlySavedSubtaskCanBeUpdated() {
            Epic epic = createAndSaveEpic();

            Subtask sub = new Subtask("updated sub", "desc of updated sub", epic, null, null);
            manager.updateSubtask(sub);

            assertEmpty(manager.getSubtasks());
        }

        @Test
        @DisplayName("можно обновить только существующую подзадачу")
        void testThatOnlyExistingSubtaskCanBeUpdated() {
            Epic epic = new Epic("epic", "desc");
            Subtask sub = new Subtask(1, "updated sub", "desc of updated sub", epic, null, null);

            manager.updateSubtask(sub);

            assertNull(manager.getSubtaskById(1));
        }

        @Test
        @DisplayName("можно обновить только подзадачу с существующим эпиком")
        void testThatOnlySubtaskWithExistingEpicCanBeUpdated() {
            Epic epic = createAndSaveEpic();
            Subtask sub = createAndSaveSubtask(epic);

            manager.removeEpics();

            Subtask newSub = new Subtask(sub.getId(), "new sub", "desc of new sub", epic, null, null);
            manager.updateSubtask(newSub);

            assertNull(manager.getSubtaskById(sub.getId()));
        }

        @Test
        @DisplayName("подзадача не может изменить своего эпика")
        void testThatSubtaskCannotChangeItEpic() {
            Epic epic = createAndSaveEpic();
            Subtask sub = createAndSaveSubtask(epic);

            Epic newEpic = createAndSaveEpic();

            Subtask newSub = new Subtask(sub.getId(), sub.getName(), sub.getDescription(), sub.getStatus(), newEpic, null, null);
            manager.updateSubtask(newSub);

            Subtask actualSub = manager.getSubtaskById(sub.getId());
            Epic actualEpic = manager.getEpicOfSubtask(actualSub);

            assertEquals(epic, actualEpic);
        }

        @Test
        @DisplayName("у эпика возвращается обновленная версия подзадачи")
        public void testThatEpicContainsUpdatedSubtask() {
            Epic epic = createAndSaveEpic();
            Subtask sub = createAndSaveSubtask(NEW, epic);

            Subtask newSub = new Subtask(sub.getId(), "new sub", "new desc", NEW, epic, null, null);
            manager.updateSubtask(newSub);

            List<Subtask> subtasks = manager.getSubtasksOfEpic(epic);
            Subtask actualSub = subtasks.getFirst();

            assertSubtaskEquals(newSub, actualSub);
        }

        @Test
        @DisplayName("подзадач у их эпика, его статус обновляется")
        public void testThatEpicStatusUpdatedAfterSubtaskUpdate() {

            Epic epic = createAndSaveEpic();
            Subtask sub1 = createAndSaveSubtask(epic);
            Subtask sub2 = createAndSaveSubtask(epic);

            // берем в работу
            sub1.setStatus(IN_PROGRESS);
            manager.updateSubtask(sub1);

            assertEquals(IN_PROGRESS, epic.getStatus(),
                    String.format("статус эпика должен быть IN_PROGRESS, а не %s", epic.getStatus()));

            // отмечаем выполненными
            sub1.setStatus(DONE);
            manager.updateSubtask(sub1);
            sub2.setStatus(DONE);
            manager.updateSubtask(sub2);

            assertEquals(DONE, epic.getStatus(),
                    String.format("статус эпика должен быть DONE, а не %s", epic.getStatus()));
        }

        @Test
        @DisplayName("подзадач у их эпика, его продолжительность обновляется")
        public void testThatEpicDurationUpdatedAfterSubtaskUpdate() {

            Epic epic = createAndSaveEpic("epic;desc");
            List<Subtask> subs = List.of(
                    createAndSaveSubtask("sub1;desc1;NEW;" + epic.getId() + ";null;60"),
                    createAndSaveSubtask("sub2;desc2;NEW;" + epic.getId() + ";null;120")
            );

            assertEquals(Duration.ofMinutes(180), epic.getDuration(), String.format(
                    "продолжительность должна быть %s, а не %s", Duration.ofMinutes(180), epic.getDuration()));

            subs.getFirst().setDuration(Duration.ofMinutes(30));
            manager.updateSubtask(subs.getFirst());

            assertEquals(Duration.ofMinutes(150), epic.getDuration(), String.format(
                    "продолжительность должна быть %s, а не %s", Duration.ofMinutes(150), epic.getDuration()));
        }

        @Test
        @DisplayName("подзадач у их эпика, его начало обновляется")
        public void testThatEpicStartTimeUpdatedAfterSubtaskUpdate() {

            Epic epic = createAndSaveEpic("epic;desc");
            List<Subtask> subs = List.of(
                    createAndSaveSubtask("sub1;desc1;NEW;" + epic.getId() + ";2024-01-01 00:00:00;60"),
                    createAndSaveSubtask("sub2;desc2;NEW;" + epic.getId() + ";2024-01-02 00:00:00;null"),
                    createAndSaveSubtask("sub3;desc3;NEW;" + epic.getId() + ";null;120"),
                    createAndSaveSubtask("sub4;desc4;NEW;" + epic.getId() + ";2024-01-03 01:00:00;120")
            );

            assertEquals(parseTime("2024-01-01 00:00:00"), epic.getStartTime(),
                    "начало должно быть 2024-01-01 00:00:00, а не " + epic.getStartTime());

            subs.getFirst().setStartTime(parseTime("2024-01-02 00:00:00"));
            manager.updateSubtask(subs.getFirst());

            assertEquals(parseTime("2024-01-02 00:00:00"), epic.getStartTime(),
                    "начало должно быть 2024-01-02 00:00:00, а не " + epic.getStartTime());
        }

        @Test
        @DisplayName("подзадач у их эпика, его окончание обновляется")
        public void testThatEpicEndTimeUpdatedAfterSubtaskUpdate() {

            Epic epic = createAndSaveEpic("epic;desc");
            List<Subtask> subs = List.of(
                    createAndSaveSubtask("sub1;desc1;NEW;" + epic.getId() + ";2024-01-01 00:00:00;60"),
                    createAndSaveSubtask("sub2;desc2;NEW;" + epic.getId() + ";2024-01-02 00:00:00;null"),
                    createAndSaveSubtask("sub3;desc3;NEW;" + epic.getId() + ";null;120"),
                    createAndSaveSubtask("sub4;desc4;NEW;" + epic.getId() + ";2024-01-03 01:00:00;120")
            );

            assertEquals(parseTime("2024-01-03 03:00:00"), epic.getEndTime(),
                    "начало должно быть 2024-01-03 03:00:00, а не " + epic.getEndTime());

            subs.getLast().setStartTime(parseTime("2024-01-04 00:00:00"));
            manager.updateSubtask(subs.getLast());

            assertEquals(parseTime("2024-01-04 02:00:00"), epic.getEndTime(),
                    "начало должно быть 2024-01-04 02:00:00, а не " + epic.getEndTime());

            subs.getLast().setDuration(Duration.ofMinutes(60));
            manager.updateSubtask(subs.getLast());

            assertEquals(parseTime("2024-01-04 01:00:00"), epic.getEndTime(),
                    "начало должно быть 2024-01-04 01:00:00, а не " + epic.getEndTime());
        }

        @Test
        @DisplayName("задачи и подзадачи возвращаются в getPrioritizedTasks")
        public void testThatTasksAndSubtasksAreRePrioritized() {
            // name;description;status;startTime;duration
            Task task1 = createAndSaveTask("task;desc1;NEW;2024-01-10 01:02:03;123");
            Task task2 = createAndSaveTask("task;desc2;NEW;2024-01-09 02:03:04;234");

            // name;description
            Epic epic1 = createAndSaveEpic("epic;desc3");
            Epic epic2 = createAndSaveEpic("epic;desc4");

            // name;description;status;epicId;startTime;duration
            Subtask sub1 = createAndSaveSubtask("sub1;desc5;NEW;" + epic1.getId() + ";2024-01-08 03:04:05;345");
            Subtask sub2 = createAndSaveSubtask("sub2;desc6;NEW;" + epic1.getId() + ";2024-01-07 04:05:06;456");
            Subtask sub3 = createAndSaveSubtask("sub3;desc7;NEW;" + epic1.getId() + ";null;456");

            Subtask newSub2 = new Subtask(sub2.getId(), sub2.getName(), sub2.getDescription(), sub2.getStatus(),
                    sub2.getEpicId(), parseTime("2024-01-08 10:00:00"), sub2.getDuration());
            manager.updateSubtask(newSub2);

            Subtask newSub3 = new Subtask(sub3.getId(), sub3.getName(), sub3.getDescription(), sub3.getStatus(),
                    sub3.getEpicId(), parseTime("2024-01-06 05:06:07"), sub3.getDuration());
            manager.updateSubtask(newSub3);

            Task newTask2 = new Task(task2.getId(), task2.getName(), task2.getDescription(), task2.getStatus(),
                    null, task2.getDuration());
            manager.updateTask(newTask2);

            List<Task> actualTasks = manager.getPrioritizedTasks();
            List<Task> expectedTasks = List.of(sub3, sub1, sub2, task1);

            assertIterableEquals(expectedTasks, actualTasks);
        }
    }

    @Nested
    @DisplayName("При удалении")
    class WhenDeleting {

        @Test
        @DisplayName("обычных задач, менеджер их больше не возвращает")
        public void testThatManagerRemoveAllTasks() {
            createAndSaveTask();
            createAndSaveTask();

            manager.removeTasks();

            assertEmpty(manager.getTasks());
        }

        @Test
        @DisplayName("обычной задачи по id, менеджер ее больше не возвращает")
        void removeTaskById() {
            Task task1 = createAndSaveTask();
            Task task2 = createAndSaveTask();

            manager.removeTaskById(task2.getId());

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
            Epic epic2 = createAndSaveEpicWithSubtasks(3);

            Set<Epic> expectedEpics = Set.of(epic1);
            Set<Subtask> expectedSubtasks = Set.copyOf(manager.getSubtasksOfEpic(epic1));

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
            Epic epic = createAndSaveEpic();
            Subtask sub1 = createAndSaveSubtask(epic);
            Subtask sub2 = createAndSaveSubtask(epic);

            sub1.setStatus(IN_PROGRESS);
            manager.updateSubtask(sub1);
            sub2.setStatus(DONE);
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
            Epic epic = createAndSaveEpic();
            createAndSaveSubtask(DONE, epic);

            manager.removeSubtasks();

            assertEquals(TaskStatus.NEW, epic.getStatus(),
                    String.format("статус эпика должен быть NEW, а не %s", epic.getStatus()));
        }

        @Test
        @DisplayName("эпика, попытка сохранить его подзадачу игнорируется")
        public void testThatSavingSubtaskOfDeletedEpicIsIgnored() {
            Epic epic = createAndSaveEpic();
            Subtask sub = createAndSaveSubtask(epic);

            manager.removeEpics();
            manager.saveSubtask(sub);

            assertEmpty(manager.getSubtasks(), "подзадач не должно существовать");
        }

        @Test
        @DisplayName("эпика, попытка обновить его подзадачу игнорируется")
        public void testThatUpdatingSubtaskOfDeletedEpicIsIgnored() {
            Epic epic = createAndSaveEpic();
            Subtask sub = createAndSaveSubtask(epic);

            manager.removeEpics();

            Subtask newSub = new Subtask(sub.getId(), "sub2", "desc2", epic, null, null);
            manager.updateSubtask(newSub);

            assertEmpty(manager.getSubtasks(), "подзадач не должно существовать");
        }

        @Test
        @DisplayName("эпика, попытка удалить его подзадачу игнорируется")
        public void testThatDeletingSubtaskOfDeletedEpicIsIgnored() {
            Epic epic = createAndSaveEpic();
            Subtask sub = createAndSaveSubtask(epic);

            manager.removeEpics();

            manager.removeSubtaskById(sub.getId());
            assertEmpty(manager.getSubtasks(), "подзадач не должно существовать");
        }

        @Test
        @DisplayName("по id задачи и подзадачи возвращаются в getPrioritizedTasks")
        public void testThatTasksAndSubtasksAreRePrioritized() {
            // name;description;status;startTime;duration
            Task task1 = createAndSaveTask("task;desc1;NEW;2024-01-10 01:02:03;123");
            Task task2 = createAndSaveTask("task;desc2;NEW;2024-01-09 02:03:04;234");

            // name;description
            Epic epic1 = createAndSaveEpic("epic;desc3");
            Epic epic2 = createAndSaveEpic("epic;desc4");

            // name;description;status;epicId;startTime;duration
            Subtask sub1 = createAndSaveSubtask("sub1;desc5;NEW;" + epic1.getId() + ";2024-01-08 03:04:05;345");
            Subtask sub2 = createAndSaveSubtask("sub2;desc6;NEW;" + epic1.getId() + ";2024-01-07 04:05:06;456");
            Subtask sub3 = createAndSaveSubtask("sub3;desc7;NEW;" + epic1.getId() + ";2024-01-07 05:06:07;567");

            manager.removeSubtaskById(sub2.getId());
            List<Task> actualTasks = manager.getPrioritizedTasks();
            List<Task> expectedTasks = List.of(sub3, sub1, task2, task1);

            assertIterableEquals(expectedTasks, actualTasks);

            manager.removeTaskById(task1.getId());
            actualTasks = manager.getPrioritizedTasks();
            expectedTasks = List.of(sub3, sub1, task2);

            assertIterableEquals(expectedTasks, actualTasks);
        }

        @Test
        @DisplayName("всех задач в getPrioritizedTasks возвращаются только подзадачи")
        public void testThatAfterTaskRemovalOnlySubtasksArePrioritized() {
            // name;description;status;startTime;duration
            Task task1 = createAndSaveTask("task;desc1;NEW;2024-01-10 01:02:03;123");
            Task task2 = createAndSaveTask("task;desc2;NEW;2024-01-09 02:03:04;234");

            // name;description
            Epic epic1 = createAndSaveEpic("epic;desc3");
            Epic epic2 = createAndSaveEpic("epic;desc4");

            // name;description;status;epicId;startTime;duration
            Subtask sub1 = createAndSaveSubtask("sub1;desc5;NEW;" + epic1.getId() + ";2024-01-08 03:04:05;345");
            Subtask sub2 = createAndSaveSubtask("sub2;desc6;NEW;" + epic1.getId() + ";2024-01-07 04:05:06;456");
            Subtask sub3 = createAndSaveSubtask("sub3;desc7;NEW;" + epic1.getId() + ";2024-01-06 05:06:07;567");

            manager.removeTasks();
            List<Task> actualTasks = manager.getPrioritizedTasks();
            List<Task> expectedTasks = List.of(sub3, sub2, sub1);

            assertIterableEquals(expectedTasks, actualTasks);
        }

        @Test
        @DisplayName("всех подзадач в getPrioritizedTasks возвращаются только задачи")
        public void testThatAfterSubtaskRemovalOnlyTasksArePrioritized() {
            // name;description;status;startTime;duration
            Task task1 = createAndSaveTask("task;desc1;NEW;2024-01-10 01:02:03;123");
            Task task2 = createAndSaveTask("task;desc2;NEW;2024-01-09 02:03:04;234");

            // name;description
            Epic epic1 = createAndSaveEpic("epic;desc3");
            Epic epic2 = createAndSaveEpic("epic;desc4");

            // name;description;status;epicId;startTime;duration
            Subtask sub1 = createAndSaveSubtask("sub1;desc5;NEW;" + epic1.getId() + ";2024-01-08 03:04:05;345");
            Subtask sub2 = createAndSaveSubtask("sub2;desc6;NEW;" + epic1.getId() + ";2024-01-07 04:05:06;456");
            Subtask sub3 = createAndSaveSubtask("sub3;desc7;NEW;" + epic1.getId() + ";2024-01-06 05:06:07;567");

            manager.removeSubtasks();
            List<Task> actualTasks = manager.getPrioritizedTasks();
            List<Task> expectedTasks = List.of(task2, task1);

            assertIterableEquals(expectedTasks, actualTasks);
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
            Epic epic = createAndSaveEpic();
            Subtask subtask1 = createAndSaveSubtask(epic);
            Subtask subtask2 = createAndSaveSubtask(epic);
            Task task = createAndSaveTask("Обычная задача", "Описание обычной задачи");

            List<Task> history;
            List<Task> expectedHistory = new LinkedList<>();

            // смотрим обычную задачу
            manager.getTaskById(task.getId());
            expectedHistory.addFirst(task);

            history = manager.getHistory();

            assertIterableEquals(expectedHistory, history, String.format(
                "история должна быть %s, а не %s", expectedHistory, history));

            // смотрим первую подзадачу
            manager.getSubtaskById(subtask1.getId());
            expectedHistory.addFirst(subtask1);

            history = manager.getHistory();

            assertIterableEquals(expectedHistory, history, String.format(
                "история должна быть %s, а не %s", expectedHistory, history));

            // смотрим эпик
            manager.getEpicById(epic.getId());
            expectedHistory.addFirst(epic);

            history = manager.getHistory();

            assertIterableEquals(expectedHistory, history, String.format(
                "история должна быть %s, а не %s", expectedHistory, history));

            // смотрим вторую подзадачу
            manager.getSubtaskById(subtask2.getId());
            history = manager.getHistory();

            expectedHistory.addFirst(subtask2);

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
            Task newTask = new Task(id, "new task", "new desc", TaskStatus.DONE, null, null);
            manager.updateTask(newTask);

            Task historyTask = manager.getHistory().getFirst();

            assertEquals(oldStatus, historyTask.getStatus(),
                    String.format("статус должен быть %s, а не %s", oldStatus, historyTask.getStatus()));

            assertEquals(oldName, historyTask.getName(),
                    String.format("имя должно быть %s, а не %s", oldName, historyTask.getName()));

            assertEquals(oldDescription, historyTask.getDescription(),
                    String.format("описание должно быть %s, а не %s", oldDescription, historyTask.getDescription()));

            assertEquals(id, historyTask.getId(),
                    String.format("id должно быть %s, а не %s", id, historyTask.getDescription()));
        }

        @Test
        @DisplayName("При просмотре одной и той же задачи, в истории сохраняется только последний просмотр")
        public void givenTaskManyTimesWatched_whenGetHistory_thenGotTheLastWatch() {
            Task task1 = createAndSaveTask("task1", "desc1");
            Task task2 = createAndSaveTask("task2", "desc2");

            manager.getTaskById(task1.getId());
            manager.getTaskById(task2.getId());
            manager.getTaskById(task1.getId());

            List<Task> actualHistory = manager.getHistory();
            assertEquals(2, actualHistory.size());
            assertEquals(task1, actualHistory.get(0));
            assertEquals(task2, actualHistory.get(1));
        }

        @Test
        @DisplayName("При удалении задачи, она больше не возвращается в истории")
        public void givenTaskDeleted_whenGetHistory_thenGotNoTask() {
            Task task = createAndSaveTask();
            Epic epic = createAndSaveEpic();
            Subtask sub = createAndSaveSubtask(epic);

            manager.getTaskById(task.getId());
            manager.getEpicById(epic.getId());
            manager.getSubtaskById(sub.getId());
            manager.removeTaskById(task.getId());
            manager.removeSubtaskById(sub.getId());
            manager.removeEpicById(epic.getId());

            assertEmpty(manager.getHistory());
        }

        @Test
        @DisplayName("При удалении всех задач история пуста")
        public void givenAllTaskDeleted_whenGetHistory_thenGotNothing() {
            Task task = createAndSaveTask();
            Epic epic = createAndSaveEpic();
            Subtask sub = createAndSaveSubtask(epic);

            manager.getTaskById(task.getId());
            manager.getEpicById(epic.getId());
            manager.getSubtaskById(sub.getId());
            manager.removeTasks();
            manager.removeSubtasks();
            manager.removeEpics();

            assertEmpty(manager.getHistory());
        }
    }

    @Nested
    @DisplayName("При пересчете статуса эпика")
    class WhenEpicStatusUpdate{

        private Epic epic;

        @BeforeEach
        public void setup() {
            epic = new Epic("epic", "desc");
            manager.saveEpic(epic);
        }

        @Test
        @DisplayName("Если список подзадач пуст, то статус NEW")
        public void testThanStatusIsNewWhenSubtaskListIsEmpty() {
            manager.updateEpic(epic);
            assertEquals(NEW, epic.getStatus());
        }

        @Test
        @DisplayName("Если все подзадачи NEW, то статус NEW")
        public void testThanStatusIsNewWhenAllSubtasksAreNew() {
            createAndSaveSubtask(NEW, epic);
            createAndSaveSubtask(NEW, epic);

            manager.updateEpic(epic);

            assertEquals(NEW, epic.getStatus());
        }

        @Test
        @DisplayName("Если все подзадачи DONE, то статус DONE")
        public void testThanStatusIsDoneWhenAllSubtasksAreDone() {
            createAndSaveSubtask(DONE, epic);
            createAndSaveSubtask(DONE, epic);

            manager.updateEpic(epic);

            assertEquals(DONE, epic.getStatus());
        }

        @Test
        @DisplayName("Если часть подзадач NEW, а часть DONE, то статус IN_PROGRESS")
        public void testThanStatusIsInProgressWhenSubtasksAreNewAndDone() {
            createAndSaveSubtask(DONE, epic);
            createAndSaveSubtask(NEW, epic);

            manager.updateEpic(epic);

            assertEquals(IN_PROGRESS, epic.getStatus());
        }

        @Test
        @DisplayName("Если часть подзадач NEW, а часть IN_PROGRESS, то статус IN_PROGRESS")
        public void testThanStatusIsInProgressWhenSubtasksAreNewAndInProgress() {
            createAndSaveSubtask(IN_PROGRESS, epic);
            createAndSaveSubtask(NEW, epic);

            manager.updateEpic(epic);

            assertEquals(IN_PROGRESS, epic.getStatus());
        }

        @Test
        @DisplayName("Если часть подзадач DONE, а часть IN_PROGRESS, то статус IN_PROGRESS")
        public void testThanStatusIsInProgressWhenSubtasksAreDoneAndInProgress() {
            createAndSaveSubtask(IN_PROGRESS, epic);
            createAndSaveSubtask(DONE, epic);

            manager.updateEpic(epic);

            assertEquals(IN_PROGRESS, epic.getStatus());
        }

        @Test
        @DisplayName("Если часть подзадач NEW, часть DONE, а часть IN_PROGRESS, то статус IN_PROGRESS")
        public void testThanStatusIsInProgressWhenSubtasksAreNewAndDoneAndInProgress() {
            createAndSaveSubtask(IN_PROGRESS, epic);
            createAndSaveSubtask(DONE, epic);
            createAndSaveSubtask(NEW, epic);

            manager.updateEpic(epic);

            assertEquals(IN_PROGRESS, epic.getStatus());
        }
    }

    private Task createAndSaveTask() {
        Task task = new Task("task", "desc", null, null);
        manager.saveTask(task);
        return task;
    }

    private Task createAndSaveTask(String name, String desc) {
        Task task = new Task(name, desc, null, null);
        manager.saveTask(task);
        return task;
    }

    private Task createAndSaveTask(String formattedTask) {
        Task task = createTask(formattedTask);
        manager.saveTask(task);
        return task;
    }

    // name;description;status;startTime;duration
    private Task createTask(String formattedTask) {
        String[] chunks = formattedTask.split(";");
        return new Task(
            chunks[0], // name
            chunks[1], // description
            TaskStatus.valueOf(chunks[2]), // status
            parseTime(chunks[3]), // startTime
            parseDuration(chunks[4]) // duration
        );
    }

    private Epic createAndSaveEpic() {
        Epic epic = new Epic("e", "d");
        manager.saveEpic(epic);
        return epic;
    }

    private Epic createAndSaveEpic(String formattedEpic) {
        Epic epic = createEpic(formattedEpic);
        manager.saveEpic(epic);
        return epic;
    }

    private Epic createAndSaveEpicWithSubtasks(int subtaskCount) {

        Epic epic = createAndSaveEpic();

        for (int i = 0; i < subtaskCount; i++) {
            Subtask sub = new Subtask("sub of e " + i, "desc of sub " + i, epic, null, null);
            manager.saveSubtask(sub);
        }

        return epic;
    }

    private Subtask createAndSaveSubtask(Epic epic) {
        Subtask sub = new Subtask("sub", "desc of sub", epic, null, null);
        manager.saveSubtask(sub);
        return sub;
    }

    private Subtask createAndSaveSubtask(String formattedSubtask) {
        Subtask sub = createSubtask(formattedSubtask);
        manager.saveSubtask(sub);
        return sub;
    }

    private Subtask createAndSaveSubtask(TaskStatus status, Epic epic) {
        Subtask sub = new Subtask("sub", "desc of sub", status, epic, null, null);
        manager.saveSubtask(sub);
        return sub;
    }

    private Epic createEpic(String formattedEpic) {
        String[] chunks = formattedEpic.split(";");
        return new Epic(
                chunks[0], // name
                chunks[1] // description
        );
    }

    // name;description;status;epicId;startTime;duration
    private Subtask createSubtask(String formattedSubtask) {
        String[] chunks = formattedSubtask.split(";");
        return new Subtask(
                null, // id
                chunks[0], // name
                chunks[1], // description
                TaskStatus.valueOf(chunks[2]), // status
                Integer.parseInt(chunks[3]), // epicId
                parseTime(chunks[4]), // startTime
                parseDuration(chunks[5]) // duration
        );
    }

    private LocalDateTime parseTime(String formattedTime) {
        return formattedTime.equals("null") ? null : LocalDateTime.parse(formattedTime, DATE_TIME_FORMATTER);
    }

    private Duration parseDuration(String formattedDuration) {
        return formattedDuration.equals("null") ? null : Duration.ofMinutes(Long.parseLong(formattedDuration));
    }


}