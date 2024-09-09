package managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;
import util.CSVFormat;
import util.Tasks;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static lib.TestAssertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static tasks.TaskStatus.*;
import static tasks.TaskType.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager>{

    static class Suite {
        public List<Task> tasks = new ArrayList<>();
        public List<Epic> epics = new ArrayList<>();
        public List<Subtask> subtasks = new ArrayList<>();
    }

    private Path taskFile;
//    private TaskManager manager;

    @BeforeEach
    public void setup() throws IOException {
        taskFile = Files.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(taskFile);
    }

    @Test
    void testThatManagerIsLoadedFromFile() throws IOException {

        Task task1 = createAndSaveTask("task1;desc1;NEW;2024-01-01 01:02:03;123");
        Task task2 = createAndSaveTask("task2;desc2;IN_PROGRESS;2024-01-02 02:03:04;234");
        Epic epic1 = createAndSaveEpic("epic1;desc3");
        Epic epic2 = createAndSaveEpic("epic2;desc4");
        Subtask sub1 = createAndSaveSubtask("sub0;desc5;DONE;" + epic1.getId() + ";2024-01-03 03:04:05;345");
        Subtask sub2 = createAndSaveSubtask("sub1;desc6;DONE;" + epic1.getId() + ";2024-01-04 04:05:06;456");
        Subtask sub3 = createAndSaveSubtask("sub2;desc7;DONE;" + epic1.getId() + ";2024-01-05 05:06:07;567");

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
    public void testThatIdGeneratorUpdatedAfterLoadingTasksFromFile() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(taskFile.toFile(), StandardCharsets.UTF_8))) {
            writer.println(join("id","type","name","status","description","duration", "startTime","epic"));
            writer.println(join("2","TASK","task1","DONE","desk of task1","123",formatTime("2024-01-01 01:02:03")));
            writer.println(join("1","TASK","task2","NEW","desk of task2","234",formatTime("2024-01-02 02:03:04")));
        }

        TaskManager manager2 = FileBackedTaskManager.loadFromFile(taskFile);
        Set<Integer> ids = manager2.getTasks().stream().map(Task::getId).collect(Collectors.toSet());

        Task newTask = Tasks.createTask("task;desc;NEW;2024-01-03 03:04:05;345");
        manager2.saveTask(newTask);

        assertFalse(ids.contains(newTask.getId()));
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
    public void testThatManagerSavesTaskToFile() throws IOException {
        Task task = createAndSaveTask("task;desc;NEW;2024-01-01 00:00:00;123");

        String line = readLastLine();
        int id = getIdFromLine(line);

        assertEquals(task.getId(), id);
    }

    @Test
    public void testThatManagerSavesEpicToFile() throws IOException {
        Epic epic = createAndSaveEpic("epic;desc");

        String line = readLastLine();
        int id = getIdFromLine(line);
        assertEquals(epic.getId(), id);
    }

    @Test
    public void testThatManagerSavesSubtaskToFile() throws IOException {
        Epic epic = createAndSaveEpic("epic;desc");
        Subtask sub = createAndSaveSubtask("sub;desc1;NEW;" + epic.getId() + ";2024-01-01 01:02:03;123");

        String line = readLastLine();
        int id = getIdFromLine(line);
        assertEquals(sub.getId(), id);
    }

    @Test
    public void testThatManagerUpdatesTaskInFile() throws IOException {
        Task task = createAndSaveTask("task;desc;NEW;2024-01-01 01:02:03;123");

        task.setName("new task");
        manager.updateTask(task);

        Task updatedTask = CSVFormat.fromString(readLastLine());
        assertTaskEquals(task, updatedTask);
    }

    @Test
    public void testThatManagerUpdatesEpicInFile() throws IOException {
        Epic epic = createAndSaveEpic("epic;desc");

        epic.setName("new epic");
        manager.updateEpic(epic);

        Epic updatedEpic = (Epic)(CSVFormat.fromString(readLastLine()));
        assertEpicEquals(epic, updatedEpic);
    }

    @Test
    public void testThatManagerUpdatesSubtaskToFile() throws IOException {
        Epic epic = createAndSaveEpic("epic;desc");
        Subtask sub = createAndSaveSubtask("sub;desc1;NEW;" + epic.getId() + ";2024-01-01 01:02:03;123");

        sub.setName("new sub");
        manager.updateSubtask(sub);

        Subtask updatedSub = (Subtask)(CSVFormat.fromString(readLastLine()));
        assertEquals(sub, updatedSub);
    }

    @Test
    public void testThatManagerRemovesTasksFromFile() throws IOException {
        Suite suite = createSuite();

        manager.removeTasks();

        Set<Integer> actualIds = Set.copyOf(readTaskId());

        Set<Integer> expectedIds = new HashSet<>();
        expectedIds.addAll(suite.epics.stream().map(Task::getId).toList());
        expectedIds.addAll(suite.subtasks.stream().map(Task::getId).toList());

        assertEquals(expectedIds, actualIds);
    }

    @Test
    public void testThatManagerRemovesTaskByIdFromFile() throws IOException {
        Suite suite = createSuite();

        Task task = suite.tasks.get(1);
        manager.removeTaskById(task.getId());

        Set<Integer> actualIds = Set.copyOf(readTaskId());

        Set<Integer> expectedIds = new HashSet<>();
        expectedIds.addAll(suite.tasks.stream().filter(t -> t != task).map(Task::getId).toList());
        expectedIds.addAll(suite.epics.stream().map(Task::getId).toList());
        expectedIds.addAll(suite.subtasks.stream().map(Task::getId).toList());

        assertEquals(expectedIds, actualIds);
    }

    @Test
    public void testThatManagerRemovesEpicsFromFile() throws IOException {
        Suite suite = createSuite();

        manager.removeEpics();

        Set<Integer> actualIds = Set.copyOf(readTaskId());

        Set<Integer> expectedIds = new HashSet<>(suite.tasks.stream().map(Task::getId).toList());

        assertEquals(expectedIds, actualIds);
    }

    @Test
    public void testThatManagerRemovesEpicByIdFromFile() throws IOException {
        Suite suite = createSuite();
        Epic epic = suite.epics.getFirst();

        manager.removeEpicById(epic.getId());

        Set<Integer> actualIds = Set.copyOf(readTaskId());

        Set<Integer> expectedIds = new HashSet<>();
        expectedIds.addAll(suite.tasks.stream().map(Task::getId).toList());
        expectedIds.addAll(suite.epics.stream().filter(e -> e != epic).map(Task::getId).toList());
        expectedIds.addAll(suite.subtasks.stream().filter(s -> !s.getEpicId().equals(epic.getId())).map(Task::getId).toList());

        assertEquals(expectedIds, actualIds);
    }

    @Test
    public void testThatManagerRemovesSubtasksFromFile() throws IOException {
        Suite suite = createSuite();

        manager.removeSubtasks();

        Set<Integer> actualIds = Set.copyOf(readTaskId());

        Set<Integer> expectedIds = new HashSet<>();
        expectedIds.addAll(suite.tasks.stream().map(Task::getId).toList());
        expectedIds.addAll(suite.epics.stream().map(Task::getId).toList());

        assertEquals(expectedIds, actualIds);
    }

    @Test
    public void testThatManagerRemovesSubtaskByIdFromFile() throws IOException {
        Suite suite = createSuite();
        Subtask sub = suite.subtasks.get(1);

        manager.removeSubtaskById(sub.getId());

        Set<Integer> actualIds = Set.copyOf(readTaskId());

        Set<Integer> expectedIds = new HashSet<>();
        expectedIds.addAll(suite.tasks.stream().map(Task::getId).toList());
        expectedIds.addAll(suite.epics.stream().map(Task::getId).toList());
        expectedIds.addAll(suite.subtasks.stream().filter(s -> s != sub).map(Task::getId).toList());

        assertEquals(expectedIds, actualIds);
    }

    private String join(Object... objects) {
        return Arrays.stream(objects)
                .map(String::valueOf)
                .collect(Collectors.joining(CSVFormat.SEPARATOR));
    }

    private String readLastLine() throws IOException {
        return Files.readAllLines(taskFile, StandardCharsets.UTF_8).getLast();
    }

    private List<Integer> readTaskId() throws IOException {
        return Files.readAllLines(taskFile, StandardCharsets.UTF_8)
                .stream()
                .skip(1)
                .map(line -> line.split(CSVFormat.SEPARATOR))
                .map(chunks -> Integer.parseInt(chunks[0]))
                .toList();
    }

    private String formatTime(String formattedTime) {
        return CSVFormat.formatTime(Tasks.parseTime(formattedTime));
    }

    private int getIdFromLine(String line) {
        String[] chunks = line.split(CSVFormat.SEPARATOR);
        return Integer.parseInt(chunks[0]);
    }

    private Suite createSuite() {

        Suite suite = new Suite();

        Task task1 = createAndSaveTask("task1;desc1;NEW;2024-01-01 01:02:03;123");
        Task task2 = createAndSaveTask("task2;desc2;NEW;2024-01-02 02:03:04;234");
        Task task3 = createAndSaveTask("task3;desc3;NEW;2024-01-03 03:04:05;345");
        Epic epic1 = createAndSaveEpic("epic1;desc4");
        Epic epic2 = createAndSaveEpic("epic2;desc5");
        Subtask sub1 = createAndSaveSubtask("sub1;desc6;NEW;" + epic1.getId() + ";2024-01-04 04:05:06;456");
        Subtask sub2 = createAndSaveSubtask("sub2;desc7;NEW;" + epic1.getId() + ";2024-01-05 05:06:07;567");

        suite.tasks.add(task1);
        suite.tasks.add(task2);
        suite.tasks.add(task3);
        suite.epics.add(epic1);
        suite.epics.add(epic2);
        suite.subtasks.add(sub1);
        suite.subtasks.add(sub2);

        return suite;
    }
}