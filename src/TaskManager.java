import java.util.ArrayList;
import java.util.HashMap;

public interface TaskManager {
    HashMap<Integer, Task> getDefaultTasks();

    HashMap<Integer, Epic> getEpicTasks();

    HashMap<Integer, SubTask> getSubTasks();

    ArrayList<Task> getAllDefaultTasks();

    ArrayList<Epic> getAllEpicTasks();

    ArrayList<SubTask> getAllSubTasks();

    int genereteId();

    void removeAllDefaultTasks();

    void removeAllEpicTasks();

    void removeAllSubTasks();

    Task getDefaultTaskById(int id);

    Epic getEpicTaskById(int id);

    Task getSubTaskById(int id);

    void removeDefaultTaskById(int id);

    void removeEpicTaskById(int id);

    void removeSubTaskById(int id);

    void createDefaultTask(Task task);

    void createEpicTask(Epic task);

    void createSubTask(SubTask task);

    void updateDefaultTask(Task task);

    void updateEpicTask(Epic task);

    void updateSubTask(SubTask task);

    ArrayList<SubTask> getAllSubTasksFromEpic(int epicId);

    void updateEpicTaskStatus(Epic epic);
}
