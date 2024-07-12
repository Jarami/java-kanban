package tasks;

import managers.Managers;
import managers.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        Subtask sub = new Subtask("sub", "sub desc", epic);

        int actualEpicId = sub.getEpicId();

        assertEquals(epicId, actualEpicId);
    }

    @Test
    @DisplayName("Вернуть null, если эпик не сохранен")
    void getNullIfEpicNotSaved() {
        Epic epic = new Epic("epic", "desc");
        Subtask sub = new Subtask("sub", "sub desc", epic);

        assertNull(sub.getEpicId());
    }
}