package kanban.http.handlers;

import com.google.gson.Gson;
import kanban.HttpTaskServer;
import kanban.managers.Managers;
import kanban.managers.TaskManager;
import kanban.tasks.Epic;
import kanban.tasks.Subtask;
import kanban.tasks.Task;
import kanban.util.Tasks;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrioritizedHandlerTest {
    protected static final Gson GSON = HttpTaskServer.getGson();
    protected static final String resourcePath = "http://localhost:" + HttpTaskServer.PORT + "/prioritized";
    protected static TaskManager manager;
    protected static HttpTaskServer server;

    @BeforeAll
    static void start() throws IOException {
        manager = Managers.getDefault();
        server = new HttpTaskServer(manager);
        server.start();
    }

    @AfterAll
    static void shutdown() {
        server.stop();
    }

    @AfterEach
    void clean() {
        manager.removeTasks();
        manager.removeEpics();
    }

    @Test
    @DisplayName("получаем все задачи")
    public void givenTasksSaved_whenGetTasks_gotIt() throws IOException, InterruptedException {
        createTestSuite();
        HttpResponse<String> resp = Request.get(resourcePath);

        String expectedBody = GSON.toJson(getAllTasks());
        String actualBody = resp.body();
        assertEquals(expectedBody, actualBody);
    }

    private List<Task> getAllTasks() {
        return manager.getPrioritizedTasks();
    }

    protected void createTestSuite() {
        createAndSaveTask("task1;desc1;NEW;2024-01-01 00:00:00;120");
        createAndSaveTask("task2;desc2;NEW;2024-01-02 00:00:00;120");
        Epic epic1 = createAndSaveEpic("epic;desc3");
        createAndSaveEpic("epic;desc4");
        createAndSaveSubtask("sub1;desc5;NEW;%s;2024-01-03 00:00:00;120", epic1.getId());
        createAndSaveSubtask("sub2;desc6;NEW;%s;2024-01-04 00:00:00;120", epic1.getId());
    }

    protected Task createAndSaveTask(String formattedTask) {
        Task task = Tasks.createTask(formattedTask);
        manager.saveTask(task);
        return task;
    }

    protected Epic createAndSaveEpic(String formattedEpic) {
        Epic epic = Tasks.createEpic(formattedEpic);
        manager.saveEpic(epic);
        return epic;
    }

    protected Subtask createAndSaveSubtask(String formattedEpic, int epicId) {
        Subtask sub = Tasks.createSubtask(String.format(formattedEpic, epicId));
        manager.saveSubtask(sub);
        return sub;
    }
}
