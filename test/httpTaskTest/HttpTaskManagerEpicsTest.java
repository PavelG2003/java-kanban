package httpTaskTest;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import task.Epic;
import task.SubTask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerEpicsTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer httpTaskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerEpicsTest() throws IOException {
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
    public void testAddEpic() throws IOException, InterruptedException {
        Epic task = new Epic(
                "Test 1",
                "Testing task 1"
                );
        String jsonTask = gson.toJson(task);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> tasksFromManager = manager.getAllEpicTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size());
        assertEquals("Test 1", tasksFromManager.getFirst().getTitle());
    }

    @Test
    public void testGetAllEpics() throws IOException, InterruptedException {
        Epic task1 = new Epic(
                "Test 1",
                "Testing task 1"
        );

        Epic task2 = new Epic(
                "Test 2",
                "Testing task 2"
        );

        manager.createEpicTask(task1);
        manager.createEpicTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Epic[] tasks = gson.fromJson(response.body(), Epic[].class);
        assertNotNull(tasks, "Задачи не возвращаются");
        assertEquals("Test 1", tasks[0].getTitle());
        assertEquals("Test 2", tasks[1].getTitle());
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic task1 = new Epic(
                "Test 1",
                "Testing task 1"
        );
        manager.createEpicTask(task1);
        int id = task1.getTaskId();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Epic actualTask = gson.fromJson(response.body(), Epic.class);

        assertEquals(200, response.statusCode());
        assertNotNull(actualTask);
        assertEquals("Test 1", actualTask.getTitle());
        assertEquals(id, actualTask.getTaskId());
        assertEquals("Testing task 1", actualTask.getDescription());
    }

    @Test
    public void testDeleteEpicById() throws IOException, InterruptedException {
        Epic task1 = new Epic(
                "Test 1",
                "Testing task 1"
        );
        manager.createEpicTask(task1);
        int id = task1.getTaskId();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Epic> tasksFromManager = manager.getAllEpicTasks();

        assertEquals(0, tasksFromManager.size());
        assertNull(manager.getDefaultTasks().get(id));
    }

    @Test
    public void testGetEpicsSubtasks() throws IOException, InterruptedException {
        Epic task1 = new Epic(
                "Test 1",
                "Testing task 1"
        );
        manager.createEpicTask(task1);
        int id = task1.getTaskId();

        SubTask sub1 = new SubTask(
                "Sub1",
                "DescSub1",
                id,
                LocalDateTime.now(),
                100
        );

        SubTask sub2 = new SubTask(
                "Sub2",
                "DescSub2",
                id,
                LocalDateTime.of(2026, 10, 13, 10, 30),
                20
        );

        manager.createSubTask(sub1);
        manager.createSubTask(sub2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + id + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        SubTask[] subtasks = gson.fromJson(response.body(), SubTask[].class);

        assertEquals("Sub1", subtasks[0].getTitle());
        assertEquals(LocalDateTime.of(
                2026,
                10,
                13,
                10,
                30
        ), subtasks[1].getStartTime());
    }
}
