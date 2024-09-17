package kanban.managers;

import kanban.managers.HistoryManager;
import kanban.managers.Managers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import kanban.tasks.Epic;
import kanban.tasks.Subtask;
import kanban.tasks.Task;
import static kanban.lib.TestAssertions.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    HistoryManager history;

    @BeforeEach
    public void setup() {
        history = Managers.getDefaultHistory();
    }

    @Test
    @DisplayName("получить пустую историю задач")
    public void getEmptyHistory() {
        List<Task> tasks = history.getHistory();
        assertEmpty(tasks);
    }

    @Test
    @DisplayName("Добавить три разных задачи")
    public void addDifferentTasks() {
        Task task = new Task(1, "task", "task desc", null, null);
        history.add(task);
        Epic epic = new Epic(2, "epic", "epic desc");
        history.add(epic);
        Subtask sub = new Subtask(3, "sub", "sub desc", epic, null, null);
        history.add(sub);

        List<Task> actualTasks = history.getHistory();
        List<Task> expectedTasks = List.of(sub, epic, task);
        assertIterableEquals(expectedTasks, actualTasks);
    }

    @Test
    @DisplayName("Добавить десять задач")
    public void testThatTenTasksCanBeAdded() {

        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasks.add(new Task(i,"task" + i, "desc of task " + i, null, null));
        }

        for (int i = 9; i >= 0; i--) {
            history.add(tasks.get(i));
        }

        List<Task> actualTasks = history.getHistory();
        assertIterableEquals(tasks, actualTasks);
    }

    @Test
    @DisplayName("Добавить несколько экземпляров существующей задачи")
    public void givenManyTaskWithSameId_whenGetHistory_thenGotLatestTask() {
        Task task1 = new Task(1, "task1", "desc1", null, null);
        Task task2 = new Task(2, "task2", "desc2", null, null);
        Task task3 = new Task(1, "task3", "desc3", null, null);
        history.add(task1);
        history.add(task2);
        history.add(task3);

        List<Task> actualHistory = history.getHistory();
        Task actualHistoryTask3 = actualHistory.get(0);
        Task actualHistoryTask2 = actualHistory.get(1);

        assertEquals(2, actualHistory.size(),
            String.format("history size must be 2, not %s", actualHistory.size()));

        assertEquals("task2", actualHistoryTask2.getName(),
            String.format("task2 name must be task2, not %s", actualHistoryTask2.getName()));

        assertEquals("task3", actualHistoryTask3.getName(),
            String.format("task3 name must be task3, not %s", actualHistoryTask3.getName()));
    }

    @Test
    @DisplayName("Очистить историю")
    public void clearHistory() {
        Task task = new Task(1, "task", "task desc", null, null);
        history.add(task);

        history.clear();

        assertEmpty(history.getHistory());
    }

    @Test
    @DisplayName("Удалить существующую задачу из начала")
    public void givenNonEmptyHistory_whenRemoveFirst_thenGotTaskRemoved() {
        Task task1 = new Task(1, "task1", "desc1", null, null);
        Task task2 = new Task(2, "task2", "desc2", null, null);
        Task task3 = new Task(3, "task3", "desc3", null, null);
        history.add(task1);
        history.add(task2);
        history.add(task3);

        history.remove(1);

        assertIterableEquals(List.of(task3, task2), history.getHistory());
    }

    @Test
    @DisplayName("Удалить существующую задачу с конца")
    public void givenNonEmptyHistory_whenRemoveLast_thenGotTaskRemoved() {
        Task task1 = new Task(1, "task1", "desc1", null, null);
        Task task2 = new Task(2, "task2", "desc2", null, null);
        Task task3 = new Task(3, "task3", "desc3", null, null);
        history.add(task1);
        history.add(task2);
        history.add(task3);

        history.remove(3);

        assertIterableEquals(List.of(task2, task1), history.getHistory());
    }

    @Test
    @DisplayName("Удалить существующую задачу из середины")
    public void givenNonEmptyHistory_whenRemoveFromMiddle_thenGotTaskRemoved() {
        Task task1 = new Task(1, "task1", "desc1", null, null);
        Task task2 = new Task(2, "task2", "desc2", null, null);
        Task task3 = new Task(3, "task3", "desc3", null, null);
        history.add(task1);
        history.add(task2);
        history.add(task3);

        history.remove(2);

        assertIterableEquals(List.of(task3, task1), history.getHistory());
    }

    @Test
    @DisplayName("Удалить несколько экземпляров существующей задачи")
    public void givenManyTaskWithSameId_whenRemove_thenGotAllRemoved() {
        Task task1 = new Task(1, "task1", "desc1", null, null);
        Task task2 = new Task(2, "task2", "desc2", null, null);
        Task task3 = new Task(1, "task3", "desc3", null, null);
        history.add(task1);
        history.add(task2);
        history.add(task3);

        history.remove(1);

        assertIterableEquals(List.of(task2), history.getHistory());
    }

    @Test
    @DisplayName("Удалить несуществующую задачу из истории")
    public void givenNonExistingTask_whenRemove_thenNothingHappens() {
        Task task1 = new Task(1, "task1", "desc1", null, null);
        Task task2 = new Task(2, "task2", "desc2", null, null);
        history.add(task1);
        history.add(task2);

        history.remove(3);

        assertIterableEquals(List.of(task2, task1), history.getHistory());
    }
}