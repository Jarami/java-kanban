package lib;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestAssertions {
    public static void assertEmpty(Collection<?> col) {
        assertTrue(col.isEmpty());
    }

    public static void assertEmpty(Collection<?> col, String message) {
        assertTrue(col.isEmpty(), message);
    }
}
