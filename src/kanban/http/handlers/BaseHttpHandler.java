package kanban.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kanban.HttpTaskServer;
import kanban.http.ResponseEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler implements HttpHandler {

    protected static Gson GSON = HttpTaskServer.getGson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();
        System.out.println("Началась обработка " + exchange.getRequestURI() + " (" + method + ") от клиента.");

        try {
            ResponseEntity response;
            switch (method) {
                case "GET":
                    response = handleGet(exchange);
                    break;
                case "POST":
                    response = handlePost(exchange);
                    break;
                case "DELETE":
                    response = handleDelete(exchange);
                    break;
                default:
                    response = getBadRequest(exchange);
            }

            sendResponse(exchange, response);

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, new ResponseEntity(500, "Ошибка при обработке запроса"));
        }
    }

    protected void sendResponse(HttpExchange exchange, ResponseEntity response) throws IOException {

        response.getHeaders()
                .forEach((header, value) ->
                        exchange.getResponseHeaders().add(header, value));

        if (response.getMessage() == null) {
            exchange.sendResponseHeaders(response.getCode(), -1);
        } else {
            byte[] resp = response.getMessage().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(response.getCode(), resp.length);
            exchange.getResponseBody().write(resp);
        }

        exchange.close();
    }

    protected ResponseEntity handleGet(HttpExchange h) {
        return getBadRequest(h);
    }

    protected ResponseEntity handlePost(HttpExchange h) throws IOException {
        return getBadRequest(h);
    }

    protected ResponseEntity handleDelete(HttpExchange h) {
        return getBadRequest(h);
    }

    protected ResponseEntity getBadRequest(HttpExchange h) {
        return new ResponseEntity(400,"Неизвестный запрос " + h.getRequestURI() + " ("
                + h.getRequestMethod() + ")");
    }
}
