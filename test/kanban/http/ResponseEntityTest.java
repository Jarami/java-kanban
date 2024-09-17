package kanban.http;

import com.google.gson.Gson;
import kanban.HttpTaskServer;
import kanban.tasks.Task;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResponseEntityTest {

    private final Gson gson = HttpTaskServer.getGson();

    @Test
    void givenResponse_whenGetCode_gotIt() {
        ResponseEntity response = new ResponseEntity(400, "Bad Request");
        assertEquals(400, response.getCode());
    }

    @Test
    void givenObject_whenGetMessage_gotJson() {
        Task task = new Task(1, "task", "desc", LocalDateTime.parse("2024-01-01T00:00:00"),
                Duration.ofMinutes(15));
        ResponseEntity response = new ResponseEntity(200, task);

        assertEquals(gson.toJson(task), response.getMessage());

        String expectedContentType = "application/json; charset=utf-8";
        String actualContentType = response.getHeaders().get("Content-Type");

        assertEquals(expectedContentType, actualContentType);
    }

    @Test
    void givenString_whenGetMessage_gotJson() {
        ResponseEntity response = new ResponseEntity(200, "hello");

        assertEquals("hello", response.getMessage());

        String expectedContentType = "text/html; charset=utf-8";
        String actualContentType = response.getHeaders().get("Content-Type");

        assertEquals(expectedContentType, actualContentType);
    }

    @Test
    void givenNull_whenGetMessage_gotJson() {
        Object message = null;
        ResponseEntity response = new ResponseEntity(200, message);

        assertNull(response.getMessage());
        assertNull(response.getHeaders().get("Content-Type"));
    }

    @Test
    void givenResponse_whenInitializeWithMap_gotHeaders() {
        ResponseEntity response = new ResponseEntity(200, null,
                Map.of("Content-Type", "text/html; charset=utf-8",
                       "Cache-Control", "max-age=12154"));

        String expectedContentType = "text/html; charset=utf-8";
        String actualContentType = response.getHeaders().get("Content-Type");

        assertEquals(expectedContentType, actualContentType);

        String expectedCacheControl = "max-age=12154";
        String actualCacheControl = response.getHeaders().get("Cache-Control");

        assertEquals(expectedCacheControl, actualCacheControl);
    }
}