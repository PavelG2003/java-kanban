package task;

public class SubTask extends Task {
    private int epicId;

    public SubTask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    public TaskTypes getType() {
        return TaskTypes.SUBTASK;
    }

    public int getEpicId() {
        return epicId;
    }
}
