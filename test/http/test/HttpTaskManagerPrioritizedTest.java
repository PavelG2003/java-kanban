package http.test;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import task.Epic;
import task.SubTask;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Set;


import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerPrioritizedTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer httpTaskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();
    public HttpTaskManagerPrioritizedTest() throws IOException {
    }

    @BeforeEach
    public void start() {
        manager.removeAllSubTasks();
        manager.removeAllEpicTasks();
        manager.removeAllDefaultTasks();
        httpTaskServer.start();
    }

    @AfterEach
    public void stop() {
        httpTaskServer.stop();
    }

    @Test
    public void testGetPrioritized() throws IOException, InterruptedException {
        Task task = new Task(
                "Test 1",
                "Testing task 1",
                LocalDateTime.of(2024, 6, 6, 12, 0),
                100);
        Task task2 = new Task(
                "Test 2",
                "Testing task 2",
                LocalDateTime.of(2021, 6, 6, 12, 0),
                100
        );
        Epic epic = new Epic("Epic 1", "Epic description");
        manager.createEpicTask(epic);
        int epicId = epic.getTaskId();

        SubTask subTask1 = new SubTask(
                "Subtask 1",
                "Subtask description 1",
                epicId,
                LocalDateTime.of(2026, 3, 6, 12, 0),
                100
        );
        manager.createDefaultTask(task);
        manager.createDefaultTask(task2);
        manager.createEpicTask(epic);
        manager.createSubTask(subTask1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        Set<Task> prioritizedTasks = manager.getPrioritizedTasks();

        assertNotNull(tasks);
        assertEquals(prioritizedTasks.size(), tasks.length);
    }
}
