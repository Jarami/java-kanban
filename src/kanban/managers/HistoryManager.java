package kanban.managers;

import kanban.tasks.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);

    void remove(int id);

    void clear();

    List<Task> getHistory();
}
