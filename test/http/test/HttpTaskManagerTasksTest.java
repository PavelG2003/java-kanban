package http.test;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer server = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerTasksTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.removeAllDefaultTasks();
        manager.removeAllEpicTasks();
        manager.removeAllSubTasks();
        server.start();
    }

    @AfterEach
    public void shutDown() {
        server.stop();
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        Task task = new Task(
                "Test 1",
                "Testing task 1",
                LocalDateTime.of(2024, 6, 6, 12, 0),
                100);
        String jsonTask = gson.toJson(task);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getAllDefaultTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size());
        assertEquals("Test 1", tasksFromManager.getFirst().getTitle());
    }

    @Test
    public void testGetAllTasks() throws IOException, InterruptedException {
        Task task1 = new Task(
                "Test 1",
                "Testing task 1",
                LocalDateTime.of(2024, 6, 6, 12, 0),
                100
        );

        Task task2 = new Task(
                "Test 2",
                "Testing task 2",
                LocalDateTime.of(2025, 6, 6, 12, 0),
                100
        );

        manager.createDefaultTask(task1);
        manager.createDefaultTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertNotNull(tasks, "Задачи не возвращаются");
        assertEquals("Test 1", tasks[0].getTitle());
        assertEquals("Test 2", tasks[1].getTitle());
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        Task task1 = new Task(
                "Test 1",
                "Testing task 1",
                LocalDateTime.of(2024, 6, 6, 12, 0),
                100
        );
        manager.createDefaultTask(task1);
        int id = task1.getTaskId();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task actualTask = gson.fromJson(response.body(), Task.class);

        assertEquals(200, response.statusCode());
        assertNotNull(actualTask);
        assertEquals("Test 1", actualTask.getTitle());
        assertEquals(LocalDateTime.of(2024, 6, 6, 12, 0), actualTask.getStartTime());
        assertEquals(id, actualTask.getTaskId());
    }

    @Test
    public void testDeleteTaskById() throws IOException, InterruptedException {
        Task task1 = new Task(
                "Test 1",
                "Testing task 1",
                LocalDateTime.of(2024, 6, 6, 12, 0),
                100
        );
        manager.createDefaultTask(task1);
        int id = task1.getTaskId();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getAllDefaultTasks();

        assertEquals(0, tasksFromManager.size());
        assertNull(manager.getDefaultTasks().get(id));
    }
}
