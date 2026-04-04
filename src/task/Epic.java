package task;
import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subTaskIds;

    public Epic(String title, String description) {
        super(title, description);
        subTaskIds = new ArrayList<>();
    }

    public ArrayList<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    public void addSubTaskId(int subTaskId) {
        if (subTaskId == this.taskId) {
            return;
        }
        subTaskIds.add(subTaskId);
    }

    public void removeSubTask(int subTaskId) {
        subTaskIds.remove(Integer.valueOf(subTaskId));
    }

    public void clearSubTasks() {
        subTaskIds.clear();
    }
}
