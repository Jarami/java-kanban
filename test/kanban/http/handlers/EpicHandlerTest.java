package kanban.http.handlers;

import com.google.gson.Gson;
import kanban.HttpTaskServer;
import kanban.managers.Managers;
import kanban.managers.TaskManager;
import kanban.tasks.Epic;
import kanban.tasks.Subtask;
import kanban.tasks.Task;
import kanban.tasks.TaskStatus;
import kanban.util.Tasks;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EpicHandlerTest {

    protected static final Gson GSON = HttpTaskServer.getGson();
    protected static final String resourcePath = "http://localhost:" + HttpTaskServer.PORT + "/epics";
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
    @DisplayName("создаем эпик")
    public void givenNewEpic_whenSave_got201() throws IOException, InterruptedException {
        Epic epic = createEpic();
        HttpResponse<String> resp = Request.post(resourcePath, epic);

        assertEquals(201, resp.statusCode());

        Epic epicFromManager = getAllTasks().getFirst();
        assertNotNull(epicFromManager.getId());
        assertTaskEquals(epic, epicFromManager);
    }

    @Test
    @DisplayName("получаем все эпики")
    public void givenTasksSaved_whenGetTasks_gotIt() throws IOException, InterruptedException {
        createTestSuite();
        HttpResponse<String> resp = Request.get(resourcePath);

        String expectedBody = GSON.toJson(getAllTasks());
        String actualBody = resp.body();
        assertEquals(expectedBody, actualBody);
    }

    @Test
    @DisplayName("получаем эпик по id")
    public void givenTasksSaved_whenGetTaskById_gotIt() throws IOException, InterruptedException {
        createTestSuite();

        Epic epic = getAllTasks().getFirst();
        HttpResponse<String> resp = Request.get(resourcePath + "/" + epic.getId());

        String expectedBody = GSON.toJson(epic);
        String actualBody = resp.body();
        assertEquals(expectedBody, actualBody);
    }

    @Test
    @DisplayName("если запрашиваем задачу по несуществующему id, то получаем 404")
    public void givenTasksSaved_whenGetNonExistingTaskById_got404() throws IOException, InterruptedException {
        Epic epic = createAndSaveEpic();
        int wrongId = epic.getId() + 1;

        HttpResponse<String> resp = Request.get(resourcePath + "/" + wrongId);

        assertEquals(404, resp.statusCode());
    }

    @Test
    @DisplayName("удаляем эпик")
    public void givenTask_whenDelete_got200() throws IOException, InterruptedException {
        createTestSuite();
        List<Epic> epics = getAllTasks();
        Epic epic = epics.getFirst();
        HttpResponse<String> resp = Request.delete(resourcePath + "/" + epic.getId());

        assertEquals(200, resp.statusCode());

        List<Epic> newEpics = getAllTasks();
        assertEquals(epics.size() - 1, newEpics.size());

        List<Integer> ids = newEpics.stream().map(Task::getId).toList();
        assertFalse(ids.contains(epic.getId()));
    }

    private Epic createEpic() {
        return Tasks.createEpic("task;desc");
    }

    public Epic createAndSaveEpic() {
        Epic epic = createEpic();
        manager.saveTask(epic);
        return epic;
    }

    private List<Epic> getAllTasks() {
        return manager.getEpics();
    }

    public void assertTaskEquals(Epic expectedEpic, Epic actualEpic) {
        assertEquals(expectedEpic.getName(),        actualEpic.getName());
        assertEquals(expectedEpic.getDescription(), actualEpic.getDescription());
        assertEquals(expectedEpic.getStatus(),      actualEpic.getStatus());
        assertEquals(expectedEpic.getDuration(),    actualEpic.getDuration());
        assertEquals(expectedEpic.getStartTime(),   actualEpic.getStartTime());
        assertIterableEquals(expectedEpic.getSubtasksId(), actualEpic.getSubtasksId());
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
