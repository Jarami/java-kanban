package util;

import exceptions.ManagerSaveException;
import tasks.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static tasks.TaskType.*;

public class CSVFormat {

    public static final String SEPARATOR = "\t";

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
            println(join("id", "type", "name", "status", "description", "epic"));
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

        private String taskToString(Task task) {
            return join(task.getId(), TASK, task.getName(), task.getStatus(), task.getDescription());
        }

        private String taskToString(Epic epic) {
            return join(epic.getId(), EPIC, epic.getName(), epic.getStatus(), epic.getDescription());
        }

        private String taskToString(Subtask subtask) {
            return join(subtask.getId(), SUBTASK, subtask.getName(), subtask.getStatus(), subtask.getDescription(),
                    subtask.getEpicId());
        }

        private String join(Object... objects) {
            return Arrays.stream(objects)
                    .map(String::valueOf)
                    .collect(Collectors.joining(SEPARATOR));
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

    private static Task fromString(String line) {
        String[] chunks = line.split(SEPARATOR);
        int id = Integer.parseInt(chunks[0]);
        TaskType type = TaskType.valueOf(chunks[1]);
        String name = chunks[2];
        TaskStatus status = TaskStatus.valueOf(chunks[3]);
        String desc = chunks[4];

        return switch (type) {
            case TASK -> new Task(id, name, desc, status);
            case EPIC -> new Epic(id, name, desc);
            case SUBTASK -> new Subtask(id, name, desc, status, Integer.parseInt(chunks[5]));
            default -> throw new ManagerSaveException("Неизвестный тип задачи " + type);
        };
    }

}
