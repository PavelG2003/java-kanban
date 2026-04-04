package manager;

import task.*;

import java.io.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File path;

    public FileBackedTaskManager(File path) {
        this.path = path;
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write("id,type,name,status,description,epic");
            writer.newLine();

            for (Task task : getAllDefaultTasks()) {
                    String stringTask = toString(task);
                    writer.write(stringTask);
                    writer.newLine();
            }

            for (Epic epic : getAllEpicTasks()) {
                String stringEpic = toString(epic);
                writer.write(stringEpic);
                writer.newLine();
            }

            for (SubTask subTask : getAllSubTasks()) {
                String stringSubTask = toString(subTask);
                writer.write(stringSubTask);
                writer.newLine();
            }

        } catch (IOException exp) {
            throw new ManagerSaveException(exp);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            int maxId = 0;

            while ((line = reader.readLine()) != null) {
                Task task = fromString(line);
                int taskId = task.getTaskId();
                if (taskId > maxId) {
                    maxId = taskId;
                }
                TaskTypes type = task.getType();
                switch (type) {
                    case SUBTASK:
                        manager.subTasks.put(taskId, (SubTask) task);
                        break;
                    case EPIC:
                        manager.epicTasks.put(taskId, (Epic) task);
                        break;
                    case TASK:
                        manager.defaultTasks.put(taskId, task);
                        break;
                }
            }
            manager.counter = maxId + 1;

            for (SubTask subTask : manager.getAllSubTasks()) {
                int epicId = subTask.getEpicId();
                Epic epic = manager.getEpicTaskById(epicId);
                epic.addSubTaskId(subTask.getTaskId());
            }

        } catch (IOException exp) {
            throw new ManagerSaveException(exp);
        }

        return manager;
    }

    @Override
    public void createDefaultTask(Task task) {
        super.createDefaultTask(task);
        save();
    }

    @Override
    public void createEpicTask(Epic task) {
        super.createEpicTask(task);
        save();
    }

    @Override
    public void createSubTask(SubTask task) {
        super.createSubTask(task);
        save();
    }

    @Override
    public void updateDefaultTask(Task task) {
        super.updateDefaultTask(task);
        save();
    }

    @Override
    public void updateEpicTask(Epic task) {
        super.updateEpicTask(task);
        save();
    }

    @Override
    public void updateSubTask(SubTask task) {
        super.updateSubTask(task);
        save();
    }

    @Override
    public void removeDefaultTaskById(int id) {
        super.removeDefaultTaskById(id);
        save();
    }

    @Override
    public void removeEpicTaskById(int id) {
        super.removeEpicTaskById(id);
        save();
    }

    @Override
    public void removeSubTaskById(int id) {
        super.removeSubTaskById(id);
        save();
    }

    @Override
    public void removeAllDefaultTasks() {
        super.removeAllDefaultTasks();
        save();
    }

    @Override
    public void removeAllEpicTasks() {
        super.removeAllEpicTasks();
        save();
    }

    @Override
    public void removeAllSubTasks() {
        super.removeAllSubTasks();
        save();
    }

    public String toString(Task task) {
        TaskTypes type = task.getType();
        switch (type) {
            case SUBTASK:
            return task.getTaskId() + "," +
                    type + "," +
                    task.getTitle() + "," +
                    task.getTaskStatus() + "," +
                    task.getDescription() + "," +
                    ((SubTask) task).getEpicId();
            case EPIC:
            return task.getTaskId() + "," +
                    type + "," +
                    task.getTitle() + "," +
                    task.getTaskStatus() + "," +
                    task.getDescription() + ",";
            case TASK:
            return task.getTaskId() + "," +
                    type + "," +
                    task.getTitle() + "," +
                    task.getTaskStatus() + "," +
                    task.getDescription() + ",";
        }
        return null;
    }

    public static Task fromString(String str) {
        String[] fields = str.split(",");
        int id = Integer.parseInt(fields[0]);
        TaskTypes type = TaskTypes.valueOf(fields[1]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];

        switch (type) {
            case SUBTASK:
                int epicId = Integer.parseInt(fields[5]);
                SubTask subTask = new SubTask(name, description, epicId);
                subTask.setTaskId(id);
                subTask.setTaskStatus(status);
                return subTask;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setTaskId(id);
                epic.setTaskStatus(status);
                return epic;
            case TASK:
                Task task = new Task(name, description);
                task.setTaskId(id);
                task.setTaskStatus(status);
                return task;
            default:
                throw new IllegalArgumentException("Unknown task type: " + type);
        }

    }
}
