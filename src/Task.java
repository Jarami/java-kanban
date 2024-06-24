import java.util.Objects;

public class Task {

    private static int taskCounter = 0;

    private final String name;
    private final String description;
    private final int uuid;
    private TaskStatus status;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.uuid = taskCounter;

        taskCounter++;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getUuid() {
        return uuid;
    }

    public TaskStatus getStatus() {
        return status;
    }
}
