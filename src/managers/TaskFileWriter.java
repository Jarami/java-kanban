package managers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TaskFileWriter implements Closeable {

    private final Path taskFile;
    private final PrintWriter writer;

    public TaskFileWriter(Path taskFile) throws IOException {

        this.taskFile = taskFile;
        prepareTaskFile();
        clearTaskFile();

        this.writer = new PrintWriter(new FileWriter(taskFile.toFile(), StandardCharsets.UTF_8, true));
    }

    @Override
    public void close() {
        writer.close();
    }

    public void println(String line) {
        writer.println(line);
    }

    private void prepareTaskFile() throws IOException {
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
