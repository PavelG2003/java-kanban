import java.util.Objects;

public class Task {
    protected String title;
    protected String description;
    protected int taskId;
    protected TaskStatus taskStatus;

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.taskStatus = TaskStatus.NEW;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getTaskId() {
        return taskId;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus newTaskStatus) {
        this.taskStatus = newTaskStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return taskId == task.taskId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(taskId);
    }

    @Override
    public String toString() {
        return "Task{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", taskId=" + taskId +
                ", taskStatus=" + taskStatus +
                '}';
    }
}


