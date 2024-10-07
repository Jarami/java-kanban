package kanban.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import kanban.exceptions.ManagerSaveException;
import kanban.http.ResponseEntity;
import kanban.http.util.PathMatcher;
import kanban.managers.TaskManager;
import kanban.tasks.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler {

    private final TaskManager manager;

    public SubtaskHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    protected ResponseEntity handleGet(HttpExchange exchange) {

        PathMatcher matcher = PathMatcher.with(exchange.getRequestURI().getPath())
                .match("/subtasks")
                .match("/subtasks/{id}");

        if (matcher.getMatchedPath() == null) {
            return getBadRequest(exchange);
        }

        return switch (matcher.getMatchedPath()) {
            case "/subtasks" -> {
                List<Subtask> subs = manager.getSubtasks();
                yield new ResponseEntity(200, subs);
            }
            case "/subtasks/{id}" -> {
                String id = matcher.getPathParameters().getFirst();
                yield manager.getSubtaskById(Integer.parseInt(id))
                        .map(t -> new ResponseEntity(200, t))
                        .orElse(new ResponseEntity(404, "Задача не найдена"));
            }
            default -> getBadRequest(exchange);
        };

    }

    @Override
    public ResponseEntity handlePost(HttpExchange exchange) throws IOException {

        PathMatcher matcher = PathMatcher.with(exchange.getRequestURI().getPath())
                .match("/subtasks");

        if (matcher.getMatchedPath() == null || !matcher.getMatchedPath().equals("/subtasks")) {
            return getBadRequest(exchange);
        }

        InputStream inputStream = exchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        Subtask sub = GSON.fromJson(body, Subtask.class);

        try {
            if (sub.getId() == null) {
                manager.saveSubtask(sub);
            } else {
                manager.updateSubtask(sub);
            }
            return new ResponseEntity(201);

        } catch (ManagerSaveException e) {
            return new ResponseEntity(406, e.getMessage());
        }
    }

    @Override
    protected ResponseEntity handleDelete(HttpExchange exchange) {

        PathMatcher matcher = PathMatcher.with(exchange.getRequestURI().getPath())
                .match("/subtasks/{id}");

        if (matcher.getMatchedPath() == null || !matcher.getMatchedPath().equals("/subtasks/{id}")) {
            return getBadRequest(exchange);
        } else {
            String id = matcher.getPathParameters().getFirst();
            manager.removeSubtaskById(Integer.parseInt(id));
            return new ResponseEntity(200);
        }
    }
}
