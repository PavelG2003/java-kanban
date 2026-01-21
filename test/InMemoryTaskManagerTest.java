import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private InMemoryTaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void shouldCreateAndGetDefaultTask() {
        Task task = new Task("Task 1", "Description");

        taskManager.createDefaultTask(task);

        Task savedTask = taskManager.getDefaultTaskById(task.getTaskId());

        assertNotNull(savedTask);
        assertEquals("Task 1", savedTask.getTitle());
        assertEquals(TaskStatus.NEW, savedTask.getTaskStatus());
    }

    @Test
    void shouldRemoveDefaultTaskById() {
        Task task = new Task("Task", "Desc");
        taskManager.createDefaultTask(task);

        taskManager.removeDefaultTaskById(task.getTaskId());

        assertNull(taskManager.getDefaultTasks().get(task.getTaskId()));
    }

    @Test
    void shouldCreateEpicWithoutSubtasksWithNewStatus() {
        Epic epic = new Epic("Epic", "Desc");

        taskManager.createEpicTask(epic);

        Epic savedEpic = taskManager.getEpicTaskById(epic.getTaskId());
        assertEquals(TaskStatus.NEW, savedEpic.getTaskStatus());
    }

    @Test
    void epicStatusShouldBeNewWhenAllSubtasksNew() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.createEpicTask(epic);

        SubTask sub1 = new SubTask("Sub1", "Desc", epic.getTaskId());
        SubTask sub2 = new SubTask("Sub2", "Desc", epic.getTaskId());

        taskManager.createSubTask(sub1);
        taskManager.createSubTask(sub2);

        Epic savedEpic = taskManager.getEpicTaskById(epic.getTaskId());
        assertEquals(TaskStatus.NEW, savedEpic.getTaskStatus());
    }

    @Test
    void epicStatusShouldBeDoneWhenAllSubtasksDone() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.createEpicTask(epic);

        SubTask sub1 = new SubTask("Sub1", "Desc", epic.getTaskId());
        SubTask sub2 = new SubTask("Sub2", "Desc", epic.getTaskId());


        taskManager.createSubTask(sub1);
        taskManager.createSubTask(sub2);

        sub1.setTaskStatus(TaskStatus.DONE);
        sub2.setTaskStatus(TaskStatus.DONE);

        taskManager.updateSubTask(sub1);
        taskManager.updateSubTask(sub2);

        Epic savedEpic = taskManager.getEpicTaskById(epic.getTaskId());
        assertEquals(TaskStatus.DONE, savedEpic.getTaskStatus());
    }

    @Test
    void epicStatusShouldBeInProgressWhenMixedStatuses() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.createEpicTask(epic);

        SubTask sub1 = new SubTask("Sub1", "Desc", epic.getTaskId());
        SubTask sub2 = new SubTask("Sub2", "Desc", epic.getTaskId());


        taskManager.createSubTask(sub1);
        taskManager.createSubTask(sub2);

        sub2.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubTask(sub2);

        Epic savedEpic = taskManager.getEpicTaskById(epic.getTaskId());
        assertEquals(TaskStatus.IN_PROGRESS, savedEpic.getTaskStatus());
    }

    @Test
    void shouldRemoveSubtaskAndUpdateEpic() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.createEpicTask(epic);

        SubTask sub = new SubTask("Sub", "Desc", epic.getTaskId());
        taskManager.createSubTask(sub);

        taskManager.removeSubTaskById(sub.getTaskId());

        ArrayList<SubTask> subtasks = taskManager.getAllSubTasksFromEpic(epic.getTaskId());
        assertTrue(subtasks.isEmpty());

        Epic savedEpic = taskManager.getEpicTaskById(epic.getTaskId());
        assertEquals(TaskStatus.NEW, savedEpic.getTaskStatus());
    }

    @Test
    void shouldReturnEmptySubtaskListForUnknownEpic() {
        ArrayList<SubTask> subtasks = taskManager.getAllSubTasksFromEpic(999);

        assertNotNull(subtasks);
        assertTrue(subtasks.isEmpty());
    }
    @Test
    void shouldAddTaskToHistory() {
        Task task = new Task("Task", "Desc");
        taskManager.createDefaultTask(task);

        taskManager.getDefaultTaskById(task.getTaskId());

        ArrayList<Task> history = taskManager.historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }

    @Test
    void shouldNotAddNullToHistory() {
        taskManager.getDefaultTaskById(999);

        ArrayList<Task> history = taskManager.historyManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    void shouldNotDuplicateTaskInHistory() {
        Task task = new Task("Task", "Desc");
        taskManager.createDefaultTask(task);

        taskManager.getDefaultTaskById(task.getTaskId());
        taskManager.getDefaultTaskById(task.getTaskId());
        taskManager.getDefaultTaskById(task.getTaskId());

        ArrayList<Task> history = taskManager.historyManager.getHistory();
        assertEquals(1, history.size());
    }

    @Test
    void shouldMoveTaskToEndWhenViewedAgain() {
        Task task1 = new Task("Task1", "Desc");
        Task task2 = new Task("Task2", "Desc");

        taskManager.createDefaultTask(task1);
        taskManager.createDefaultTask(task2);

        taskManager.getDefaultTaskById(task1.getTaskId());
        taskManager.getDefaultTaskById(task2.getTaskId());
        taskManager.getDefaultTaskById(task1.getTaskId());

        ArrayList<Task> history = taskManager.historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task1, history.get(1));
    }

    @Test
    void shouldStoreDifferentTaskTypesInHistory() {
        Task task = new Task("Task", "Desc");
        Epic epic = new Epic("Epic", "Desc");

        taskManager.createDefaultTask(task);
        taskManager.createEpicTask(epic);

        taskManager.getDefaultTaskById(task.getTaskId());
        taskManager.getEpicTaskById(epic.getTaskId());

        ArrayList<Task> history = taskManager.historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(task, history.get(0));
        assertEquals(epic, history.get(1));
    }




}
