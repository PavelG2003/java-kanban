package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import server.HttpTaskServer;
import task.Task;

import java.io.IOException;
import java.util.Set;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    TaskManager manager;
    Gson gson;

    public PrioritizedHandler(TaskManager manager) {
        this.manager = manager;
        gson = HttpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (method.equals("GET")) {
            handleGetPrioritized(exchange);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleGetPrioritized(HttpExchange exchange) throws IOException {
        Set<Task> tasks = manager.getPrioritizedTasks();
        String jsonSubtasks = gson.toJson(tasks);
        sendText(exchange, jsonSubtasks);
    }
}
