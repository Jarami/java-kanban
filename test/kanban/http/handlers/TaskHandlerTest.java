package kanban.http.handlers;

import kanban.tasks.Task;
import kanban.util.Tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static kanban.tasks.TaskStatus.DONE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskHandlerTest extends AbstractHandlerTest<Task> {


    @Override
    public String getResourcePath() {
        return domain + "/tasks";
    }

    @Override
    public Task createTask() {
        return Tasks.createTask("task;desc;NEW;2024-01-01 00:00:00;120");
    }

    @Override
    public void updateTask(Task task) {
        task.setName("new name");
        task.setDescription("new desc");
        task.setStatus(DONE);
        task.setDuration(Duration.ofMinutes(130));
        task.setStartTime(LocalDateTime.parse("2024-01-01T00:01:00"));
    }

    @Override
    public Task createAndSaveTask() {
        Task task = createTask();
        manager.saveTask(task);
        return task;
    }

    @Override
    public List<Task> getAllTasks() {
        return manager.getTasks();
    }

    @Override
    public Optional<Task> getTaskById(int id) {
        return manager.getTaskById(id);
    }

    @Override
    public void assertTaskEquals(Task expectedTask, Task actualTask) {
        assertEquals(expectedTask.getName(),        actualTask.getName());
        assertEquals(expectedTask.getDescription(), actualTask.getDescription());
        assertEquals(expectedTask.getStatus(),      actualTask.getStatus());
        assertEquals(expectedTask.getDuration(),    actualTask.getDuration());
        assertEquals(expectedTask.getStartTime(),   actualTask.getStartTime());
    }

    @Override
    public Task copyTask(Task task) {
        return Tasks.copy(task);
    }
}

