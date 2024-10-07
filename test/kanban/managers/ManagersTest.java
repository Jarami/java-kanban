package kanban.managers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ManagersTest {

    @Test
    void getDefault() {
        TaskManager manager = Managers.getDefault();
        assertInstanceOf(InMemoryTaskManager.class, manager);
    }

    @Test
    void getDefaultHistory() {
        HistoryManager manager = Managers.getDefaultHistory();
        assertInstanceOf(InMemoryHistoryManager.class, manager);
    }
}