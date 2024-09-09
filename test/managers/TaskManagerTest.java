package managers;

import exceptions.ManagerSaveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;
import util.Tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static lib.TestAssertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static tasks.TaskStatus.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    protected TaskManager manager;

    @Nested
    @DisplayName("При сохранении")
    class WhenSaving {

        @Test
        @DisplayName("обычная задача не сохраняется, если duration не положителный")
        public void testThatManagerDoesNotSaveTaskWithNonPositiveDuration(){
            Task task = Tasks.createTask("task;desc;NEW;2024-01-01 01:02:03;-1");
            assertThrows(ManagerSaveException.class, () -> manager.saveTask(task));
        }

        @Test
        @DisplayName("обычная задача получает id и статус NEW")
        public void testThatMangerSavesTask() {
            Task task = Tasks.createTask("task;desc;NEW;null;null");
            int id = manager.saveTask(task);

            assertEquals(id, task.getId(), "После сохранения id должен быть определен");
            assertEquals(task.getStatus(), NEW, "После сохранения должен остаться статус NEW");
        }

        @Test
        @DisplayName("эпик получает id и статус NEW")
        public void testThatManagerSavesEpic() {
            Epic epic = Tasks.createEpic("epic;desc");
            int id = manager.saveEpic(epic);

            assertEquals(id, epic.getId(), "После сохранения id должен быть определен");
            assertEquals(epic.getStatus(), NEW, "После сохранения должен остаться статус NEW");
        }

        @Test
        @DisplayName("эпик не сохраняется, если duration неположительный")
        public void testThatManagerDoesNotSaveEpicWithNonPositiveDuration(){
            Epic epic = Tasks.createEpic("epic;desc");
            epic.setDuration(Duration.ofMinutes(-1));
            assertThrows(ManagerSaveException.class, () -> manager.saveEpic(epic));
        }

        @Test
        @DisplayName("подзадача получает id и статус NEW")
        public void testThatManagerSavesSubtask() {
            Epic epic = Tasks.createEpic("epic;desc");
            manager.saveEpic(epic);

            Subtask subtask = Tasks.createSubtask("sub;desc1;NEW;" + epic.getId() + ";null;null");
            int id = manager.saveSubtask(subtask);

            assertEquals(id, subtask.getId(), "После сохранения id должен быть определен");
            assertEquals(subtask.getStatus(), NEW, "После сохранения должен остаться статус NEW");
        }

        @Test
        @DisplayName("подзадача не сохраняется, если duration не положительный")
        public void testThatManagerDoesNotSaveSubtaskWithNonPositiveDuration() {
            Epic epic = Tasks.createEpic("epic;desc");
            manager.saveEpic(epic);
            Subtask sub = Tasks.createSubtask("sub;desc1;NEW;" + epic.getId() + ";2024-01-01 01:02:03;-1");
            assertThrows(ManagerSaveException.class, () -> manager.saveSubtask(sub));
        }

        @Test
        @DisplayName("id разных задач отличаются")
        public void testThatDifferentTasksHaveDifferentId() {

            Task task1 = createAndSaveTask("task1;desc1;NEW;null;null");
            Task task2 = createAndSaveTask("task2;desc2;NEW;null;null");

            Epic epic1 = createAndSaveEpic("epic1;desc3");
            Epic epic2 = createAndSaveEpic("epic2;desc4");

            Subtask sub1 = createAndSaveSubtask("sub1;desc5;NEW;" + epic1.getId() + ";null;null");
            Subtask sub2 = createAndSaveSubtask("sub2;desc6;NEW;" + epic1.getId() + ";null;null");
            Subtask sub3 = createAndSaveSubtask("sub3;desc7;NEW;" + epic1.getId() + ";null;null");
            Set<Integer> ids = Set.of(task1.getId(), task2.getId(), epic1.getId(), epic2.getId(), sub1.getId(),
                    sub2.getId(), sub3.getId());

            assertEquals(7, ids.size(), "идентификаторы разных задач должны отличаться");
        }

        @Test
        @DisplayName("статус у нового эпика с подзадачами будет NEW")
        public void testThatEpicWithSubtasksIsNewAfterSaving() {
            Epic epic = createAndSaveEpic("epic1;desc3");
            createAndSaveSubtask("sub1;desc5;NEW;" + epic.getId() + ";null;null");
            createAndSaveSubtask("sub2;desc6;NEW;" + epic.getId() + ";null;null");

            assertEquals(NEW, epic.getStatus());
        }

        @Test
        @DisplayName("задачи и подзадачи возвращаются в getPrioritizedTasks")
        public void testThatTasksAndSubtasksArePrioritized() {
            // name;description;status;startTime;duration
            Task task1 = createAndSaveTask("task1;desc1;NEW;2024-01-10 01:02:03;123");
            Task task2 = createAndSaveTask("task2;desc2;NEW;2024-01-09 02:03:04;234");

            // name;description
            Epic epic1 = createAndSaveEpic("epic1;desc3");
            Epic epic2 = createAndSaveEpic("epic2;desc4");

            // name;description;status;epicId;startTime;duration
            Subtask sub1 = createAndSaveSubtask("sub1;desc5;NEW;" + epic1.getId() + ";2024-01-08 03:04:05;345");
            Subtask sub2 = createAndSaveSubtask("sub2;desc6;NEW;" + epic1.getId() + ";2024-01-07 04:05:06;456");
            Subtask sub3 = createAndSaveSubtask("sub3;desc7;NEW;" + epic1.getId() + ";null;456");

            List<Task> actualTasks = manager.getPrioritizedTasks();
            List<Task> expectedTasks = List.of(sub2, sub1, task2, task1);

            assertIterableEquals(expectedTasks, actualTasks);
        }

        @Test
        @DisplayName("непересекающихся задач все ОК")
        public void testThatNonInterceptedTasksAreSaved() {
            // name;description;status;startTime;duration
            Task task1 = createAndSaveTask("task1;desc1;NEW;2024-01-10 01:02:03;123");
            Task task2 = createAndSaveTask("task2;desc2;NEW;2024-01-09 02:03:04;234");

            // name;description
            Epic epic1 = createAndSaveEpic("epic1;desc3");
            Epic epic2 = createAndSaveEpic("epic2;desc4");

            // name;description;status;epicId;startTime;duration
            Subtask sub1 = createAndSaveSubtask("sub1;desc5;NEW;" + epic1.getId() + ";2024-01-08 03:04:05;345");
            Subtask sub2 = createAndSaveSubtask("sub2;desc6;NEW;" + epic1.getId() + ";2024-01-07 04:05:06;456");
            Subtask sub3 = createAndSaveSubtask("sub3;desc7;NEW;" + epic1.getId() + ";2024-01-06 05:06:07;567");

            List<Task> actualTasks = manager.getTasks();
            List<Task> expectedTasks = List.of(task1, task2);

            assertIterableEquals(expectedTasks, actualTasks, "задачи должны сохраняться");

            List<Epic> actualEpics = manager.getEpics();
            List<Epic> expectedEpics = List.of(epic1, epic2);

            assertIterableEquals(expectedEpics, actualEpics, "эпики должны сохраняться");

            List<Subtask> actualSubtasks = manager.getSubtasks();
            List<Subtask> expectedSubtasks = List.of(sub1, sub2, sub3);

            assertIterableEquals(expectedSubtasks, actualSubtasks, "подзадачи должны сохраняться");
        }

        @Test
        @DisplayName("если задача пересекается с другой задачей, то она не сохраняется")
        public void testThatTaskInterceptedWithSomeSubtaskIsNotSaved() {
            // name;description;status;startTime;duration
            Task task = createAndSaveTask("task1;desc1;NEW;2024-01-10 01:02:03;123");
            assertThrows(ManagerSaveException.class, () -> {
                createAndSaveTask("task2;desc2;NEW;2024-01-10 02:03:04;234");
            });

            assertIterableEquals(List.of(task), manager.getTasks());
        }

        @Test
        @DisplayName("если задача пересекается с подзадачей, то задача не сохраняется")
        public void testThatTaskInterceptedWithOtherTaskIsNotSaved() {
            // name;description
            Epic epic1 = createAndSaveEpic("epic1;desc3");

            // name;description;status;epicId;startTime;duration
            Subtask sub1 = createAndSaveSubtask("sub1;desc5;NEW;" + epic1.getId() + ";2024-01-08 03:04:05;345");

            // name;description;status;startTime;duration
            assertThrows(ManagerSaveException.class, () -> {
                createAndSaveTask("task2;desc2;NEW;2024-01-08 03:04:05;234");
            });

            assertEmpty(manager.getTasks());
            assertIterableEquals(List.of(epic1), manager.getEpics());
            assertIterableEquals(List.of(sub1), manager.getSubtasks());
        }

        @Test
        @DisplayName("если подзадача пересекается с другой подзадачей, то она не сохраняется")
        public void testThatSubtaskInterceptedWithSomeSubtaskIsNotSaved() {

            // name;description
            Epic epic1 = createAndSaveEpic("epic1;desc1");
            Epic epic2 = createAndSaveEpic("epic2;desc2");

            // name;description;status;epicId;startTime;duration
            Subtask sub1 = createAndSaveSubtask("sub1;desc3;NEW;" + epic1.getId() + ";2024-01-08 03:04:05;345");

            assertThrows(ManagerSaveException.class, () -> {
                createAndSaveSubtask("sub2;desc4;NEW;" + epic2.getId() + ";2024-01-08 04:05:06;234");
            });

            assertIterableEquals(List.of(epic1, epic2), manager.getEpics());
            assertIterableEquals(List.of(sub1), manager.getSubtasks());
        }

        @Test
        @DisplayName("если подзадача пересекается с задачей, то подзадача не сохраняется")
        public void testThatSubtaskInterceptedWithSomeTaskIsNotSaved() {
            // name;description;status;startTime;duration
            Task task = createAndSaveTask("task1;desc1;NEW;2024-01-08 03:04:05;234");

            // name;description
            Epic epic = createAndSaveEpic("epic1;desc2");

            assertThrows(ManagerSaveException.class, () -> {
                // name;description;status;epicId;startTime;duration
                createAndSaveSubtask("sub1;desc3;NEW;" + epic.getId() + ";2024-01-08 04:05:06;345");
            });

            assertIterableEquals(List.of(task), manager.getTasks());
            assertEmpty(manager.getSubtasks());
        }
    }

    @Nested
    @DisplayName("При обновлении")
    class WhenUpdating{

        @Test
        @DisplayName("обычная задача может изменить имя, описание и статус")
        public void testThatManagerUpdateTask() {

            Task task = createAndSaveTask("task;desc;NEW;null;null");

            Task newTask = new Task(task.getId(), "updated task", "updated desc", DONE,
                    Tasks.parseTime("2024-01-01 00:00:00"), Duration.ofMinutes(123));
            manager.updateTask(newTask);

            Task actualTask = manager.getTaskById(task.getId());

            assertTaskEquals(newTask, actualTask);
        }

        @Test
        @DisplayName("можно обновить только сохраненную задачу")
        void testThatOnlySavedTaskCanBeUpdated() {
            Task task = Tasks.createTask("task;desc;NEW;null;null");
            manager.updateTask(task);

            assertEmpty(manager.getTasks());
        }

        @Test
        @DisplayName("можно обновить только существующую задачу")
        void testThatOnlyExistingTaskCanBeUpdated() {
            Task task = new Task(1, "updated task", "updated desc", null, null);
            manager.updateTask(task);

            assertNull(manager.getTaskById(1));
        }

        @Test
        @DisplayName("эпик может изменить имя, описание, статус и список подзадач")
        public void testThatManagerUpdateEpic() {

            Epic epic = createAndSaveEpic("epic1;desc1");
            createAndSaveSubtask("sub1;desc2;DONE;" + epic.getId() + ";2024-01-01 00:00:00;120");
            createAndSaveSubtask("sub2;desc3;DONE;" + epic.getId() + ";2024-01-02 00:00:00;120");

            Epic newEpic = new Epic(epic.getId(), "new epic", "new desc");
            createAndSaveSubtask("sub3;desc4;NEW;" + epic.getId() + ";2024-01-03 00:00:00;120");
            createAndSaveSubtask("sub4;desc5;NEW;" + epic.getId() + ";2024-01-04 00:00:00;120");
            createAndSaveSubtask("sub5;desc6;NEW;" + epic.getId() + ";2024-01-05 00:00:00;120");
            manager.updateEpic(newEpic);

            System.out.println(manager.getSubtasksOfEpic(epic).size());
            System.out.println(manager.getSubtasksOfEpic(newEpic).size());

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
            Epic epic = createAndSaveEpic("epic1;desc1");
            Subtask sub = createAndSaveSubtask("sub1;desc2;DONE;" + epic.getId() + ";2024-01-01 00:00:00;120");

            manager.removeEpics();

            Subtask newSub = new Subtask(sub.getId(), "new sub", "desc of new sub", epic, null, null);
            manager.updateSubtask(newSub);

            assertNull(manager.getSubtaskById(sub.getId()));
        }

        @Test
        @DisplayName("подзадача не может изменить своего эпика")
        void testThatSubtaskCannotChangeItEpic() {
            Epic epic = createAndSaveEpic("epic1;desc1");
            Subtask sub = createAndSaveSubtask("sub1;desc2;DONE;" + epic.getId() + ";2024-01-01 00:00:00;120");

            Epic newEpic = createAndSaveEpic();

            Subtask newSub = new Subtask(sub.getId(), sub.getName(), sub.getDescription(), sub.getStatus(), newEpic,
                    null, null);
            manager.updateSubtask(newSub);

            Subtask actualSub = manager.getSubtaskById(sub.getId());
            Epic actualEpic = manager.getEpicOfSubtask(actualSub);

            assertEquals(epic, actualEpic);
        }

        @Test
        @DisplayName("у эпика возвращается обновленная версия подзадачи")
        public void testThatEpicContainsUpdatedSubtask() {
            Epic epic = createAndSaveEpic("epic1;desc1");
            Subtask sub = createAndSaveSubtask("sub1;desc2;DONE;" + epic.getId() + ";2024-01-01 00:00:00;120");

            Subtask newSub = new Subtask(sub.getId(), "new sub", "new desc", NEW, epic, null, null);
            manager.updateSubtask(newSub);

            List<Subtask> subtasks = manager.getSubtasksOfEpic(epic);
            Subtask actualSub = subtasks.getFirst();

            assertSubtaskEquals(newSub, actualSub);
        }

        @Test
        @DisplayName("подзадач у их эпика, его статус обновляется")
        public void testThatEpicStatusUpdatedAfterSubtaskUpdate() {

            Epic epic = createAndSaveEpic("epic1;desc1");
            Subtask sub1 = createAndSaveSubtask("sub1;desc2;DONE;" + epic.getId() + ";2024-01-01 00:00:00;120");
            Subtask sub2 = createAndSaveSubtask("sub2;desc3;DONE;" + epic.getId() + ";2024-01-02 00:00:00;120");

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

            Subtask newSub = Tasks.copy(subs.getFirst());
            newSub.setStartTime(parseTime("2024-01-02 00:00:00"));
            manager.updateSubtask(newSub);

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

            Subtask newSub = Tasks.copy(subs.getLast());
            newSub.setStartTime(parseTime("2024-01-04 00:00:00"));
            manager.updateSubtask(newSub);

            assertEquals(parseTime("2024-01-04 02:00:00"), epic.getEndTime(),
                    "начало должно быть 2024-01-04 02:00:00, а не " + epic.getEndTime());

            Subtask newSub1 = Tasks.copy(newSub);
            newSub1.setDuration(Duration.ofMinutes(60));
            manager.updateSubtask(newSub1);

            assertEquals(parseTime("2024-01-04 01:00:00"), epic.getEndTime(),
                    "начало должно быть 2024-01-04 01:00:00, а не " + epic.getEndTime());
        }

        @Test
        @DisplayName("задачи и подзадачи возвращаются в getPrioritizedTasks")
        public void testThatTasksAndSubtasksAreRePrioritized() {
            // name;description;status;startTime;duration
            Task task1 = createAndSaveTask("task1;desc1;NEW;2024-01-10 01:02:03;123");
            Task task2 = createAndSaveTask("task2;desc2;NEW;2024-01-09 02:03:04;234");

            // name;description
            Epic epic1 = createAndSaveEpic("epic;desc3");
            Epic epic2 = createAndSaveEpic("epic;desc4");

            // name;description;status;epicId;startTime;duration
            Subtask sub1 = createAndSaveSubtask("sub1;desc5;NEW;" + epic1.getId() + ";2024-01-08 03:04:05;345");
            Subtask sub2 = createAndSaveSubtask("sub2;desc6;NEW;" + epic1.getId() + ";2024-01-07 04:05:06;456");
            Subtask sub3 = createAndSaveSubtask("sub3;desc7;NEW;" + epic1.getId() + ";null;456");

            Subtask newSub2 = Tasks.copy(sub2);
            newSub2.setStartTime(parseTime("2024-01-08 10:00:00"));
            manager.updateSubtask(newSub2);

            Subtask newSub3 = Tasks.copy(sub3);
            newSub3.setStartTime(parseTime("2024-01-06 05:06:07"));
            manager.updateSubtask(newSub3);

            Task newTask2 = Tasks.copy(task2);
            newTask2.setStartTime(null);
            manager.updateTask(newTask2);

            List<Task> actualTasks = manager.getPrioritizedTasks();
            List<Task> expectedTasks = List.of(sub3, sub1, sub2, task1);

            assertIterableEquals(expectedTasks, actualTasks);
        }

        @Test
        @DisplayName("подзадачи, она не должна изменяться, если пересекается с другими")
        public void testThatUpdatedTaskIsNotSavedIfItInterceptedWithOthers() {
            // name;description;status;startTime;duration
            Task task1 = createAndSaveTask("task1;desc1;NEW;2024-01-10 01:00:00;120");
            Task task2 = createAndSaveTask("task2;desc2;NEW;2024-01-09 02:00:00;120");

            // name;description
            Epic epic1 = createAndSaveEpic("epic1;desc3");
            Epic epic2 = createAndSaveEpic("epic2;desc4");

            // name;description;status;epicId;startTime;duration
            Subtask sub1 = createAndSaveSubtask("sub1;desc5;NEW;" + epic1.getId() + ";2024-01-08 03:00:00;120");
            Subtask sub2 = createAndSaveSubtask("sub2;desc6;NEW;" + epic1.getId() + ";2024-01-07 04:00:00;120");
            Subtask sub3 = createAndSaveSubtask("sub3;desc7;NEW;" + epic1.getId() + ";2024-01-06 05:00:00;120");

            // начинает пересекаться с task2 - так нельзя
            Task newTask1 = Tasks.copy(task1);
            newTask1.setStartTime(parseTime("2024-01-09 03:00:00"));
            assertThrows(ManagerSaveException.class, () -> {
                manager.updateTask(newTask1);
            });
            assertTaskEquals(task1, manager.getTaskById(task1.getId()));

            // начинает пересекаться с sub2 - так нельзя
            Task newTask2 = Tasks.copy(task2);
            newTask2.setStartTime(parseTime("2024-01-07 05:00:00"));
            assertThrows(ManagerSaveException.class, () -> {
                manager.updateTask(newTask2);
            });
            assertTaskEquals(task2, manager.getTaskById(task2.getId()));

            // начинает пересекаться сама с собой - так можно
            Task newSub3 = Tasks.copy(sub3);
            newSub3.setStartTime(parseTime("2024-01-06 06:00:00"));
            assertDoesNotThrow(() -> {
                manager.updateTask(newSub3);
            });
            assertTaskEquals(task1, manager.getTaskById(task1.getId()));
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
        @DisplayName("подзадачи у эпика, его начало и конец должны обновиться")
        public void testThatEpicStartTimeUpdatedAfterSubtaskRemoval() {

            // name;description
            Epic epic = createAndSaveEpic("epic;desc");

            // name;description;status;epicId;startTime;duration
            Subtask sub1 = createAndSaveSubtask("sub1;desc5;NEW;" + epic.getId() + ";2024-01-06 00:00:00;120");
            Subtask sub2 = createAndSaveSubtask("sub2;desc6;NEW;" + epic.getId() + ";2024-01-07 00:00:00;120");
            Subtask sub3 = createAndSaveSubtask("sub3;desc7;NEW;" + epic.getId() + ";2024-01-08 00:00:00;120");


            manager.removeSubtaskById(sub1.getId());
            manager.removeSubtaskById(sub3.getId());

            assertEquals(parseTime("2024-01-07 00:00:00"), epic.getStartTime());
            assertEquals(parseTime("2024-01-07 02:00:00"), epic.getEndTime());
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
            Task task1 = createAndSaveTask("task1;desc1;NEW;2024-01-10 01:02:03;123");
            Task task2 = createAndSaveTask("task2;desc2;NEW;2024-01-09 02:03:04;234");

            // name;description
            Epic epic1 = createAndSaveEpic("epic;desc3");
            Epic epic2 = createAndSaveEpic("epic;desc4");

            // name;description;status;epicId;startTime;duration
            Subtask sub1 = createAndSaveSubtask("sub1;desc5;NEW;" + epic1.getId() + ";2024-01-08 03:04:05;345");
            Subtask sub2 = createAndSaveSubtask("sub2;desc6;NEW;" + epic1.getId() + ";2024-01-07 04:05:06;456");
            Subtask sub3 = createAndSaveSubtask("sub3;desc7;NEW;" + epic1.getId() + ";2024-01-06 05:06:07;567");

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
            Task task1 = createAndSaveTask("task1;desc1;NEW;2024-01-10 01:02:03;123");
            Task task2 = createAndSaveTask("task2;desc2;NEW;2024-01-09 02:03:04;234");

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
            Task task1 = createAndSaveTask("task1;desc1;NEW;2024-01-10 01:02:03;123");
            Task task2 = createAndSaveTask("task2;desc2;NEW;2024-01-09 02:03:04;234");

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

        @Test
        @DisplayName("задачи, она уже не участвует в определении пересечений")
        public void testThatUpdatedTaskIsNotSavedIfItInterceptedWithOthers() {
            // name;description;status;startTime;duration
            Task task1 = createAndSaveTask("task1;desc1;NEW;2024-01-10 01:00:00;120");
            Task task2 = createAndSaveTask("task2;desc2;NEW;2024-01-09 02:00:00;120");

            // name;description
            Epic epic1 = createAndSaveEpic("epic1;desc3");
            Epic epic2 = createAndSaveEpic("epic2;desc4");

            // name;description;status;epicId;startTime;duration
            Subtask sub1 = createAndSaveSubtask("sub1;desc5;NEW;" + epic1.getId() + ";2024-01-08 03:00:00;120");
            Subtask sub2 = createAndSaveSubtask("sub2;desc6;NEW;" + epic1.getId() + ";2024-01-07 04:00:00;120");
            Subtask sub3 = createAndSaveSubtask("sub3;desc7;NEW;" + epic1.getId() + ";2024-01-06 05:00:00;120");

            manager.removeTaskById(task2.getId());

            // начинает пересекаться с task2 - можно, так как задача task2 удалена
            Task newTask1 = Tasks.copy(task1);
            newTask1.setStartTime(parseTime("2024-01-09 03:00:00"));
            assertDoesNotThrow(() -> {
                manager.updateTask(newTask1);
            });
            assertTaskEquals(newTask1, manager.getTaskById(task1.getId()));
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

    protected Task createAndSaveTask() {
        Task task = new Task("task", "desc", null, null);
        manager.saveTask(task);
        return task;
    }

    protected Task createAndSaveTask(String name, String desc) {
        Task task = new Task(name, desc, null, null);
        manager.saveTask(task);
        return task;
    }

    protected Task createAndSaveTask(String formattedTask) {
        Task task = Tasks.createTask(formattedTask);
        manager.saveTask(task);
        return task;
    }

    protected Epic createAndSaveEpic() {
        Epic epic = new Epic("e", "d");
        manager.saveEpic(epic);
        return epic;
    }

    protected Epic createAndSaveEpic(String formattedEpic) {
        Epic epic = Tasks.createEpic(formattedEpic);
        manager.saveEpic(epic);
        return epic;
    }

    protected Epic createAndSaveEpicWithSubtasks(int subtaskCount) {

        Epic epic = createAndSaveEpic();

        for (int i = 0; i < subtaskCount; i++) {
            Subtask sub = new Subtask("sub of e " + i, "desc of sub " + i, epic, null, null);
            manager.saveSubtask(sub);
        }

        return epic;
    }

    protected Subtask createAndSaveSubtask(Epic epic) {
        Subtask sub = new Subtask("sub", "desc of sub", epic, null, null);
        manager.saveSubtask(sub);
        return sub;
    }

    protected Subtask createAndSaveSubtask(String formattedSubtask) {
        Subtask sub = Tasks.createSubtask(formattedSubtask);
        manager.saveSubtask(sub);
        return sub;
    }

    protected Subtask createAndSaveSubtask(TaskStatus status, Epic epic) {
        Subtask sub = new Subtask("sub", "desc of sub", status, epic, null, null);
        manager.saveSubtask(sub);
        return sub;
    }

    protected LocalDateTime parseTime(String formattedTime) {
        return formattedTime.equals("null") ? null : LocalDateTime.parse(formattedTime, DATE_TIME_FORMATTER);
    }

    protected Duration parseDuration(String formattedDuration) {
        return formattedDuration.equals("null") ? null : Duration.ofMinutes(Long.parseLong(formattedDuration));
    }
}
