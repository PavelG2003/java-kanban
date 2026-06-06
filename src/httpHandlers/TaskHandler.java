package httpHandlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.NotFoundException;
import manager.TaskManager;
import server.HttpTaskServer;
import task.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    TaskManager manager;
    Gson gson;

    public TaskHandler(TaskManager manager) {
        this.manager = manager;
        gson = HttpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        Endpoint endpoint = getEndpoint(path, method);

        switch (endpoint) {
            case GET_TASKS: {
                handleGetTasks(exchange);
                break;
            }
            case GET_TASK: {
                handleGetTask(exchange);
                break;
            }
            case СREATE_TASK: {
                handleCreateTask(exchange);
                break;
            }
            case DELETE_TASK: {
                handleDeleteTask(exchange);
                break;
            }
            default:
                sendNotFound(exchange);
        }
    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        List<Task> tasks = manager.getAllDefaultTasks();
        String jsonTasks = gson.toJson(tasks);
        sendText(exchange, jsonTasks);
    }

    private void handleGetTask(HttpExchange exchange) throws IOException {
        Optional<Integer> optTaskId = getId(exchange);
        if (optTaskId.isEmpty()) {
            sendIncorrectId(exchange);
            return;
        }
        int taskId = optTaskId.get();
        try {
            Task task = manager.getDefaultTaskById(taskId);
            String jsonTask = gson.toJson(task);
            sendText(exchange, jsonTask);
        } catch (NotFoundException e) {
            e.getMessage();
            sendNotFound(exchange);
        }
    }

    private void handleCreateTask(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Task task = gson.fromJson(body, Task.class);
        Optional<Integer> optTaskId = getId(exchange);
        if (optTaskId.isEmpty()) {
            try {
                manager.createDefaultTask(task);
                sendTaskWasCreated(exchange);
                return;
            } catch (IllegalArgumentException e) {
                e.getMessage();
                sendHasInteractions(exchange);
                return;
            }
        }
        try {
            manager.updateDefaultTask(task);
            sendTaskWasCreated(exchange);
        } catch (IllegalArgumentException e) {
            e.getMessage();
            sendHasInteractions(exchange);
        }
    }

    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        Optional<Integer> optTaskId = getId(exchange);
        if (optTaskId.isEmpty()) {
            sendIncorrectId(exchange);
            return;
        }
        int taskId = optTaskId.get();
        manager.removeDefaultTaskById(taskId);
        String jsonTask = gson.toJson("Задача удалена");
        sendText(exchange, jsonTask);
    }

    private Optional<Integer> getId(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        if (pathParts.length < 3) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if ((pathParts.length == 2) && requestMethod.equals("GET")) {
            return Endpoint.GET_TASKS;
        } else if (requestMethod.equals("POST")) {
            return Endpoint.СREATE_TASK;
        } else if ((pathParts.length == 3) && requestMethod.equals("GET")) {
            return Endpoint.GET_TASK;
        } else if (requestMethod.equals("DELETE")) {
            return Endpoint.DELETE_TASK;
        } else {
            return Endpoint.UNKNOWN;
        }
    }

    enum Endpoint {
        GET_TASKS,
        GET_TASK,
        СREATE_TASK,
        DELETE_TASK,
        UNKNOWN
    }
}
