package kanban.repo;

import kanban.tasks.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static kanban.lib.TestAssertions.assertEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class InMemoryRepoTest {

    InMemoryRepo<Task> repo;

    @BeforeEach
    void setup() {
        repo = new InMemoryRepo<>();
    }

    @Test
    @DisplayName("должен сохранить задачу")
    void save() {
        Task task = new Task(1, "task", "desc", null, null);
        repo.save(task);
        assertEquals(task, repo.findById(1).orElseThrow());
    }

    @Test
    @DisplayName("должен выдавать список сохраненных задач")
    void saveAll() {
        Task task1 = new Task(1, "task1", "desc1", null, null);
        Task task2 = new Task(2, "task2", "desc2", null, null);

        repo.save(task1);
        repo.save(task2);

        Collection<Task> tasks = List.of(task1, task2);

        assertIterableEquals(tasks, repo.findAll());
    }

    @Test
    @DisplayName("должен выдавать нужную задачу по id")
    void testThatRepoReturnsCorrectTaskById() {
        Task task1 = new Task(1, "task1", "desc1", null, null);
        Task task2 = new Task(2, "task2", "desc2", null, null);

        repo.save(task1);
        repo.save(task2);

        assertEquals(task1, repo.findById(task1.getId()).orElseThrow());
        assertEquals(task2, repo.findById(task2.getId()).orElseThrow());
    }

    @Test
    @DisplayName("должен удалить все задачи")
    void delete() {
        Task task1 = new Task(1, "task1", "desc1", null, null);
        Task task2 = new Task(2, "task2", "desc2", null, null);

        repo.save(task1);
        repo.save(task2);
        repo.delete();

        assertEmpty(repo.findAll());
    }

    @Test
    @DisplayName("должен удалить определенную задачи по id")
    void deleteById() {
        Task task1 = new Task(1, "task1", "desc1", null, null);
        Task task2 = new Task(2, "task2", "desc2", null, null);
        Task task3 = new Task(3, "task3", "desc3", null, null);

        repo.save(task1);
        repo.save(task2);
        repo.save(task3);
        repo.deleteById(2);

        Collection<Task> tasks = List.of(task1, task3);

        assertIterableEquals(tasks, repo.findAll());
    }

}