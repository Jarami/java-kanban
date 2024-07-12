import java.util.LinkedList;
import java.util.List;

class InMemoryHistoryManager implements HistoryManager {
    private final int maxCapacity;
    private final List<Task> tasks;

    InMemoryHistoryManager(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        tasks = new LinkedList<>();
    }

    public synchronized void add(Task task) {
        if (tasks.size() >= maxCapacity) {
            tasks.removeFirst();
        }
        tasks.addLast(task);
    }

    public void clear() {
        tasks.clear();
    }

    public List<Task> getHistory() {
        return tasks;
    }
}