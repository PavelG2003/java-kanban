package manager;

import task.Task;
import utils.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private Map<Integer, Node<Task>> nodeByTaskId;
    public Node<Task> head;
    public Node<Task> tail;

    public InMemoryHistoryManager() {
        nodeByTaskId = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        int taskId = task.getTaskId();

        if (nodeByTaskId.containsKey(taskId)) {
            remove(taskId);
        }

        linkLast(task);
    }

    @Override
    public void remove(int id) {
        Node<Task> taskNode = nodeByTaskId.remove(id);
        if (taskNode != null) {
            removeNode(taskNode);
        }
    }

    @Override
    public ArrayList<Task> getHistory() {
        return getTasks();
    }

    private void linkLast(Task task) {
        Node<Task> newNode = new Node<>(task);

        if (head == null) {
           head = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
        }
        tail = newNode;
        nodeByTaskId.put(task.getTaskId(), newNode);
    }

    private ArrayList<Task> getTasks() {
        ArrayList<Task> tasksArray = new ArrayList<>();
        Node<Task> current = head;
        while (current != null) {
            tasksArray.add(current.data);
            current = current.next;
        }
        return tasksArray;
    }

    private void removeNode(Node<Task> node) {
        if (node == null) {
            return;
        }

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
    }
}
