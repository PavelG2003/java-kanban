import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private ArrayList<Task> history;

    public InMemoryHistoryManager() {
        history = new ArrayList<>();
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        if (history.contains(task)) {
            history.remove(task);
        }

        if (history.size() == 10) {
            history.removeFirst();
        }

        history.add(task);
    }


    @Override
    public ArrayList<Task> getHistory() {
        return history;
    }
}
