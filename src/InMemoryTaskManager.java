import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private int counter = 1;
    private HashMap<Integer, Task> defaultTasks;
    private HashMap<Integer, Epic> epicTasks;
    private HashMap<Integer, SubTask> subTasks;

    public InMemoryTaskManager() {
        defaultTasks = new HashMap<>();
        epicTasks = new HashMap<>();
        subTasks = new HashMap<>();
    }

    HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public HashMap<Integer, Task> getDefaultTasks() {
        return defaultTasks;
    }

    @Override
    public HashMap<Integer, Epic> getEpicTasks() {
        return epicTasks;
    }

    @Override
    public HashMap<Integer, SubTask> getSubTasks() {
        return subTasks;
    }

    @Override
    public ArrayList<Task> getAllDefaultTasks() {
        ArrayList<Task> tasks = new ArrayList<>(defaultTasks.values());
        return tasks;
    }

    @Override
    public ArrayList<Epic> getAllEpicTasks() {
        ArrayList<Epic> tasks = new ArrayList<>(epicTasks.values());
        return tasks;
    }

    @Override
    public ArrayList<SubTask> getAllSubTasks() {
        ArrayList<SubTask> tasks = new ArrayList<>(subTasks.values());
        return tasks;
    }

    @Override
    public int genereteId() {
        return counter++;
    }

    @Override
    public void removeAllDefaultTasks() {
      for (int id : defaultTasks.keySet()) {
          historyManager.remove(id);
      }
        defaultTasks.clear();
    }

    @Override
    public void removeAllEpicTasks() {
      for (Epic epic : epicTasks.values()) {
          int epicId = epic.getTaskId();
          ArrayList<Integer> subTasksIds = epic.getSubTaskIds();
          if (!subTasksIds.isEmpty()) {
              for (int id : subTasksIds) {
                  historyManager.remove(id);
              }
          }
          historyManager.remove(epicId);
      }

      for (Epic epic : epicTasks.values()) {
          ArrayList<Integer> subTasksIds = epic.getSubTaskIds();
          if (!subTasksIds.isEmpty()) {
              for (int id : subTasksIds) {
                  subTasks.remove(id);
              }
              epic.clearSubTasks();
          }
      }
      epicTasks.clear();
    }

    @Override
    public void removeAllSubTasks() {
       for (int id : subTasks.keySet()) {
           historyManager.remove(id);
       }
       for (Epic epic : epicTasks.values()) {
           epic.clearSubTasks();
           epic.setTaskStatus(TaskStatus.NEW);
       }
       subTasks.clear();
    }

    @Override
    public Task getDefaultTaskById(int id) {
        Task task = defaultTasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpicTaskById(int id) {
        Epic epic = epicTasks.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public SubTask getSubTaskById(int id) {
        SubTask subTask = subTasks.get(id);
        historyManager.add(subTask);
        return subTask;
    }

    @Override
    public void removeDefaultTaskById(int id) {
        defaultTasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeEpicTaskById(int id) {
        Epic epic = epicTasks.get(id);
        ArrayList<Integer> subTasksIds = epic.getSubTaskIds();
        if (!subTasksIds.isEmpty()) {
            for (int subTaskId : subTasksIds) {
                subTasks.remove(subTaskId);
                historyManager.remove(subTaskId);
            }
            epic.clearSubTasks();
        }
        epicTasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeSubTaskById(int id) {
        SubTask subTask = subTasks.remove(id);
        if (subTask == null) return;

        int epicId = subTask.getEpicId();
        Epic epic = epicTasks.get(epicId);
        if (epic != null) {
            epic.removeSubTask(id);
            updateEpicTaskStatus(epic);
        }
        historyManager.remove(id);
    }

    @Override
    public void createDefaultTask(Task task) {
        int id = genereteId();
        task.setTaskId(id);
        defaultTasks.put(id, task);
    }

    @Override
    public void createEpicTask(Epic task) {
        int id = genereteId();
        task.setTaskId(id);
        epicTasks.put(id, task);
    }

    @Override
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

    @Override
    public void updateDefaultTask(Task task) {
        defaultTasks.put(task.getTaskId(), task);
    }

    @Override
    public void updateEpicTask(Epic task) {
        epicTasks.put(task.getTaskId(), task);
    }

    @Override
    public void updateSubTask(SubTask task) {
        subTasks.put(task.getTaskId(), task);
        int epicId = task.getEpicId();
        Epic epic = epicTasks.get(epicId);
        if (epic != null) {
            updateEpicTaskStatus(epic);
        }
    }

    @Override
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

    @Override
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
