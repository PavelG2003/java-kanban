package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.NotFoundException;
import manager.TaskManager;
import server.HttpTaskServer;
import task.SubTask;
import task.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    private static final int COLLECTION_PART_LENGTH = 2;
    private static final int ITEM_PART_LENGTH = 3;
    TaskManager manager;
    Gson gson;

    public SubtaskHandler(TaskManager manager) {
        this.manager = manager;
        gson = HttpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        SubtaskHandler.Endpoint endpoint = getEndpoint(path, method);

        switch (endpoint) {
            case GET_SUBTASKS: {
                handleGetSubtasks(exchange);
                break;
            }
            case GET_SUBTASK: {
                handleGetSubtask(exchange);
                break;
            }
            case СREATE_SUBTASK: {
                handleCreateSubtask(exchange);
                break;
            }
            case DELETE_SUBTASK: {
                handleDeleteSubtask(exchange);
                break;
            }
            default:
                sendNotFound(exchange);
        }
    }

    private void handleGetSubtasks(HttpExchange exchange) throws IOException {
        List<SubTask> tasks = manager.getAllSubTasks();
        String jsonSubtasks = gson.toJson(tasks);
        sendText(exchange, jsonSubtasks);
    }

    private void handleGetSubtask(HttpExchange exchange) throws IOException {
        Optional<Integer> optTaskId = getId(exchange);
        if (optTaskId.isEmpty()) {
            sendIncorrectId(exchange);
            return;
        }
        int taskId = optTaskId.get();
        try {
            Task subtask = manager.getSubTaskById(taskId);
            String jsonTask = gson.toJson(subtask);
            sendText(exchange, jsonTask);
        } catch (NotFoundException e) {
            e.getMessage();
            sendNotFound(exchange);
        }
    }

    private void handleCreateSubtask(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        SubTask subtask = gson.fromJson(body, SubTask.class);
        Optional<Integer> optTaskId = getId(exchange);
        if (optTaskId.isEmpty()) {
            try {
                manager.createSubTask(subtask);
                sendTaskWasCreated(exchange);
                return;
            } catch (IllegalArgumentException e) {
                e.getMessage();
                sendHasInteractions(exchange);
            }
        }
        try {
            manager.updateSubTask(subtask);
            sendTaskWasCreated(exchange);
        } catch (IllegalArgumentException e) {
            e.getMessage();
            sendHasInteractions(exchange);
        }
    }

    private void handleDeleteSubtask(HttpExchange exchange) throws IOException {
        Optional<Integer> optTaskId = getId(exchange);
        if (optTaskId.isEmpty()) {
            sendIncorrectId(exchange);
            return;
        }
        int taskId = optTaskId.get();
        manager.removeSubTaskById(taskId);
        String jsonTask = gson.toJson("Подзадача удалена");
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

    private SubtaskHandler.Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if ((pathParts.length == COLLECTION_PART_LENGTH) && requestMethod.equals("GET")) {
            return SubtaskHandler.Endpoint.GET_SUBTASKS;
        } else if (requestMethod.equals("POST")) {
            return SubtaskHandler.Endpoint.СREATE_SUBTASK;
        } else if ((pathParts.length == ITEM_PART_LENGTH) && requestMethod.equals("GET")) {
            return SubtaskHandler.Endpoint.GET_SUBTASK;
        } else if (requestMethod.equals("DELETE")) {
            return SubtaskHandler.Endpoint.DELETE_SUBTASK;
        } else {
            return SubtaskHandler.Endpoint.UNKNOWN;
        }
    }

    enum Endpoint {
        GET_SUBTASKS,
        GET_SUBTASK,
        СREATE_SUBTASK,
        DELETE_SUBTASK,
        UNKNOWN
    }
}
