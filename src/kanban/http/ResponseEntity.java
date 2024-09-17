package kanban.http;

import com.google.gson.Gson;
import kanban.HttpTaskServer;

import java.util.HashMap;
import java.util.Map;

public class ResponseEntity {
    private static final Gson GSON = HttpTaskServer.getGson();

    private final int code;
    private final String message;
    private final Map<String, String> headers = new HashMap<>();

    public ResponseEntity(int code, Object message, Map<String, String> headers) {
        this.code = code;

        if (message == null) {
            this.message = null;
        } else if (message instanceof String) {
            this.message = (String)message;
        } else {
            this.message = GSON.toJson(message);
        }

        this.headers.putAll(headers);
        if (this.headers.get("Content-Type") == null && message != null) {
            if (message instanceof String) {
                this.headers.put("Content-Type", "text/html; charset=utf-8");
            } else {
                this.headers.put("Content-Type", "application/json; charset=utf-8");
            }
        }
    }

    public ResponseEntity(int code, Object message) {
        this(code, message, Map.of());
    }

    public ResponseEntity(int code) {
        this(code, null, Map.of());
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
