package lib;

import java.util.Collection;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestAssertions {
    public static void assertEmpty(Collection<?> col) {
        assertTrue(col.isEmpty());
    }

    public static void assertEmpty(Collection<?> col, String message) {
        assertTrue(col.isEmpty(), message);
    }
}
