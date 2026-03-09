import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager historyManager;
    private InMemoryTaskManager taskManager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        taskManager = new InMemoryTaskManager();
        task1 = new Task("Task 1", "Description 1");
        task1.setTaskId(1);
        task2 = new Task("Task 2", "Description 2");
        task2.setTaskId(2);
        task3 = new Task("Task 3", "Description 3");
        task3.setTaskId(3);
    }

    @Test
    void shouldAddTaskToHistory() {
        historyManager.add(task1);
        assertEquals(1, historyManager.getHistory().size());
        assertTrue(historyManager.getHistory().contains(task1));
    }

    @Test
    void shouldNotAddNullTask() {
        historyManager.add(null);
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void shouldMoveTaskToEndWhenAddedAgain() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task1, history.get(1));
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getTaskId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertFalse(history.contains(task2));
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void shouldRemoveFirstTaskFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getTaskId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void shouldRemoveLastTaskFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task3.getTaskId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void shouldRemoveMiddleTaskFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getTaskId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void shouldReturnEmptyHistoryWhenNoTasks() {
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void shouldHandleRemovingNonExistentTask() {
        historyManager.add(task1);
        historyManager.remove(999);

        assertEquals(1, historyManager.getHistory().size());
        assertEquals(task1, historyManager.getHistory().get(0));
    }

    @Test
    void shouldMaintainCorrectOrderAfterMultipleOperations() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(task2.getTaskId());
        historyManager.add(task2);
        historyManager.remove(task1.getTaskId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task3, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void shouldReturnEmptyHistoryWhenRemoveEpicsWithSubTasks() {
        Epic firstEpic = new Epic("Epic1", "Desc1");
        taskManager.createEpicTask(firstEpic);
        int firstEpicId = firstEpic.getTaskId();

        SubTask firstEpicSub = new SubTask("Sub1.1", "Desc1.1", firstEpicId);
        SubTask secondEpicSub = new SubTask("Sub1.2", "Desc1.2", firstEpicId);
        taskManager.createSubTask(firstEpicSub);
        taskManager.createSubTask(secondEpicSub);

        taskManager.getEpicTaskById(firstEpicId);
        taskManager.getSubTaskById(firstEpicSub.getTaskId());
        taskManager.getSubTaskById(secondEpicSub.getTaskId());

        Epic secondEpic = new Epic("Epic2", "Desc2");
        taskManager.createEpicTask(secondEpic);
        int secondEpicId = secondEpic.getTaskId();

        SubTask sub3 = new SubTask("Sub2.1", "Desc2.1", secondEpicId);
        taskManager.createSubTask(sub3);

        taskManager.getEpicTaskById(secondEpicId);
        taskManager.getSubTaskById(sub3.getTaskId());

        taskManager.removeAllEpicTasks();

        ArrayList<Task> history = taskManager.historyManager.getHistory();

        assertEquals(0, history.size());
    }

    @Test
    void shouldNotDuplicateTaskInHistory() {
        taskManager.createDefaultTask(task1);

        taskManager.getDefaultTaskById(task1.getTaskId());
        taskManager.getDefaultTaskById(task1.getTaskId());
        taskManager.getDefaultTaskById(task1.getTaskId());

        ArrayList<Task> history = taskManager.historyManager.getHistory();
        assertEquals(1, history.size());
    }

    @Test
    void shouldRemoveEpicSubTasksFromHistoryWhenEpicRemove() {
        Epic firstEpic = new Epic("Epic1", "Desc1");
        taskManager.createEpicTask(firstEpic);
        int firstEpicId = firstEpic.getTaskId();
        taskManager.getEpicTaskById(firstEpicId);

        SubTask firstEpicSub = new SubTask("Sub1.1", "Desc1.1", firstEpicId);
        SubTask secondEpicSub = new SubTask("Sub1.2", "Desc1.2", firstEpicId);
        taskManager.createSubTask(firstEpicSub);
        taskManager.createSubTask(secondEpicSub);
        taskManager.getSubTaskById(firstEpicSub.getTaskId());
        taskManager.getSubTaskById(secondEpicSub.getTaskId());
        taskManager.removeEpicTaskById(firstEpicId);

        assertEquals(0, historyManager.getHistory().size());
    }
}