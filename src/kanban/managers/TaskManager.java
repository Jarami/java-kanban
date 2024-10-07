package kanban.managers;

import kanban.tasks.Epic;
import kanban.tasks.Subtask;
import kanban.tasks.Task;

import java.util.List;
import java.util.Optional;

public interface TaskManager {
    // Сохранение
    int saveTask(Task task);

    int saveEpic(Epic epic);

    int saveSubtask(Subtask subtask);

    // Получение
    List<Task> getTasks();

    Optional<Task> getTaskById(int id);

    List<Epic> getEpics();

    Optional<Epic> getEpicById(int id);

    List<Subtask> getSubtasks();

    Optional<Subtask> getSubtaskById(int id);

    List<Subtask> getSubtasksOfEpic(Epic epic);

    Epic getEpicOfSubtask(Subtask subtask);

    // Обновление
    void updateTask(Task task);

    void updateEpic(Epic epic);

    // При обновлении подзадачи нужно обновить родительский эпик
    void updateSubtask(Subtask subtask);

    // Удаление
    void removeTasks();

    void removeTaskById(int id);

    void removeEpics();

    // При удалении эпика все его подзадачи тоже удаляются
    void removeEpicById(int id);

    // При удалении подзадач из хранилища также нужно удалить их у эпиков
    void removeSubtasks();

    // При удалении подзадачи нужно обновить родительский эпик
    void removeSubtaskById(int id);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
