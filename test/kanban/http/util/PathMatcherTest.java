package kanban.http.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static kanban.lib.TestAssertions.*;

public class PathMatcherTest {

    @Test
    @DisplayName("/tasks должен соответствовать /tasks")
    public void givenTasksPattern_whenMatch_gotTrue() {
        String path = "/tasks";
        PathMatcher matcher = PathMatcher.with(path)
                .match("/tasks");

        assertEquals("/tasks", matcher.getMatchedPath());
        assertEmpty(matcher.getPathParameters());
    }

    @Test
    @DisplayName("/some/tasks не должен соответствовать /tasks")
    public void givenPathWithWrongLeadingPath_whenMatch_gotTrue() {
        String path = "/some/tasks";
        PathMatcher matcher = PathMatcher.with(path)
                .match("/tasks");

        assertNull(matcher.getMatchedPath());
        assertNull(matcher.getPathParameters());
    }

    @Test
    @DisplayName("/tasks/some не должен соответствовать /tasks")
    public void givenPathWithWrongTrailingPath_whenMatch_gotTrue() {
        String path = "/tasks/some";
        PathMatcher matcher = PathMatcher.with(path)
                .match("/tasks");

        assertNull(matcher.getMatchedPath());
        assertNull(matcher.getPathParameters());
    }

    @Test
    @DisplayName("/some/tasks/done не должен соответствовать /tasks")
    public void givenPathWithWrongLeadingAndTrailingPath_whenMatch_gotTrue() {
        String path = "/some/tasks/done";
        PathMatcher matcher = PathMatcher.with(path)
                .match("/tasks");

        assertNull(matcher.getMatchedPath());
        assertNull(matcher.getPathParameters());
    }

    @Test
    @DisplayName("/tasks/ должен соответствовать /tasks")
    public void givenTasksWithTrailingSlash_whenMatch_gotTrue() {
        String path = "/tasks/";
        PathMatcher matcher = PathMatcher.with(path)
                .match("/tasks");

        assertEquals("/tasks", matcher.getMatchedPath());
        assertEmpty(matcher.getPathParameters());
    }

    @Test
    @DisplayName("/tasks должен соответствовать /tasks при выборе из нескольких шаблонов")
    public void givenSeveralPatterns_whenMatch_gotCorrectMatch() {
        String path = "/tasks";
        PathMatcher matcher = PathMatcher.with(path)
                .match("/tasks/id")
                .match("/tasks");

        assertEquals("/tasks", matcher.getMatchedPath());
        assertEmpty(matcher.getPathParameters());
    }
    
    @Test
    @DisplayName("/tasks/123 должен соответствовать /tasks/{id}")
    public void givenPath_whenMatchWithCapture_gotPathParams() {
        String path = "/tasks/123";
        PathMatcher matcher = PathMatcher.with(path)
                .match("/tasks")
                .match("/tasks/{id}");

        assertEquals("/tasks/{id}", matcher.getMatchedPath());
        assertIterableEquals(List.of("123"), matcher.getPathParameters());
    }

    @Test
    @DisplayName("/tasks/123/some должен соответствовать /tasks/{id}/some")
    public void givenPath_whenMatchWithCaptureAndTrailingPath_gotPathParams() {
        String path = "/tasks/123/some";
        PathMatcher matcher = PathMatcher.with(path)
                .match("/tasks/{id}")
                .match("/tasks/{id}/some");

        assertEquals("/tasks/{id}/some", matcher.getMatchedPath());
        assertIterableEquals(List.of("123"), matcher.getPathParameters());
    }
}
