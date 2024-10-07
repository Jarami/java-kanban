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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

abstract class AbstractHandlerTest<T extends Task> {

    protected static final Gson GSON = HttpTaskServer.getGson();
    protected static final String domain = "http://localhost:" + HttpTaskServer.PORT;
    protected static TaskManager manager;
    protected static HttpTaskServer server;

    abstract String getResourcePath();
    abstract T createTask();
    abstract void updateTask(T task);
    abstract T createAndSaveTask();
    abstract List<T> getAllTasks();
    abstract Optional<T> getTaskById(int id);
    abstract void assertTaskEquals(T expectedTask, T actualTask);
    abstract T copyTask(T task);

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
    @DisplayName("создаем задачу")
    public void givenNewTask_whenSave_got201() throws IOException, InterruptedException {
        T task = createTask();
        HttpResponse<String> resp = Request.post(getResourcePath(), task);

        assertEquals(201, resp.statusCode());

        T originalTask = getAllTasks().getFirst();
        assertNotNull(originalTask.getId());
        assertTaskEquals(task, originalTask);
    }

    @Test
    @DisplayName("если создаваемая задача пересекается с другими, то 406")
    public void givenTaskInterceptionWithOther_whenSave_got406() throws IOException, InterruptedException {
        T task = createAndSaveTask();
        T newTask = copyTask(task);
        newTask.setId(null);

        HttpResponse<String> resp = Request.post(getResourcePath(), newTask);

        assertEquals(406, resp.statusCode());

        int actualTaskNumber = getAllTasks().size();
        assertEquals(1, actualTaskNumber);
    }

    @Test
    @DisplayName("получаем все задачи")
    public void givenTasksSaved_whenGetTasks_gotIt() throws IOException, InterruptedException {
        createTestSuite();
        HttpResponse<String> resp = Request.get(getResourcePath());

        String expectedBody = GSON.toJson(getAllTasks());
        String actualBody = resp.body();
        assertEquals(expectedBody, actualBody);
    }

    @Test
    @DisplayName("получаем задачу по id")
    public void givenTasksSaved_whenGetTaskById_gotIt() throws IOException, InterruptedException {
        createTestSuite();

        T task = getAllTasks().getFirst();
        HttpResponse<String> resp = Request.get(getResourcePath() + "/" + task.getId());

        String expectedBody = GSON.toJson(task);
        String actualBody = resp.body();
        assertEquals(expectedBody, actualBody);
    }

    @Test
    @DisplayName("если запрашиваем задачу по несуществующему id, то получаем 404")
    public void givenTasksSaved_whenGetNonExistingTaskById_got404() throws IOException, InterruptedException {
        T task = createAndSaveTask();
        int wrongId = task.getId() + 1;

        HttpResponse<String> resp = Request.get(getResourcePath() + "/" + wrongId);

        assertEquals(404, resp.statusCode());
    }

    @Test
    @DisplayName("обновляем задачу")
    public void givenExistingTask_whenUpdate_got200() throws IOException, InterruptedException {
        T task = createAndSaveTask();
        T newTask = copyTask(task);
        updateTask(newTask);

        HttpResponse<String> resp = Request.post(getResourcePath(), newTask);

        assertEquals(201, resp.statusCode());

        T taskFromManager = getTaskById(newTask.getId()).orElseThrow();
        assertTaskEquals(newTask, taskFromManager);
    }

    @Test
    @DisplayName("если обновляемая задача пересекается с другими, то 406")
    public void givenNewTaskInterceptingWithOther_whenSave_got406() throws IOException, InterruptedException {
        createTestSuite();
        List<T> tasks = getAllTasks();
        T task1 = tasks.get(0);
        T task2 = copyTask(tasks.get(1));
        LocalDateTime originalStartTime = task2.getStartTime();
        task2.setStartTime(task1.getStartTime()); // теперь task2 будет пересекаться с task1

        HttpResponse<String> resp = Request.post(getResourcePath(), task2);

        assertEquals(406, resp.statusCode());

        T taskFromManager = getTaskById(task2.getId()).orElseThrow();
        assertEquals(originalStartTime, taskFromManager.getStartTime());
    }

    @Test
    @DisplayName("удаляем задачу")
    public void givenTask_whenDelete_got200() throws IOException, InterruptedException {
        createTestSuite();
        List<T> tasks = getAllTasks();
        T task = tasks.getFirst();
        HttpResponse<String> resp = Request.delete(getResourcePath() + "/" + task.getId());

        assertEquals(200, resp.statusCode());

        List<T> newTasks = getAllTasks();
        assertEquals(tasks.size() - 1, newTasks.size());

        List<Integer> ids = newTasks.stream().map(Task::getId).toList();
        assertFalse(ids.contains(task.getId()));
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

