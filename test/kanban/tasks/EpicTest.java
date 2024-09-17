package kanban.tasks;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static kanban.lib.TestAssertions.assertEmpty;
import static kanban.tasks.TaskStatus.*;

import kanban.managers.Managers;
import kanban.managers.TaskManager;
import kanban.tasks.Epic;
import kanban.tasks.Subtask;
import kanban.tasks.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

class EpicTest {

    TaskManager manager;
    Epic epic;

    @BeforeEach
    void setup() {
        manager = Managers.getDefault();
        epic = new Epic("epic", "desc");
        manager.saveEpic(epic);
    }

    @Test
    @DisplayName("Добавить подзадачу, если отсутствует")
    void addSubtaskIfAbsent() {
        // добавляем первую подзадачу
        Subtask sub1 = new Subtask("sub1", "desc1", epic, null, null);
        manager.saveSubtask(sub1);
        epic.addSubtaskIdIfAbsent(sub1);
        assertIterableEquals(List.of(sub1), manager.getSubtasksOfEpic(epic));

        // добавляем вторую подзадачу
        Subtask sub2 = new Subtask("sub2", "desc2", epic, null, null);
        manager.saveSubtask(sub2);
        epic.addSubtaskIdIfAbsent(sub2);
        assertIterableEquals(List.of(sub1, sub2), manager.getSubtasksOfEpic(epic));

    }

    @Test
    @DisplayName("Не добавлять подзадачу, если присутствует")
    void doNotAddSubtaskIfPresent() {
        // добавляем первую подзадачу
        Subtask sub1 = new Subtask("sub1", "desc1", epic, null, null);
        manager.saveSubtask(sub1);

        epic.addSubtaskIdIfAbsent(sub1);
        epic.addSubtaskIdIfAbsent(sub1);

        assertIterableEquals(List.of(sub1), manager.getSubtasksOfEpic(epic));
    }

    @Test
    @DisplayName("Удалить подзадачу, если присутствует")
    void removeSubtaskIdIfPresent() {
        Subtask sub1 = new Subtask("sub1", "desc1", epic, null, null);
        manager.saveSubtask(sub1);
        epic.addSubtaskIdIfAbsent(sub1);

        Subtask sub2 = new Subtask("sub2", "desc2", epic, null, null);
        manager.saveSubtask(sub2);
        epic.addSubtaskIdIfAbsent(sub2);

        epic.removeSubtask(sub1);
        assertIterableEquals(List.of(sub2), manager.getSubtasksOfEpic(epic));
    }

    @Test
    @DisplayName("Игнорировать удаление подзадачи, если отсутствует у эпика")
    void ignoreRemoveSubtaskIfAbsent() {
        Epic epic1 = new Epic("epic1", "desc1");
        manager.saveEpic(epic1);

        Epic epic2 = new Epic("epic1", "desc1");
        manager.saveEpic(epic2);

        Subtask sub = new Subtask("sub1", "desc1", epic1, null, null);
        manager.saveSubtask(sub);

        epic2.removeSubtask(sub);
        assertEmpty(manager.getSubtasksOfEpic(epic2));
    }

    @Test
    @DisplayName("Удалить все подзадачи")
    void removeSubtasks() {
        Subtask sub1 = new Subtask("sub1", "desc1", epic, null, null);
        manager.saveSubtask(sub1);
        epic.addSubtaskIdIfAbsent(sub1);

        Subtask sub2 = new Subtask("sub2", "desc2", epic, null, null);
        manager.saveSubtask(sub2);
        epic.addSubtaskIdIfAbsent(sub2);

        epic.removeSubtasks();

        assertEmpty(epic.getSubtasksId());
    }

