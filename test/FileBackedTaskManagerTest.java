import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @Test
    void save() throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task defaultTask  = new Task("Task1", "Desc1");
        manager.createDefaultTask(defaultTask);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loaded.getAllDefaultTasks().size());
        assertEquals("Task1", loaded.getAllDefaultTasks().get(0).getTitle());
    }

    @Test
    void loadFromFile() throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task defaultTask  = new Task("Task1", "Desc1");
        manager.createDefaultTask(defaultTask);

        Epic epic = new Epic("Epic1", "DescEpic1");
        manager.createEpicTask(epic);

        Epic epic2 = new Epic("Epic2", "DescEpic2");
        manager.createEpicTask(epic2);
        int epic2Id = epic2.getTaskId();

        SubTask subTask = new SubTask("Sub1", "DescSub1", epic2Id);
        manager.createSubTask(subTask);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        SubTask loadedSub = loaded.getAllSubTasks().get(0);

        assertEquals(epic2Id, loadedSub.getEpicId());
        assertEquals(2, loaded.getAllEpicTasks().size());
        assertEquals(1, loaded.getDefaultTasks().size());
        assertEquals(1, loaded.getAllSubTasks().size());
    }

    @Test
    void testToString() {
        FileBackedTaskManager manager = new FileBackedTaskManager(null);

        Task task = new Task("Task1", "Desc1");
        task.setTaskId(1);
        task.setTaskStatus(TaskStatus.NEW);

        String taskToString = manager.toString(task);

        assertEquals("1,TASK,Task1,NEW,Desc1,", taskToString);
    }

    @Test
    void fromString() {
        String taskString = "1,TASK,Task1,NEW,Desc1,";
        Task task = FileBackedTaskManager.fromString(taskString);

        assertNotNull(task);
        assertEquals(1, task.getTaskId());
        assertEquals("Task1", task.getTitle());
        assertEquals(TaskStatus.NEW, task.getTaskStatus());
        assertEquals("Desc1", task.getDescription());
    }
}