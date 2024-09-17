package kanban.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import kanban.tasks.Epic;
import kanban.tasks.Subtask;
import kanban.tasks.Task;
import kanban.tasks.TaskStatus;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static kanban.lib.TestAssertions.*;

class CSVFormatTest {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    @DisplayName("Writer сохраняет задачи в файл")
    public void testThatWriterSavesTasksToFile() throws IOException {
        Path taskFile = Files.createTempFile("tasks", ".csv");

        try (CSVFormat.TaskFileWriter writer = CSVFormat.writer(taskFile)) {
            writer.println(createTask("1;task1;desc1;NEW;2024-09-05 01:02:03;123"));
            writer.println(createEpic("2;epic1;desc2;IN_PROGRESS;2024-09-06 02:03:04;234"));
            writer.println(createEpic("3;epic2;desc3;NEW;null;null"));
            writer.println(createSubtask("4;sub1;desc4;NEW;2;2024-09-07 03:04:05;345"));
            writer.println(createSubtask("5;sub2;desc5;DONE;2;2024-09-08 04:05:06;456"));
        }

        List<String> lines = Files.readAllLines(taskFile, StandardCharsets.UTF_8);

        List<String> expectedLines = List.of(
            header(),
            line("1;TASK;task1;NEW;desc1;123;2024-09-05 01:02:03"),
            line("2;EPIC;epic1;IN_PROGRESS;desc2;234;2024-09-06 02:03:04"),
            line("3;EPIC;epic2;NEW;desc3;null;null"),
            line("4;SUBTASK;sub1;NEW;desc4;345;2024-09-07 03:04:05;2"),
            line("5;SUBTASK;sub2;DONE;desc5;456;2024-09-08 04:05:06;2")
        );

        assertEquals(6, lines.size());
        assertEquals(expectedLines.get(1), lines.get(1));
        assertEquals(expectedLines.get(2), lines.get(2));
        assertEquals(expectedLines.get(3), lines.get(3));
        assertEquals(expectedLines.get(4), lines.get(4));
        assertEquals(expectedLines.get(5), lines.get(5));
    }

    @Test
    public void loadTasksFromFile() throws IOException {

        List<String> lines = List.of(
                header(),
                line("1;TASK;task1;NEW;desc1;123;2024-09-05 01:02:03"),
                line("2;EPIC;epic1;IN_PROGRESS;desc2;234;2024-09-06 02:03:04"),
                line("3;EPIC;epic2;NEW;desc3;null;null"),
                line("4;SUBTASK;sub1;NEW;desc4;345;2024-09-07 03:04:05;2"),
                line("5;SUBTASK;sub2;DONE;desc5;456;2024-09-08 04:05:06;2")
        );

        Path taskFile = Files.createTempFile("tasks", ".csv");
        try (PrintWriter writer = new PrintWriter(new FileWriter(taskFile.toFile(), StandardCharsets.UTF_8))) {
            lines.forEach(writer::println);
        }

        List<Task> tasks = CSVFormat.loadTasksFromFile(taskFile);

        Task task = createTask("1;task1;desc1;NEW;2024-09-05 01:02:03;123");
        Epic epic1 = createEpic("2;epic1;desc2;IN_PROGRESS;2024-09-06 02:03:04;234");
        Epic epic2 = createEpic("3;epic2;desc3;NEW;null;null");
        Subtask sub1 = createSubtask("3;sub1;desc4;NEW;2;2024-09-07 03:04:05;345");
        Subtask sub2 = createSubtask("4;sub2;desc5;DONE;2;2024-09-08 04:05:06;456");

        assertTaskEquals(task, tasks.get(0));
        assertEpicEquals(epic1, (Epic)(tasks.get(1)));
        assertEpicEquals(epic2, (Epic)(tasks.get(2)));
        assertSubtaskEquals(sub1, (Subtask)(tasks.get(3)));
        assertSubtaskEquals(sub2, (Subtask)(tasks.get(4)));
    }

    @Test
    public void taskToString() {
        Task task = createTask("1;task;desc;NEW;2024-01-01 01:02:03;123");
        String formattedTask = CSVFormat.taskToString(task);
        Task restoredTask = CSVFormat.fromString(formattedTask);

        assertTaskEquals(task, restoredTask);
    }

    @Test
    public void epicToString() {
        Epic epic = createEpic("1;task;desc;NEW;2024-01-01 01:02:03;123");
        String formattedEpic = CSVFormat.taskToString(epic);
        Epic restoredEpic = (Epic)(CSVFormat.fromString(formattedEpic));

        assertEpicEquals(epic, restoredEpic);
    }

    @Test
    public void subtaskToString() {
        Subtask sub = createSubtask("1;sub;desc;NEW;2;2024-01-01 01:02:03;123");
        String formattedSub = CSVFormat.taskToString(sub);
        Subtask restoredSub = (Subtask)(CSVFormat.fromString(formattedSub));

        assertSubtaskEquals(sub, restoredSub);
    }

    private Task createTask(String formattedTask) {
        String[] chunks = formattedTask.split(";");
        return new Task(
                Integer.parseInt(chunks[0]), // id
                chunks[1], // name
                chunks[2], // description
                TaskStatus.valueOf(chunks[3]), // status
                parseTime(chunks[4]), // startTime
                Duration.ofMinutes(Integer.parseInt(chunks[5])) // duration
        );
    }

    private Epic createEpic(String formattedEpic) {
        String[] chunks = formattedEpic.split(";");
        return new Epic(
                Integer.parseInt(chunks[0]), // id
                chunks[1], // name
                chunks[2], // description
                TaskStatus.valueOf(chunks[3]), // status
                parseTime(chunks[4]), // startTime
                parseDuration(chunks[5]) // duration
        );
    }

    private Subtask createSubtask(String formattedSubtask) {
        String[] chunks = formattedSubtask.split(";");
        return new Subtask(
                Integer.parseInt(chunks[0]), // id
                chunks[1], // name
                chunks[2], // description
                TaskStatus.valueOf(chunks[3]), // status
                Integer.parseInt(chunks[4]), // epicId
                parseTime(chunks[5]), // startTime
                Duration.ofMinutes(Integer.parseInt(chunks[6])) // duration
        );
    }

    private String header() {
        return "I'm a header";
    }

    private String line(String pattern) {
        String[]  chunks = pattern.split(";");
        if (!chunks[6].equals("null")) {
            chunks[6] = reformatTime(chunks[6]);
        }
        return String.join(CSVFormat.SEPARATOR, chunks);
    }

    private String reformatTime(String formattedTime) {
        return CSVFormat.formatTime(parseTime(formattedTime));
    }

    private LocalDateTime parseTime(String formattedTime) {
        return formattedTime.equals("null") ? null : LocalDateTime.parse(formattedTime, DATE_TIME_FORMATTER);
    }

    private Duration parseDuration(String formattedDuration) {
        return formattedDuration.equals("null") ? null : Duration.ofMinutes(Integer.parseInt(formattedDuration));
    }
}