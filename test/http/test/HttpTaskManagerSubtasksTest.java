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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerSubtasksTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer server = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerSubtasksTest() throws IOException {
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
    public void testAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Epic description");
        manager.createEpicTask(epic);
        int epicId = epic.getTaskId();

        SubTask subTask = new SubTask(
                "Subtask 1",
                "Subtask description 1",
                epicId,
                LocalDateTime.of(2024, 6, 6, 12, 0),
                100
        );

        String jsonTask = gson.toJson(subTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(201, response.statusCode());

        List<SubTask> subtasksFromManager = manager.getAllSubTasks();

        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, subtasksFromManager.size());
        assertEquals("Subtask 1", subtasksFromManager.getFirst().getTitle());
        assertEquals(epicId, subtasksFromManager.getFirst().getEpicId());
    }

    @Test
    public void testGetAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Epic description");
        manager.createEpicTask(epic);
        int epicId = epic.getTaskId();

        SubTask subTask1 = new SubTask(
                "Subtask 1",
                "Subtask description 1",
                epicId,
                LocalDateTime.of(2024, 6, 6, 12, 0),
                100
        );

        SubTask subTask2 = new SubTask(
                "Subtask 2",
                "Subtask description 2",
                epicId,
                LocalDateTime.of(2025, 6, 6, 12, 0),
                100
        );

        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());

        SubTask[] subtasks = gson.fromJson(response.body(), SubTask[].class);

        assertNotNull(subtasks, "Подзадачи не возвращаются");
        assertEquals(2, subtasks.length);
        assertEquals("Subtask 1", subtasks[0].getTitle());
        assertEquals("Subtask 2", subtasks[1].getTitle());
        assertEquals(epicId, subtasks[0].getEpicId());
        assertEquals(epicId, subtasks[1].getEpicId());
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Epic description");
        manager.createEpicTask(epic);
        int epicId = epic.getTaskId();

        SubTask subTask = new SubTask(
                "Subtask 1",
                "Subtask description 1",
                epicId,
                LocalDateTime.of(2024, 6, 6, 12, 0),
                100
        );

        manager.createSubTask(subTask);
        int subtaskId = subTask.getTaskId();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());

        SubTask actualSubtask = gson.fromJson(response.body(), SubTask.class);

        assertNotNull(actualSubtask);
        assertEquals(subtaskId, actualSubtask.getTaskId());
        assertEquals("Subtask 1", actualSubtask.getTitle());
        assertEquals("Subtask description 1", actualSubtask.getDescription());
        assertEquals(epicId, actualSubtask.getEpicId());
        assertEquals(LocalDateTime.of(2024, 6, 6, 12, 0), actualSubtask.getStartTime());
    }

    @Test
    public void testDeleteSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Epic description");
        manager.createEpicTask(epic);
        int epicId = epic.getTaskId();

        SubTask subTask = new SubTask(
                "Subtask 1",
                "Subtask description 1",
                epicId,
                LocalDateTime.of(2024, 6, 6, 12, 0),
                100
        );

        manager.createSubTask(subTask);
        int subtaskId = subTask.getTaskId();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());

        List<SubTask> subtasksFromManager = manager.getAllSubTasks();

        assertEquals(0, subtasksFromManager.size());
        assertNull(manager.getSubTasks().get(subtaskId));
    }
}