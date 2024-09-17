package kanban.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kanban.http.ResponseEntity;
import kanban.http.util.PathMatcher;
import kanban.managers.TaskManager;
import kanban.tasks.Task;

import java.util.List;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    TaskManager manager;
    public HistoryHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    protected ResponseEntity handleGet(HttpExchange exchange) {

        PathMatcher matcher = PathMatcher.with(exchange.getRequestURI().getPath())
                .match("/history");

        if (matcher.getMatchedPath() == null || !matcher.getMatchedPath().equals("/history")) {
            return getBadRequest(exchange);
        } else {
            List<Task> tasks = manager.getHistory();
            return new ResponseEntity(200, tasks);
        }

    }
}
