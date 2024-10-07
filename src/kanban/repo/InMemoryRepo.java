package kanban.repo;

import kanban.tasks.Task;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryRepo<T extends Task> implements TaskRepo<T> {

    private final Map<Integer, T> repo = new LinkedHashMap<>();

    @Override
    public void save(T task) {
        repo.put(task.getId(), task);
    }

    @Override
    public Optional<T> findById(Integer taskId) {
        T task = repo.get(taskId);
        return task == null ? Optional.empty() : Optional.of(task);
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
