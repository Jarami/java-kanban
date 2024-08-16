package managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static lib.TestAssertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static tasks.TaskStatus.*;
import static tasks.TaskType.*;

class FileBackedTaskManagerTest {

    private Path taskFile;
    private TaskManager manager;

    @BeforeEach
    public void setup() throws IOException {
        taskFile = Files.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(taskFile);
    }

    @Test
    void testThatManagerIsLoadedFromFile() throws IOException {

        Task task1 = createAndSaveTask("task1", "desc of task1", NEW);
        Task task2 = createAndSaveTask("task2", "desc of task2", IN_PROGRESS);
        Epic epic1 = createAndSaveEpic("epic1", "desc of epic1");
        Epic epic2 = createAndSaveEpic("epic2", "desc of epic2");
        Subtask sub1 = createAndSaveSubtask("sub 0", "sub 0 desc", DONE, epic1);
        Subtask sub2 = createAndSaveSubtask("sub 1", "sub 1 desc", DONE, epic1);
        Subtask sub3 = createAndSaveSubtask("sub 2", "sub 2 desc", DONE, epic1);

        TaskManager manager2 = FileBackedTaskManager.loadFromFile(taskFile);

        assertTaskEquals(task1, manager2.getTaskById(task1.getId()));
        assertTaskEquals(task2, manager2.getTaskById(task2.getId()));
        assertEpicEquals(epic1, manager2.getEpicById(epic1.getId()));
        assertEpicEquals(epic2, manager2.getEpicById(epic2.getId()));
        assertSubtaskEquals(sub1, manager2.getSubtaskById(sub1.getId()));
        assertSubtaskEquals(sub2, manager2.getSubtaskById(sub2.getId()));
        assertSubtaskEquals(sub3, manager2.getSubtaskById(sub3.getId()));
        assertEquals(7, manager2.getTasks().size() + manager2.getEpics().size()
                + manager2.getSubtasks().size());
    }

    @Test
    public void testThatManagerCreatedFromEmptyFileHasNoTasks() throws IOException {
        Path taskFile = Files.createTempFile("tasks", ".csv");
        TaskManager manager = FileBackedTaskManager.loadFromFile(taskFile);

        assertEmpty(manager.getTasks());
        assertEmpty(manager.getEpics());
        assertEmpty(manager.getSubtasks());
    }

    @Test
    void testThatManagerSavesTaskToFile() throws IOException {
        Task task = new Task("task", "desc of task");
        manager.saveTask(task);

        String line = readLastLine();
        String expectedLine = join(task.getId(), TASK, task.getName(), task.getStatus(), task.getDescription());
        assertEquals(expectedLine, line);
    }

    @Test
    void testThatManagerSavesEpicToFile() throws IOException {
        Epic epic = new Epic("epic", "desc of epic");
        manager.saveEpic(epic);

        String line = readLastLine();
        String expectedLine = join(epic.getId(), EPIC, epic.getName(), epic.getStatus(), epic.getDescription());
        assertEquals(expectedLine, line);
    }

    @Test
    void testThatManagerSavesSubtaskToFile() throws IOException {
        Epic epic = new Epic("epic", "desc of epic");
        manager.saveEpic(epic);
        Subtask sub = new Subtask("sub", "desc of sub", epic);
        manager.saveSubtask(sub);

        String line = readLastLine();
        String expectedLine = join(sub.getId(), SUBTASK, sub.getName(), sub.getStatus(), sub.getDescription(),
                sub.getEpicId());
        assertEquals(expectedLine, line);
    }

    @Test
    void testThatManagerUpdatesTaskInFile() throws IOException {
        Task task = new Task("task", "desc of task");
        manager.saveTask(task);

        task.setName("new task");
        manager.updateTask(task);

        String line = readLastLine();
        String expectedLine = join(task.getId(), TASK, task.getName(), task.getStatus(), task.getDescription());
        assertEquals(expectedLine, line);
    }

    @Test
    void testThatManagerUpdatesEpicInFile() throws IOException {
        Epic epic = new Epic("epic", "desc of epic");
        manager.saveEpic(epic);

        epic.setName("new epic");
        manager.updateEpic(epic);

        String line = readLastLine();
        String expectedLine = join(epic.getId(), EPIC, epic.getName(), epic.getStatus(), epic.getDescription());
        assertEquals(expectedLine, line);
    }

    @Test
    void testThatManagerUpdatesSubtaskToFile() throws IOException {
        Epic epic = new Epic("epic", "desc of epic");
        manager.saveEpic(epic);
        Subtask sub = new Subtask("sub", "desc of sub", epic);
        manager.saveSubtask(sub);

        sub.setName("new sub");
        manager.updateSubtask(sub);

        String line = readLastLine();
        String expectedLine = join(sub.getId(), SUBTASK, sub.getName(), sub.getStatus(), sub.getDescription(),
                sub.getEpicId());
        assertEquals(expectedLine, line);
    }

    @Test
    void testThatManagerRemovesTasksFromFile() throws IOException {
        createAndSaveTasks(3);
        Epic epic1 = createAndSaveEpic();
        Epic epic2 = createAndSaveEpic();
        List<Subtask> subs = createAndSaveSubtasks(epic1, 2);

        manager.removeTasks();

        Set<Integer> actualIds = Set.copyOf(readTaskId());
        Set<Integer> expectedIds = Set.of(epic1.getId(), epic2.getId(), subs.get(0).getId(), subs.get(1).getId());
        assertEquals(expectedIds, actualIds);
    }

