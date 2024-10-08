package kanban.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import kanban.http.ResponseEntity;
import kanban.http.util.PathMatcher;
import kanban.managers.TaskManager;
import kanban.tasks.Task;

import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {

    private final TaskManager manager;

    public PrioritizedHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    protected ResponseEntity handleGet(HttpExchange exchange) {

        PathMatcher matcher = PathMatcher.with(exchange.getRequestURI().getPath())
                .match("/prioritized");

        if (matcher.getMatchedPath() == null || !matcher.getMatchedPath().equals("/prioritized")) {
            return getBadRequest(exchange);
        } else {
            List<Task> tasks = manager.getPrioritizedTasks();
            return new ResponseEntity(200, tasks);
        }

    }
}
