import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private int counter = 1;
    private HashMap<Integer, Task> defaultTasks;
    private HashMap<Integer, Epic> epicTasks;
    private HashMap<Integer, SubTask> subTasks;

    public TaskManager() {
        defaultTasks = new HashMap<>();
        epicTasks = new HashMap<>();
        subTasks = new HashMap<>();
    }

    public HashMap<Integer, Task> getDefaultTasks() {
        return defaultTasks;
    }

    public HashMap<Integer, Epic> getEpicTasks() {
        return epicTasks;
    }

    public HashMap<Integer, SubTask> getSubTasks() {
        return subTasks;
    }

    public ArrayList<Task> getAllDefaultTasks() {
        ArrayList<Task> tasks = new ArrayList<>(defaultTasks.values());
        return tasks;
    }

    public ArrayList<Epic> getAllEpicTasks() {
        ArrayList<Epic> tasks = new ArrayList<>(epicTasks.values());
        return tasks;
    }

    public ArrayList<SubTask> getAllSubTasks() {
        ArrayList<SubTask> tasks = new ArrayList<>(subTasks.values());
        return tasks;
    }

    public int genereteId() {
        return counter++;
    }

    public void removeAllDefaultTasks() {
        defaultTasks.clear();
    }

    public void removeAllEpicTasks() {
        epicTasks.clear();
    }

    public void removeAllSubTasks() {
        subTasks.clear();
    }

    public Task getDefaultTaskById(int id) {
        return defaultTasks.get(id);
    }

    public Epic getEpicTaskById(int id) {
        return epicTasks.get(id);
    }

    public Task getSubTaskById(int id) {
        return subTasks.get(id);
    }

    public void removeDefaultTaskById(int id) {
        defaultTasks.remove(id);
    }

    public void removeEpicTaskById(int id) {
        epicTasks.remove(id);
    }

    public void removeSubTaskById(int id) {
        SubTask subTask = subTasks.remove(id);
        if (subTask == null) return;

        int epicId = subTask.getEpicId();
        Epic epic = epicTasks.get(epicId);
        if (epic != null) {
            epic.removeSubTask(id);
        }

    }

    public void createDefaultTask(Task task) {
        int id = genereteId();
        task.setTaskId(id);
        defaultTasks.put(id, task);
    }

    public void createEpicTask(Epic task) {
        int id = genereteId();
        task.setTaskId(id);
        epicTasks.put(id, task);
    }

    public void createSubTask(SubTask task) {
        int id = genereteId();
        task.setTaskId(id);
        subTasks.put(id, task);

        int epicId = task.getEpicId();
        Epic epic = epicTasks.get(epicId);
        if (epic != null) {
            epic.addSubTaskId(id);
            updateEpicTaskStatus(epic);
        }
    }

    public void updateDefaultTask(Task task) {
        defaultTasks.put(task.getTaskId(), task);
    }

    public void updateEpicTask(Epic task) {
        epicTasks.put(task.getTaskId(), task);
    }

    public void updateSubTask(SubTask task) {
        subTasks.put(task.getTaskId(), task);
        int epicId = task.getEpicId();
        Epic epic = epicTasks.get(epicId);
        if (epic != null) {
            updateEpicTaskStatus(epic);
        }
    }

    public ArrayList<SubTask> getAllSubTasksFromEpic(int epicId) {
        Epic epic = epicTasks.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }
        ArrayList<SubTask> res =  new ArrayList<>();
        for (Integer subTaskId : epic.getSubTaskIds()) {
            SubTask subTask = subTasks.get(subTaskId);
            if (subTask != null) {
                res.add(subTask);
            }
        }
        return res;
    }

    public void updateEpicTaskStatus(Epic epic) {
        ArrayList<Integer> subTaskIds = epic.getSubTaskIds();
        if (subTaskIds.isEmpty()) {
            epic.setTaskStatus(TaskStatus.NEW);
            return;
        }

        boolean isAllDone = true;
        boolean isAllNew = true;

        for (Integer subTaskId : subTaskIds) {
            SubTask subTask = subTasks.get(subTaskId);
            if (subTask == null) continue;
            if (subTask.getTaskStatus() != TaskStatus.NEW) {
                isAllNew = false;
            }
            if (subTask.getTaskStatus() != TaskStatus.DONE) {
                isAllDone = false;
            }
        }

        if (isAllDone) {
            epic.setTaskStatus(TaskStatus.DONE);
        } else if (isAllNew) {
            epic.setTaskStatus(TaskStatus.NEW);
        } else {
            epic.setTaskStatus(TaskStatus.IN_PROGRESS);
        }
    }
}