    @Test
    void testThatManagerRemovesTaskByIdFromFile() throws IOException {
        List<Task> tasks = createAndSaveTasks(2);
        Epic epic1 = createAndSaveEpic();
        Subtask sub = createAndSaveSubtask(epic1);

        Task task = tasks.get(1);
        manager.removeTaskById(task.getId());

        Set<Integer> actualIds = Set.copyOf(readTaskId());
        Set<Integer> expectedIds = Set.of(tasks.get(0).getId(), epic1.getId(), sub.getId());
        assertEquals(expectedIds, actualIds);
    }

    @Test
    void testThatManagerRemovesEpicsFromFile() throws IOException {
        List<Task> tasks = createAndSaveTasks(3);
        createAndSaveEpicWithSubtasks(2);

        manager.removeEpics();

        Set<Integer> actualIds = Set.copyOf(readTaskId());
        Set<Integer> expectedIds = Set.of(tasks.get(0).getId(), tasks.get(1).getId(), tasks.get(2).getId());
        assertEquals(expectedIds, actualIds);
    }

    @Test
    void testThatManagerRemovesEpicByIdFromFile() throws IOException {
        List<Task> tasks = createAndSaveTasks(2);
        Epic epic1 = createAndSaveEpic();
        Epic epic2 = createAndSaveEpic();
        Subtask sub = createAndSaveSubtask(epic2);

        manager.removeEpicById(epic1.getId());

        Set<Integer> actualIds = Set.copyOf(readTaskId());
        Set<Integer> expectedIds = Set.of(tasks.get(0).getId(), tasks.get(1).getId(), epic2.getId(), sub.getId());
        assertEquals(expectedIds, actualIds);
    }

    @Test
    void testThatManagerRemovesSubtasksFromFile() throws IOException {
        List<Task> tasks = createAndSaveTasks(2);
        Epic epic1 = createAndSaveEpic();
        createAndSaveSubtasks(epic1, 2);
        Epic epic2 = createAndSaveEpic();
        createAndSaveSubtasks(epic2, 3);

        manager.removeSubtasks();

        Set<Integer> actualIds = Set.copyOf(readTaskId());
        Set<Integer> expectedIds = Set.of(tasks.get(0).getId(), tasks.get(1).getId(), epic1.getId(), epic2.getId());
        assertEquals(expectedIds, actualIds);
    }

    @Test
    void testThatManagerRemovesSubtaskByIdFromFile() throws IOException {
        List<Task> tasks = createAndSaveTasks(2);
        Epic epic = createAndSaveEpic();
        List<Subtask> subs = createAndSaveSubtasks(epic, 2);

        manager.removeSubtaskById(subs.get(1).getId());

        Set<Integer> actualIds = Set.copyOf(readTaskId());
        Set<Integer> expectedIds = Set.of(tasks.get(0).getId(), tasks.get(1).getId(), epic.getId(), subs.get(0).getId());
        assertEquals(expectedIds, actualIds);
    }

    private String join(Object... objects) {
        return Arrays.stream(objects)
                .map(String::valueOf)
                .collect(Collectors.joining(FileBackedTaskManager.SEPARATOR));
    }

    private String readLastLine() throws IOException {
        return Files.readAllLines(taskFile, StandardCharsets.UTF_8).getLast();
    }

    private List<Integer> readTaskId() throws IOException {
        return Files.readAllLines(taskFile, StandardCharsets.UTF_8)
                .stream()
                .skip(1)
                .map(line -> line.split(FileBackedTaskManager.SEPARATOR))
                .map(chunks -> Integer.parseInt(chunks[0]))
                .toList();
    }

    private Task createAndSaveTask(String name, String desc, TaskStatus status) {
        Task task = new Task(name, desc, status);
        manager.saveTask(task);
        return task;
    }

    private List<Task> createAndSaveTasks(int taskCount) {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) {
            Task task = new Task("task " + i, "task " + i + " desc");
            tasks.add(task);
            manager.saveTask(task);
        }
        return tasks;
    }

    private Epic createAndSaveEpic() {
        Epic epic = new Epic("epic", "epic desc");
        manager.saveEpic(epic);
        return epic;
    }

    private Epic createAndSaveEpic(String name, String desc) {
        Epic epic = new Epic(name, desc);
        manager.saveEpic(epic);
        return epic;
    }

    private Subtask createAndSaveSubtask(Epic epic) {
        Subtask sub = new Subtask("sub", "sub desc", epic);
        manager.saveSubtask(sub);
        return sub;
    }

    private Subtask createAndSaveSubtask(String name, String desc, TaskStatus status, Epic epic) {
        Subtask sub = new Subtask(name, desc, status, epic);
        manager.saveSubtask(sub);
        return sub;
    }

    private List<Subtask> createAndSaveSubtasks(Epic epic, int subtaskCount) {
        List<Subtask> subtasks = new ArrayList<>();
        for (int i = 0; i < subtaskCount; i++) {
            Subtask sub = new Subtask("sub " + i, "sub desc " + i, epic);
            subtasks.add(sub);
            manager.saveSubtask(sub);
        }
        return subtasks;
    }

    private void createAndSaveEpicWithSubtasks(int subtaskCount) {
        Epic epic = createAndSaveEpic();
        for (int i = 0; i < subtaskCount; i++) {
            createAndSaveSubtask(epic);
        }
    }
}