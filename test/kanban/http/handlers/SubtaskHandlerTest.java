package kanban.http.handlers;

import kanban.tasks.Epic;
import kanban.tasks.Subtask;
import kanban.util.Tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static kanban.tasks.TaskStatus.DONE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SubtaskHandlerTest extends AbstractHandlerTest<Subtask> {

    @Override
    public String getResourcePath() {
        return domain + "/subtasks";
    }

    @Override
    public Subtask createTask() {
        Epic epic = createAndSaveEpic("epic;desc");
        return Tasks.createSubtask(String.format("task;desc;NEW;%s;2024-01-01 00:00:00;120", epic.getId()));
    }

    @Override
    public void updateTask(Subtask task) {
        task.setName("new sub");
        task.setDescription("new desc");
        task.setStatus(DONE);
        task.setDuration(Duration.ofMinutes(130));
        task.setStartTime(LocalDateTime.parse("2024-01-01T00:01:00"));
    }

    @Override
    public Subtask createAndSaveTask() {
        Subtask sub = createTask();
        manager.saveSubtask(sub);
        return sub;
    }

    @Override
    public List<Subtask> getAllTasks() {
        return manager.getSubtasks();
    }

    @Override
    public Optional<Subtask> getTaskById(int id) {
        return manager.getSubtaskById(id);
    }

    @Override
    public void assertTaskEquals(Subtask expectedSub, Subtask actualSub) {
        assertEquals(expectedSub.getName(),        actualSub.getName());
        assertEquals(expectedSub.getDescription(), actualSub.getDescription());
        assertEquals(expectedSub.getStatus(),      actualSub.getStatus());
        assertEquals(expectedSub.getDuration(),    actualSub.getDuration());
        assertEquals(expectedSub.getStartTime(),   actualSub.getStartTime());
        assertEquals(expectedSub.getEpicId(),      actualSub.getEpicId());
    }

    @Override
    public Subtask copyTask(Subtask sub) {
        return Tasks.copy(sub);
    }
}
