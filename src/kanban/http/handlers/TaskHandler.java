package kanban.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import kanban.exceptions.ManagerSaveException;
import kanban.http.ResponseEntity;
import kanban.http.util.PathMatcher;
import kanban.managers.TaskManager;
import kanban.tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TaskHandler extends BaseHttpHandler {

    private final TaskManager manager;

    public TaskHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    protected ResponseEntity handleGet(HttpExchange exchange) {

        PathMatcher matcher = PathMatcher.with(exchange.getRequestURI().getPath())
                .match("/tasks")
                .match("/tasks/{id}");

        if (matcher.getMatchedPath() == null) {
            return getBadRequest(exchange);
        }

        return switch (matcher.getMatchedPath()) {
            case "/tasks" -> {
                List<Task> tasks = manager.getTasks();
                yield new ResponseEntity(200, tasks);
            }
            case "/tasks/{id}" -> {
                String id = matcher.getPathParameters().getFirst();
                yield manager.getTaskById(Integer.parseInt(id))
                        .map(t -> new ResponseEntity(200, t))
                        .orElse(new ResponseEntity(404, "Задача не найдена"));
            }
            default -> getBadRequest(exchange);
        };

    }

    @Override
    public ResponseEntity handlePost(HttpExchange exchange) throws IOException {
        PathMatcher matcher = PathMatcher.with(exchange.getRequestURI().getPath())
                .match("/tasks");

        if (matcher.getMatchedPath() == null || !matcher.getMatchedPath().equals("/tasks")) {
            return getBadRequest(exchange);
        }

        InputStream inputStream = exchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        Task task = GSON.fromJson(body, Task.class);

        try {
            if (task.getId() == null) {
                manager.saveTask(task);
            } else {
                manager.updateTask(task);
            }
            return new ResponseEntity(201);

        } catch (ManagerSaveException e) {
            return new ResponseEntity(406, e.getMessage());
        }
    }

    @Override
    protected ResponseEntity handleDelete(HttpExchange exchange) {

        PathMatcher matcher = PathMatcher.with(exchange.getRequestURI().getPath())
                .match("/tasks/{id}");

        if (matcher.getMatchedPath() == null || !matcher.getMatchedPath().equals("/tasks/{id}")) {
            return getBadRequest(exchange);
        } else {
            String id = matcher.getPathParameters().getFirst();
            manager.removeTaskById(Integer.parseInt(id));
            return new ResponseEntity(200);
        }
    }

}
