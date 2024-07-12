package managers;

import tasks.Task;

import java.util.LinkedList;
import java.util.List;

class InMemoryHistoryManager implements HistoryManager {

    public static final int DEFAULT_MAX_CAPACITY = 10;

    private final int maxCapacity;
    private final List<Task> tasks;

    InMemoryHistoryManager() {
        this(DEFAULT_MAX_CAPACITY);
    }

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