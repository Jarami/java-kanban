package repo;

import tasks.Task;

import java.util.Collection;
import java.util.Optional;

public interface TaskRepo<T extends Task> {

    void save(T task);

    Optional<T> findById(Integer taskId);

    Collection<T> findAll();

    void delete();

    void deleteById(Integer taskId);
}
