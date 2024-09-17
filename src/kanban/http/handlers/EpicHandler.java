package kanban.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kanban.exceptions.ManagerSaveException;
import kanban.http.ResponseEntity;
import kanban.http.util.PathMatcher;
import kanban.managers.TaskManager;
import kanban.tasks.Epic;
import kanban.tasks.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    TaskManager manager;

    public EpicHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    protected ResponseEntity handleGet(HttpExchange exchange) {

        PathMatcher matcher = PathMatcher.with(exchange.getRequestURI().getPath())
                .match("/epics")
                .match("/epics/{id}")
                .match("/epics/{id}/subtasks");

        if (matcher.getMatchedPath() == null) {
            return getBadRequest(exchange);
        }

        return switch (matcher.getMatchedPath()) {
            case "/epics" -> {
                List<Epic> tasks = manager.getEpics();
                yield new ResponseEntity(200, tasks);
            }
            case "/epics/{id}" -> {
                String id = matcher.getPathParameters().getFirst();
                yield manager.getEpicById(Integer.parseInt(id))
                        .map(t -> new ResponseEntity(200, t))
                        .orElse(new ResponseEntity(404, "Задача c id = " + id + "не найдена"));
            }
            case "/epics/{id}/subtasks" -> {
                String id = matcher.getPathParameters().getFirst();
                Optional<Epic> epic = manager.getEpicById(Integer.parseInt(id));
                if (epic.isPresent()) {
                    List<Subtask> subs = manager.getSubtasksOfEpic(epic.get());
                    yield new ResponseEntity(200, subs);
                } else {
                    yield new ResponseEntity(404, "Задача c id = " + id + "не найдена");
                }
            }
            default -> getBadRequest(exchange);
        };

    }

    @Override
    public ResponseEntity handlePost(HttpExchange exchange) throws IOException {
        PathMatcher matcher = PathMatcher.with(exchange.getRequestURI().getPath())
                .match("/epics");

        if (matcher.getMatchedPath() == null || !matcher.getMatchedPath().equals("/epics")) {
            return getBadRequest(exchange);
        }

        InputStream inputStream = exchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        Epic epic = GSON.fromJson(body, Epic.class);

        try {
            manager.saveEpic(epic);
            return new ResponseEntity(201);
        } catch (ManagerSaveException e) {
            return new ResponseEntity(406, e.getMessage());
        }
    }

    @Override
    protected ResponseEntity handleDelete(HttpExchange exchange) {

        PathMatcher matcher = PathMatcher.with(exchange.getRequestURI().getPath())
                .match("/epics/{id}");

        if (matcher.getMatchedPath() == null || !matcher.getMatchedPath().equals("/epics/{id}")) {
            return getBadRequest(exchange);
        } else {
            String id = matcher.getPathParameters().getFirst();
            manager.removeEpicById(Integer.parseInt(id));
            return new ResponseEntity(200);
        }
    }
}
