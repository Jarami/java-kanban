package tasks;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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


}