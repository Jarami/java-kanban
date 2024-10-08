package kanban.lib;

import kanban.tasks.Epic;
import kanban.tasks.Subtask;
import kanban.tasks.Task;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class TestAssertions {

    public static void assertEmpty(Collection<?> col) {
        assertTrue(col.isEmpty());
    }

    public static void assertEmpty(Collection<?> col, String message) {
        assertTrue(col.isEmpty(), message);
    }

    public static void assertEpicEquals(Epic expectedEpic, Epic actualEpic) {
        assertEquals(expectedEpic.getName(), actualEpic.getName(), String.format(
                "имя эпика должно быть %s, а не %s",
                expectedEpic.getName(), actualEpic.getName()));

        assertEquals(expectedEpic.getDescription(), actualEpic.getDescription(), String.format(
                "описание эпика должно быть %s, а не %s",
                expectedEpic.getDescription(), actualEpic.getDescription()));

        assertEquals(expectedEpic.getStatus(), actualEpic.getStatus(), String.format(
                "статус эпика должен быть %s, а не %s",
                expectedEpic.getStatus(), actualEpic.getStatus()));

        assertEquals(expectedEpic.getStartTime(), actualEpic.getStartTime(), String.format(
                "начало выполнения эпика должно быть %s, а не %s",
                expectedEpic.getStartTime(), actualEpic.getStartTime()));

        assertEquals(expectedEpic.getDuration(), actualEpic.getDuration(), String.format(
                "длительность эпика должна быть %s, а не %s",
                expectedEpic.getDuration(), actualEpic.getDuration()));

        assertIterableEquals(expectedEpic.getSubtasksId(), actualEpic.getSubtasksId(), String.format(
                "подзадачи эпика должны быть %s, а не %s",
                expectedEpic.getSubtasksId(), actualEpic.getSubtasksId()));
    }

    public static void assertTaskEquals(Task expectedTask, Task actualTask) {
        assertEquals(expectedTask.getName(), actualTask.getName(), String.format(
                "имя задачи должно быть %s, а не %s",
                expectedTask.getName(), actualTask.getName()));

        assertEquals(expectedTask.getDescription(), actualTask.getDescription(), String.format(
                "описание задачи должно быть %s, а не %s",
                expectedTask.getDescription(), actualTask.getDescription()));

        assertEquals(expectedTask.getStatus(), actualTask.getStatus(), String.format(
                "статус задачи должен быть %s, а не %s",
                expectedTask.getStatus(), actualTask.getStatus()));

        assertEquals(expectedTask.getStartTime(), actualTask.getStartTime(), String.format(
                "начало выполнения задачи должно быть %s, а не %s",
                expectedTask.getStartTime(), actualTask.getStartTime()));

        assertEquals(expectedTask.getDuration(), actualTask.getDuration(), String.format(
                "длительность задачи должна быть %s, а не %s",
                expectedTask.getDuration(), actualTask.getDuration()));
    }

    public static void assertSubtaskEquals(Subtask expectedSub, Subtask actualSub) {
        assertEquals(expectedSub.getName(), actualSub.getName(), String.format(
                "имя подзадачи должно быть %s, а не %s",
                expectedSub.getName(), actualSub.getName()));

        assertEquals(expectedSub.getDescription(), actualSub.getDescription(), String.format(
                "описание подзадачи должно быть %s, а не %s",
                expectedSub.getDescription(), actualSub.getDescription()));

        assertEquals(expectedSub.getStatus(), actualSub.getStatus(), String.format(
                "статус подзадачи должен быть %s, а не %s",
                expectedSub.getStatus(), actualSub.getStatus()));

        assertEquals(expectedSub.getStartTime(), actualSub.getStartTime(), String.format(
                "начало выполнения подзадачи должно быть %s, а не %s",
                expectedSub.getStartTime(), actualSub.getStartTime()));

        assertEquals(expectedSub.getDuration(), actualSub.getDuration(), String.format(
                "длительность подзадачи должна быть %s, а не %s",
                expectedSub.getDuration(), actualSub.getDuration()));
    }
}
