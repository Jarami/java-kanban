import java.util.Objects;

public class Task {

    protected Integer id;
    protected String name;
    protected String description;
    protected TaskStatus status;

    public Task(String name, String description) {
        this(null, name, description, TaskStatus.NEW);
    }

    public Task(Integer id, String name, String description) {
        this(id, name, description, TaskStatus.NEW);
    }

    public Task(String name, String description, TaskStatus status) {
        this(null, name, description, status);
    }

    public Task(Integer id, String name, String description, TaskStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        if (task.id == null) return false;
        return task.id.equals(id);
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }
}
