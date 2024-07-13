package tasks;

import static org.junit.jupiter.api.Assertions.*;
import static lib.TestAssertions.assertEmpty;

import managers.Managers;
import managers.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        int subId1 = manager.saveSubtask(new Subtask("sub1", "desc1", epic));
        epic.addSubtaskIdIfAbsent(subId1);
        assertIterableEquals(List.of(subId1), epic.getSubtasksId());

        // добавляем вторую подзадачу
        int subId2 = manager.saveSubtask(new Subtask("sub2", "desc2", epic));
        epic.addSubtaskIdIfAbsent(subId2);
        assertIterableEquals(List.of(subId1, subId2), epic.getSubtasksId());

    }

    @Test
    @DisplayName("Не добавлять подзадачу, если присутствует")
    void doNotAddSubtaskIfPresent() {
        // добавляем первую подзадачу
        int subId1 = manager.saveSubtask(new Subtask("sub1", "desc1", epic));
        epic.addSubtaskIdIfAbsent(subId1);

        epic.addSubtaskIdIfAbsent(subId1);
        assertIterableEquals(List.of(subId1), epic.getSubtasksId());
    }

    @Test
    @DisplayName("Удалить подзадачу, если присутствует")
    void removeSubtaskIdIfPresent() {
        int subId1 = manager.saveSubtask(new Subtask("sub1", "desc1", epic));
        epic.addSubtaskIdIfAbsent(subId1);

        int subId2 = manager.saveSubtask(new Subtask("sub2", "desc2", epic));
        epic.addSubtaskIdIfAbsent(subId2);

        epic.removeSubtaskId(subId1);
        assertIterableEquals(List.of(subId2), epic.getSubtasksId());
    }

    @Test
    @DisplayName("Игнорировать удаление подзадачи, если отсутствует")
    void ignoreRemoveSubtaskIdIfAbsent() {
        int subId1 = manager.saveSubtask(new Subtask("sub1", "desc1", epic));
        epic.addSubtaskIdIfAbsent(subId1);

        epic.removeSubtaskId(subId1 + 1);
        assertIterableEquals(List.of(subId1), epic.getSubtasksId());
    }

    @Test
    @DisplayName("Удалить все подзадачи")
    void removeSubtasksId() {
        int subId1 = manager.saveSubtask(new Subtask("sub1", "desc1", epic));
        epic.addSubtaskIdIfAbsent(subId1);

        int subId2 = manager.saveSubtask(new Subtask("sub2", "desc2", epic));
        epic.addSubtaskIdIfAbsent(subId2);

        epic.removeSubtasksId();

        assertEmpty(epic.getSubtasksId());
    }

    @Test
    @DisplayName("Проверить, что после создания у эпика нет id и статус NEW")
    void testThatNewTaskHasNullIdAndNewStatus() {
        Epic epic = new Epic("epic", "desc");

        assertNull(epic.getId());
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }
}