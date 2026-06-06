package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.NotFoundException;
import manager.TaskManager;
import server.HttpTaskServer;
import task.Epic;
import task.SubTask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    TaskManager manager;
    Gson gson;

    public EpicHandler(TaskManager manager) {
        this.manager = manager;
        gson = HttpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        EpicHandler.Endpoint endpoint = getEndpoint(path, method);

        switch (endpoint) {
            case GET_EPICS: {
                handleGetEpics(exchange);
                break;
            }
            case GET_EPIC: {
                handleGetEpic(exchange);
                break;
            }
            case  GET_EPIC_SUBTASKS: {
                handleEpicSubtasks(exchange);
                break;
            }
            case СREATE_EPIC: {
                handleCreateEpic(exchange);
                break;
            }
            case DELETE_EPIC: {
                handleDeleteEpic(exchange);
                break;
            }
            default:
                sendNotFound(exchange);
        }
    }

    private void handleGetEpics(HttpExchange exchange) throws IOException {
        List<Epic> tasks = manager.getAllEpicTasks();
        String jsonTasks = gson.toJson(tasks);
        sendText(exchange, jsonTasks);
    }

    private void handleGetEpic(HttpExchange exchange) throws IOException {
        Optional<Integer> optTaskId = getId(exchange);
        if (optTaskId.isEmpty()) {
            sendIncorrectId(exchange);
            return;
        }
        int epicId = optTaskId.get();
        try {
            Epic epic = manager.getEpicTaskById(epicId);
            String jsonEpic = gson.toJson(epic);
            sendText(exchange, jsonEpic);
        } catch (NotFoundException e) {
            e.getMessage();
            sendNotFound(exchange);
        }
    }

    private void handleEpicSubtasks(HttpExchange exchange) throws IOException {
        Optional<Integer> optTaskId = getId(exchange);
        if (optTaskId.isEmpty()) {
            sendIncorrectId(exchange);
            return;
        }
        int epicId = optTaskId.get();
        try {
            Epic epic = manager.getEpicTaskById(epicId);
            List<SubTask> subtasks = manager.getEpicSubTasks(epic);
            String jsonSubtasks = gson.toJson(subtasks);
            sendText(exchange, jsonSubtasks);
        } catch (NotFoundException e) {
            e.getMessage();
            sendNotFound(exchange);
        }
    }

    private void handleCreateEpic(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Epic epic = gson.fromJson(body, Epic.class);
        Optional<Integer> optTaskId = getId(exchange);
        if (optTaskId.isEmpty()) {
            try {
                manager.createEpicTask(epic);
                sendTaskWasCreated(exchange);
                return;
            } catch (IllegalArgumentException e) {
                e.getMessage();
                sendHasInteractions(exchange);
            }
        }
        try {
            manager.updateEpicTask(epic);
            sendTaskWasCreated(exchange);
        } catch (IllegalArgumentException e) {
            e.getMessage();
            sendHasInteractions(exchange);
        }
    }

    private void handleDeleteEpic(HttpExchange exchange) throws IOException {
        Optional<Integer> optTaskId = getId(exchange);
        if (optTaskId.isEmpty()) {
            sendIncorrectId(exchange);
            return;
        }
        int epicId = optTaskId.get();
        manager.removeEpicTaskById(epicId);
        String jsonEpic = gson.toJson("Задача удалена");
        sendText(exchange, jsonEpic);
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

    private EpicHandler.Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if ((pathParts.length == 2) && requestMethod.equals("GET")) {
            return EpicHandler.Endpoint.GET_EPICS;
        } else if (requestMethod.equals("POST")) {
            return EpicHandler.Endpoint.СREATE_EPIC;
        } else if ((pathParts.length == 3) && requestMethod.equals("GET")) {
            return EpicHandler.Endpoint.GET_EPIC;
        } else if (requestMethod.equals("DELETE")) {
            return EpicHandler.Endpoint.DELETE_EPIC;
        } else if ((pathParts.length == 4) && requestMethod.equals("GET")) {
            return EpicHandler.Endpoint.GET_EPIC_SUBTASKS;
        }
        else {
            return EpicHandler.Endpoint.UNKNOWN;
        }
    }

    enum Endpoint {
        GET_EPICS,
        GET_EPIC,
        GET_EPIC_SUBTASKS,
        СREATE_EPIC,
        DELETE_EPIC,
        UNKNOWN
    }
}