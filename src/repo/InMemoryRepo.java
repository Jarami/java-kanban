package repo;

import tasks.Task;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

public class InMemoryRepo<T extends Task> implements TaskRepo<T> {
    private final Map<Integer, T> repo = new HashMap<>();

    @Override
    public void save(T task) {
        repo.put(task.getId(), task);
    }

    @Override
    public T findById(Integer taskId) {
        return repo.get(taskId);
    }

    @Override
    public Collection<T> findAll() {
        return repo.values();
    }

    @Override
    public void delete() {
        repo.clear();
    }

    @Override
    public void deleteById(Integer taskId) {
        repo.remove(taskId);
    }
}
