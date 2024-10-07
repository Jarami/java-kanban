package kanban.tasks;

import kanban.managers.Managers;
import kanban.managers.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SubtaskTest {

    TaskManager manager;

    @BeforeEach
    void setup() {
        manager = Managers.getDefault();
    }

    @Test
    @DisplayName("Вернуть id эпика, если тот сохранен")
    void getEpicIdIfEpicSaved() {
        Epic epic = new Epic("epic", "desc");
        int epicId = manager.saveEpic(epic);
        Subtask sub = new Subtask("sub", "sub desc", epic, null, null);

        int actualEpicId = sub.getEpicId();

        assertEquals(epicId, actualEpicId);
    }

    @Test
    @DisplayName("Вернуть null, если эпик не сохранен")
    void getNullIfEpicNotSaved() {
        Epic epic = new Epic("epic", "desc");
        Subtask sub = new Subtask("sub", "sub desc", epic, null, null);

        assertNull(sub.getEpicId());
    }

    @Test
    @DisplayName("Проверить, что после создания у подзадачи нет id и статус NEW")
    void testThatNewTaskHasNullIdAndNewStatus() {
        Epic epic = new Epic("epic", "desc");
        Subtask sub = new Subtask("sub", "desc of sub", epic, null, null);

        assertNull(sub.getId());
        assertEquals(TaskStatus.NEW, sub.getStatus());
    }
}