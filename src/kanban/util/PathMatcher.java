package kanban.util;

import java.util.ArrayList;
import java.util.List;

public class PathMatcher {

    private final String path;
    private boolean isMatched;
    private String matchedPath;
    private List<String> pathParameters;

    public static PathMatcher with(String path) {
        return new PathMatcher(path);
    }

    private PathMatcher(String path) {
        this.path = path;
    }

    public String getMatchedPath() {
        return matchedPath;
    }

    public List<String> getPathParameters() {
        return pathParameters;
    }

    public PathMatcher match(String pathPattern) {

        if (isMatched) {
            return this;
        }

        List<String> arguments = getAllMatches(pathPattern);
        if (arguments != null) {
            isMatched = true;
            matchedPath = pathPattern;
            pathParameters = arguments;
        }
        return this;
    }

    private List<String> getAllMatches(String pathPattern) {

        String[] pathChunks = path.split("/");
        String[] chunks = pathPattern.split("/");

        if (pathChunks.length != chunks.length) {
            return null;
        }

        List<String> arguments = new ArrayList<>();
        for (int i = 0; i < chunks.length; i++) {
            String chunk = chunks[i];
            String pathChunk = pathChunks[i];
            if (chunk.startsWith("{") && chunk.endsWith("}")) {
                arguments.add(pathChunk);
            } else if (!chunk.equals(pathChunk)){
                return null;
            }
        }

        return arguments;
    }
}
