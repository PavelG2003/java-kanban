package httpHandlers;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {
    protected void sendText(HttpExchange exchange, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(200, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, 0);
        String res = "Ресурс не найден";
        exchange.getResponseBody().write(res.getBytes());
        exchange.close();
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(406, 0);
        String res = "При создании или обновлении задача пересекается с уже существующими";
        exchange.getResponseBody().write(res.getBytes());
        exchange.close();
    }

    protected void sendIncorrectId(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(400, 0);
        String res = "Некорректный идентификатор задачи";
        exchange.getResponseBody().write(res.getBytes());
        exchange.close();
    }

    protected void sendTaskWasCreated(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(201, 0);
        String res = "Задача создана или обновлена";
        exchange.getResponseBody().write(res.getBytes());
        exchange.close();
    }
}
