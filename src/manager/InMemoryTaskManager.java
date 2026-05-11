package manager;

import task.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected int counter = 1;
    protected final HashMap<Integer, Task> defaultTasks;
    protected final HashMap<Integer, Epic> epicTasks;
    protected final HashMap<Integer, SubTask> subTasks;
    protected final Comparator<Task> taskComparator = (t1, t2) -> {
        int compare = t1.getStartTime().compareTo(t2.getStartTime());
        if (compare == 0) {
            return Integer.compare(t1.getTaskId(), t2.getTaskId());
        }
        return compare;
    };
    protected final Set<Task> prioritizedTasks = new TreeSet<>(taskComparator);


    public InMemoryTaskManager() {
        defaultTasks = new HashMap<>();
        epicTasks = new HashMap<>();
        subTasks = new HashMap<>();
    }

    public HistoryManager historyManager = Managers.getDefaultHistory();

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
        return new ArrayList<>(defaultTasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpicTasks() {
        return new ArrayList<>(epicTasks.values());
    }

    @Override
    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
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
      for (int id : epicTasks.keySet()) {
          historyManager.remove(id);
      }
        epicTasks.clear();
    }

    @Override
    public void removeAllSubTasks() {
       for (int id : subTasks.keySet()) {
           historyManager.remove(id);
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
        }
        historyManager.remove(id);
    }

    @Override
    public void createDefaultTask(Task task) {
        if (isTaskOverlaps(task)) {
            throw new IllegalArgumentException("Задачи пересекаются");
        }

        int id = genereteId();
        task.setTaskId(id);
        defaultTasks.put(id, task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void createEpicTask(Epic task) {
        int id = genereteId();
        task.setTaskId(id);
        epicTasks.put(id, task);
        updateEpicDuration(task);
    }

    @Override
    public void createSubTask(SubTask task) {
        if (isTaskOverlaps(task)) {
            throw new IllegalArgumentException("Задачи пересекаются");
        }

        int id = genereteId();
        task.setTaskId(id);
        subTasks.put(id, task);
        int epicId = task.getEpicId();
        Epic epic = epicTasks.get(epicId);
        if (epic != null) {
            epic.addSubTaskId(id);
            updateEpicTaskStatus(epic);
            updateEpicDuration(epic);
            updateEpicStartAndEndTime(epic);
        }
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void updateDefaultTask(Task task) {
        if (isTaskOverlaps(task)) {
            throw new IllegalArgumentException("Задачи пересекаются");
        }

        defaultTasks.put(task.getTaskId(), task);
    }

    @Override
    public void updateEpicTask(Epic task) {
        epicTasks.put(task.getTaskId(), task);
    }

    @Override
    public void updateSubTask(SubTask task) {
        if (isTaskOverlaps(task)) {
            throw new IllegalArgumentException("Задачи пересекаются");
        }

        subTasks.put(task.getTaskId(), task);
        int epicId = task.getEpicId();
        Epic epic = epicTasks.get(epicId);
        if (epic != null) {
            updateEpicTaskStatus(epic);
            updateEpicDuration(epic);
            updateEpicStartAndEndTime(epic);
        }
    }

    @Override
    public ArrayList<SubTask> getAllSubTasksFromEpic(int epicId) {
        Epic epic = epicTasks.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }

        return epic.getSubTaskIds().stream()
                .map(subTasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
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

    public void updateEpicDuration(Epic epic) {
        long totalMinutes = getEpicSubTasks(epic).stream()
                .map(SubTask::getDuration)
                .mapToLong(Duration::toMinutes)
                .sum();
        epic.setDuration(totalMinutes);
    }

    public void updateEpicStartAndEndTime(Epic epic) {
        List<SubTask> epicTasks = getEpicSubTasks(epic);

        if (epicTasks.isEmpty()) {
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }

        LocalDateTime minStartTime = epicTasks.stream()
                .map(SubTask::getStartTime)
                .min(Comparator.naturalOrder())
                .orElse(null);

        LocalDateTime maxEndTime = epicTasks.stream()
                .map(SubTask::getEndTime)
                .max(Comparator.naturalOrder())
                .orElse(null);

        epic.setStartTime(minStartTime);
        epic.setEndTime(maxEndTime);
    }

    public List<SubTask> getEpicSubTasks(Epic epic) {
        return epic.getSubTaskIds().stream()
            .map(subTasks::get)
            .toList();
    }

    public Set<Task> getPrioritizedTasks() {
        return prioritizedTasks;
    }

    public boolean isTasksOverlap(Task t1, Task t2) {
        return t1.getStartTime().isBefore(t2.getEndTime()) &&
                t2.getStartTime().isBefore(t1.getEndTime());
    }

    public boolean isTaskOverlaps(Task task) {
        ArrayList<Task> allTasks = new ArrayList<>();
        allTasks.addAll(defaultTasks.values());
        allTasks.addAll(subTasks.values());

        boolean isOverlap = allTasks.stream()
                .filter(curTask -> curTask.getTaskId() != task.getTaskId())
                .anyMatch(curTask -> isTasksOverlap(curTask, task));
        return isOverlap;
    }
}