    @Test
    @DisplayName("Проверить, что после создания у эпика нет id и статус NEW")
    void testThatNewTaskHasNullIdAndNewStatus() {
        Epic epic = new Epic("epic", "desc");

        assertNull(epic.getId());
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    @DisplayName("Проверяем, что эпик корректно обновляет свой статус")
    public void testThatEpicUpdatesStatus() {
        Epic epic = new Epic("epic", "desc0");

        List<Subtask> subtasks = List.of(
                new Subtask("sub1", "desc1", NEW, epic, null, null),
                new Subtask("sub1", "desc1", NEW, epic, null, null)
        );

        assertEquals(NEW, epic.getStatus());

        subtasks.getFirst().setStatus(IN_PROGRESS);
        epic.update(subtasks);
        assertEquals(IN_PROGRESS, epic.getStatus());

        subtasks.get(0).setStatus(DONE);
        subtasks.get(1).setStatus(DONE);
        epic.update(subtasks);
        assertEquals(DONE, epic.getStatus());
    }

    @Test
    @DisplayName("Проверяем, что эпик корректно обновляет свою продолжительность")
    public void testThatEpicUpdatesDuration() {
        Epic epic = new Epic("epic", "desc0");

        List<Subtask> subtasks = List.of(
                new Subtask("sub1", "desc1", NEW, epic, null, Duration.ofMinutes(1)),
                new Subtask("sub2", "desc2", NEW, epic, null, null),
                new Subtask("sub3", "desc3", NEW, epic, null, Duration.ofMinutes(2))
        );

        epic.update(subtasks);
        assertEquals(Duration.ofMinutes(3), epic.getDuration());

        subtasks.getFirst().setDuration(Duration.ofMinutes(3));
        epic.update(subtasks);
        assertEquals(Duration.ofMinutes(5), epic.getDuration());
    }

    @Test
    @DisplayName("Проверяем, что эпик корректно обновляет время своего начала")
    public void testThatEpicUpdatesStartTime() {
        Epic epic = new Epic("epic", "desc0");

        List<Subtask> subtasks = List.of(
                new Subtask("sub1", "desc1", NEW, epic, LocalDateTime.parse("2024-01-01T00:00:00"), null),
                new Subtask("sub2", "desc2", NEW, epic, null, null),
                new Subtask("sub3", "desc3", NEW, epic, LocalDateTime.parse("2024-01-02T00:00:00"), null)
        );

        epic.update(subtasks);
        assertEquals(LocalDateTime.parse("2024-01-01T00:00:00"), epic.getStartTime(),
                String.format("Время начала должно быть 2024-01-01T00:00:00, а не %s", epic.getStartTime())
        );

        subtasks.getFirst().setStartTime(LocalDateTime.parse("2024-01-03T00:00:00"));
        epic.update(subtasks);
        assertEquals(LocalDateTime.parse("2024-01-02T00:00:00"), epic.getStartTime(),
                String.format("Время начала должно быть 2024-01-02T00:00:00, а не %s", epic.getStartTime())
        );
    }

    @Test
    @DisplayName("Проверяем, что эпик корректно обновляет время своего начала")
    public void testThatEpicUpdatesEndTime() {
        Epic epic = new Epic("epic", "desc0");

        List<Subtask> subtasks = List.of(
                new Subtask("sub1", "desc1", NEW, epic,
                        LocalDateTime.parse("2024-01-01T00:00:00"), Duration.ofHours(12)),
                new Subtask("sub2", "desc2", NEW, epic, null, null),
                new Subtask("sub3", "desc3", NEW, epic,
                        LocalDateTime.parse("2024-01-02T00:00:00"), Duration.ofHours(12))
        );

        epic.update(subtasks);
        assertEquals(LocalDateTime.parse("2024-01-02T12:00:00"), epic.getEndTime(),
                String.format("Время окончания должно быть 2024-01-02T12:00:00, а не %s", epic.getEndTime())
        );

        subtasks.getFirst().setStartTime(LocalDateTime.parse("2024-01-03T00:00:00"));
        epic.update(subtasks);
        assertEquals(LocalDateTime.parse("2024-01-03T12:00:00"), epic.getEndTime(),
                String.format("Время окончания должно быть 2024-01-03T12:00:00, а не %s", epic.getEndTime())
        );

        subtasks.getFirst().setDuration(Duration.ofHours(24));
        epic.update(subtasks);
        assertEquals(LocalDateTime.parse("2024-01-04T00:00:00"), epic.getEndTime(),
                String.format("Время окончания должно быть 2024-01-04T00:00:00, а не %s", epic.getEndTime())
        );
    }

}