package managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import static lib.TestAssertions.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    HistoryManager history;

    @BeforeEach
    void setup() {
        history = Managers.getDefaultHistory();
    }

    @Test
    @DisplayName("Добавить три разных задачи")
    void addDifferentTasks() {
        Task task = new Task("task", "task desc");
        history.add(task);
        Epic epic = new Epic("epic", "epic desc");
        history.add(epic);
        Subtask sub = new Subtask("sub", "sub desc", epic);
        history.add(sub);

        List<Task> actualTasks = history.getHistory();
        List<Task> expectedTasks = List.of(task, epic, sub);
        assertIterableEquals(expectedTasks, actualTasks);
    }

    @Test
    @DisplayName("Добавить десять задач")
    void testThatTenTasksCanBeAdded() {

        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasks.add(new Task("task" + i, "desc of task " + i));
            history.add(tasks.get(i));
        }

        List<Task> actualTasks = history.getHistory();

        assertIterableEquals(tasks, actualTasks);
    }

    @Test
    @DisplayName("Добавить одиннадцать задач")
    void testThatElevenTasksCannotBeAdded() {

        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            tasks.add(new Task("task" + i, "desc of task " + i));
            history.add(tasks.get(i));
        }

        List<Task> actualTasks = history.getHistory();
        List<Task> expectedTasks = tasks.subList(1, 11);

        assertIterableEquals(expectedTasks, actualTasks);
    }

    @Test
    @DisplayName("Добавить три разных задачи")
    void clearHistory() {
        Task task = new Task("task", "task desc");
        history.add(task);

        history.clear();

        assertEmpty(history.getHistory());
    }
}