import manager.FileBackedTaskManager;
import manager.ManagerSaveException;
import org.junit.jupiter.api.Test;
import task.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @Override
    protected FileBackedTaskManager createTaskManager() throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        file.deleteOnExit();
        return new FileBackedTaskManager(file);
    }

    @Test
    void save() throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task defaultTask  = new Task("Task1", "Desc1", LocalDateTime.now(), 100);
        manager.createDefaultTask(defaultTask);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loaded.getAllDefaultTasks().size());
        assertEquals("Task1", loaded.getAllDefaultTasks().get(0).getTitle());
    }

    @Test
    void loadFromFile() throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task defaultTask  = new Task("Task1", "Desc1", LocalDateTime.of(2026, 6, 13, 12, 5), 120);
        manager.createDefaultTask(defaultTask);

        Epic epic = new Epic("Epic1", "DescEpic1");
        manager.createEpicTask(epic);

        Epic epic2 = new Epic("Epic2", "DescEpic2");
        manager.createEpicTask(epic2);
        int epic2Id = epic2.getTaskId();

        SubTask subTask = new SubTask("Sub1", "DescSub1", epic2Id, LocalDateTime.now(), 120);
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
        LocalDateTime time = LocalDateTime.now();

        Task task = new Task("Task1", "Desc1", time, 100);
        task.setTaskId(1);
        task.setTaskStatus(TaskStatus.NEW);

        String taskToString = manager.toString(task);

        assertEquals("1,TASK,Task1,NEW,Desc1, ," + time + ",100", taskToString);
    }

    @Test
    void fromString() {
        String taskString = "1,TASK,Task1,NEW,Desc1, ,2026-05-11T14:25:47.423630200,120";
        Task task = FileBackedTaskManager.fromString(taskString);

        assertNotNull(task);
        assertEquals(1, task.getTaskId());
        assertEquals(TaskTypes.TASK, task.getType());
        assertEquals("Task1", task.getTitle());
        assertEquals(TaskStatus.NEW, task.getTaskStatus());
        assertEquals("Desc1", task.getDescription());
    }

    @Test
    void shouldNotThrowWhenSavingToAvailableFile() throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        file.deleteOnExit();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        assertDoesNotThrow(() -> manager.createDefaultTask(
                new Task("Task", "Desc", LocalDateTime.of(2026, 1, 1, 9, 0), 30)
        ));
    }

    @Test
    void shouldThrowManagerSaveExceptionWhenSavingToDirectory() throws IOException {
        File directory = File.createTempFile("tasks-directory", "");
        assertTrue(directory.delete());
        assertTrue(directory.mkdir());
        directory.deleteOnExit();
        FileBackedTaskManager manager = new FileBackedTaskManager(directory);

        assertThrows(ManagerSaveException.class, () -> manager.createDefaultTask(
                new Task("Task", "Desc", LocalDateTime.of(2026, 1, 1, 9, 0), 30)
        ));
    }

    @Test
    void shouldThrowManagerSaveExceptionWhenLoadingMissingFile() {
        File missingFile = new File("missing-file-for-test.csv");

        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(missingFile));
    }
}
