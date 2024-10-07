package kanban.http.handlers;

import com.google.gson.Gson;
import kanban.HttpTaskServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class Request {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final HttpResponse.BodyHandler<String> HANDLER = HttpResponse.BodyHandlers.ofString();
    private static final Gson GSON = HttpTaskServer.getGson();

    public static HttpResponse<String> get(String uri) throws IOException, InterruptedException {
        HttpRequest request = getRequest(uri);
        return CLIENT.send(request, HANDLER);
    }

    public static HttpResponse<String> post(String uri, Object object) throws IOException, InterruptedException {
        HttpRequest request = postRequest(uri, object);
        return CLIENT.send(request, HANDLER);
    }

    public static HttpResponse<String> delete(String uri) throws IOException, InterruptedException {
        HttpRequest request = deleteRequest(uri);
        return CLIENT.send(request, HANDLER);
    }

    public static HttpRequest getRequest(String uri) {
        return HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    private static HttpRequest postRequest(String uri, Object object) {
        return HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(object)))
                .header("Content-Type", "application/json: charset=utf-8")
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    private static HttpRequest deleteRequest(String uri) {
        return HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .DELETE()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }
}

