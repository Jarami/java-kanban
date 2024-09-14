package util;

import tasks.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static tasks.TaskType.*;

public class CSVFormat {

    public static final String SEPARATOR = "\t";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static class TaskFileWriter implements Closeable {

        private final Path taskFile;
        private final PrintWriter writer;

        public TaskFileWriter(Path taskFile) throws IOException {

            this.taskFile = taskFile;
            createTaskFileIfAbsent();
            clearTaskFile();

            this.writer = new PrintWriter(new FileWriter(taskFile.toFile(), StandardCharsets.UTF_8));
            setHeaders();
        }

        @Override
        public void close() {
            writer.close();
        }

        private void setHeaders() {
            println(join("id", "type", "name", "status", "description", "duration", "startTime", "epic"));
        }

        public void println(Task task) {
            println(taskToString(task));
        }

        public void println(Epic epic) {
            println(taskToString(epic));
        }

        public void println(Subtask subtask) {
            println(taskToString(subtask));
        }

        public void println(String line) {
            writer.println(line);
        }

        private void createTaskFileIfAbsent() throws IOException {
            if (!Files.exists(taskFile)) {
                Files.createFile(taskFile);
            }
        }

        private void clearTaskFile() throws FileNotFoundException {
            try (PrintWriter writer = new PrintWriter(taskFile.toFile())) {
                // просто удаляем содержимое
            }
        }
    }

    public static List<Task> loadTasksFromFile(Path path) throws IOException {

        return Files.readAllLines(path, StandardCharsets.UTF_8)
                .stream()
                .skip(1)
                .map(CSVFormat::fromString)
                .toList();
    }

    public static TaskFileWriter writer(Path taskFile) throws IOException {
        return new TaskFileWriter(taskFile);
    }

    public static String taskToString(Task task) {
        return join(task.getId(), TASK, task.getName(), task.getStatus(), task.getDescription(),
                formatDuration(task.getDuration()), formatTime(task.getStartTime()));
    }

    public static String taskToString(Epic epic) {
        return join(epic.getId(), EPIC, epic.getName(), epic.getStatus(), epic.getDescription(),
                formatDuration(epic.getDuration()), formatTime(epic.getStartTime()));
    }

    public static String taskToString(Subtask subtask) {
        return join(subtask.getId(), SUBTASK, subtask.getName(), subtask.getStatus(), subtask.getDescription(),
                formatDuration(subtask.getDuration()), formatTime(subtask.getStartTime()), subtask.getEpicId());
    }

    private static String join(Object... objects) {
        return Arrays.stream(objects)
                .map(String::valueOf)
                .collect(Collectors.joining(SEPARATOR));
    }

    public static String formatTime(LocalDateTime time) {
        return time == null ? "null" : DATE_TIME_FORMATTER.format(time);
    }

    public static String formatDuration(Duration duration) {
        return duration == null ? "null" : String.valueOf(duration.toMinutes());
    }

    public static Task fromString(String line) {
        String[] chunks = line.split(SEPARATOR);
        int id = Integer.parseInt(chunks[0]);
        TaskType type = TaskType.valueOf(chunks[1]);
        String name = chunks[2];
        TaskStatus status = TaskStatus.valueOf(chunks[3]);
        String desc = chunks[4];
        Duration duration = parseDuration(chunks[5]);
        LocalDateTime startTime = parseTime(chunks[6]);

        return switch (type) {
            case TASK -> new Task(id, name, desc, status, startTime, duration);
            case EPIC -> new Epic(id, name, desc, status, startTime, duration);
            case SUBTASK -> new Subtask(id, name, desc, status, Integer.parseInt(chunks[7]), startTime, duration);
        };
    }

    private static LocalDateTime parseTime(String formattedTime) {
        return formattedTime.equals("null") ? null : LocalDateTime.parse(formattedTime, DATE_TIME_FORMATTER);
    }

    private static Duration parseDuration(String formattedDuration) {
        return formattedDuration.equals("null") ? null : Duration.ofMinutes(Long.parseLong(formattedDuration));
    }

}
