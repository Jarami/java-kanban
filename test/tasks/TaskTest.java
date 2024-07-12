package tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {

    Task task;

    @BeforeEach
    void setup() {
        task = new Task("name", "desc");
    }

    @Test
    @DisplayName("Установить имя")
    void setName() {
        String newName = "new name";
        task.setName(newName);
        assertEquals(newName, task.getName());
    }

    @Test
    @DisplayName("Установить описание")
    void setDescription() {
        String newDescription = "new desc";
        task.setDescription(newDescription);
        assertEquals(newDescription, task.getDescription());
    }

    @Test
    @DisplayName("Установить id")
    void setId() {
        int id = 1;
        task.setId(id);
        assertEquals(id, task.getId());
    }

    @Test
    @DisplayName("Установить статус")
    void setStatus() {
        TaskStatus newStatus = TaskStatus.DONE;
        task.setStatus(newStatus);
        assertEquals(newStatus, task.getStatus());
    }

    @Test
    @DisplayName("Проверить, что задачи равны, если их id равны")
    void testThatTasksAreEqualIfTheirIdEqual() {
        Task task1 = new Task(1, "task1", "desc1", TaskStatus.NEW);
        Task task2 = new Task(1, "task2", "desc2", TaskStatus.DONE);
        assertEquals(task1, task2);
    }
}