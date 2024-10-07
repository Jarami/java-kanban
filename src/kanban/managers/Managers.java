package kanban.managers;

import java.io.IOException;
import java.nio.file.Files;

public class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getFileManager() throws IOException {
        return new FileBackedTaskManager(Files.createTempFile("tasks", ".csv"));
    }
}
