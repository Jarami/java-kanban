package repo;

import tasks.Task;

import java.util.Collection;

public interface TaskRepo<T extends Task> {

    void save(T task);

    T findById(Integer taskId);

    Collection<T> findAll();

    void delete();

    void deleteById(Integer taskId);
}
