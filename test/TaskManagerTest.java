import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;
import task.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager() throws Exception;

    @BeforeEach
    void initTaskManager() throws Exception {
        taskManager = createTaskManager();
    }

    @Test
    void shouldCreateAndGetDefaultTask() {
        Task task = createTask("Task 1", 9, 60);

        taskManager.createDefaultTask(task);

        Task savedTask = taskManager.getDefaultTaskById(task.getTaskId());
        assertNotNull(savedTask);
        assertEquals(task, savedTask);
        assertEquals("Task 1", savedTask.getTitle());
        assertEquals(TaskStatus.NEW, savedTask.getTaskStatus());
    }

    @Test
    void shouldReturnAllDefaultTasks() {
        Task task1 = createTask("Task 1", 9, 30);
        Task task2 = createTask("Task 2", 10, 30);

        taskManager.createDefaultTask(task1);
        taskManager.createDefaultTask(task2);

        ArrayList<Task> tasks = taskManager.getAllDefaultTasks();
        assertEquals(2, tasks.size());
        assertTrue(tasks.contains(task1));
        assertTrue(tasks.contains(task2));
    }

    @Test
    void shouldUpdateDefaultTask() {
        Task task = createTask("Task", 9, 30);
        taskManager.createDefaultTask(task);

        Task updatedTask = createTask("Updated", 11, 40);
        updatedTask.setTaskId(task.getTaskId());
        updatedTask.setTaskStatus(TaskStatus.DONE);

        taskManager.updateDefaultTask(updatedTask);

        Task savedTask = taskManager.getDefaultTaskById(task.getTaskId());
        assertEquals("Updated", savedTask.getTitle());
        assertEquals(TaskStatus.DONE, savedTask.getTaskStatus());
        assertEquals(LocalDateTime.of(2026, 1, 1, 11, 0), savedTask.getStartTime());
    }

    @Test
    void shouldRemoveDefaultTaskById() {
        Task task = createTask("Task", 9, 30);
        taskManager.createDefaultTask(task);

        taskManager.removeDefaultTaskById(task.getTaskId());

        assertNull(taskManager.getDefaultTasks().get(task.getTaskId()));
    }

    @Test
    void shouldRemoveAllDefaultTasks() {
        taskManager.createDefaultTask(createTask("Task 1", 9, 30));
        taskManager.createDefaultTask(createTask("Task 2", 10, 30));

        taskManager.removeAllDefaultTasks();

        assertTrue(taskManager.getAllDefaultTasks().isEmpty());
    }

    @Test
    void shouldCreateAndGetEpic() {
        Epic epic = new Epic("Epic", "Desc");

        taskManager.createEpicTask(epic);

        Epic savedEpic = taskManager.getEpicTaskById(epic.getTaskId());
        assertNotNull(savedEpic);
        assertEquals(epic, savedEpic);
        assertEquals(TaskStatus.NEW, savedEpic.getTaskStatus());
    }

    @Test
    void shouldUpdateEpicTitleAndDescription() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.createEpicTask(epic);

        Epic updatedEpic = new Epic("Updated epic", "Updated desc");
        updatedEpic.setTaskId(epic.getTaskId());
        updatedEpic.setDuration(0);

        taskManager.updateEpicTask(updatedEpic);

        Epic savedEpic = taskManager.getEpicTaskById(epic.getTaskId());
        assertEquals("Updated epic", savedEpic.getTitle());
        assertEquals("Updated desc", savedEpic.getDescription());
    }

    @Test
    void shouldRemoveEpicById() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.createEpicTask(epic);

        taskManager.removeEpicTaskById(epic.getTaskId());

        assertNull(taskManager.getEpicTasks().get(epic.getTaskId()));
    }

    @Test
    void shouldRemoveAllEpics() {
        taskManager.createEpicTask(new Epic("Epic 1", "Desc"));
        taskManager.createEpicTask(new Epic("Epic 2", "Desc"));

        taskManager.removeAllEpicTasks();

        assertTrue(taskManager.getAllEpicTasks().isEmpty());
    }

    @Test
    void shouldCreateSubtaskAndLinkItWithEpic() {
        Epic epic = createEpicWithManager();
        SubTask subTask = createSubTask("Sub", epic, 9, 30);

        taskManager.createSubTask(subTask);

        SubTask savedSubTask = (SubTask) taskManager.getSubTaskById(subTask.getTaskId());
        assertNotNull(savedSubTask);
        assertEquals(epic.getTaskId(), savedSubTask.getEpicId());
        assertTrue(epic.getSubTaskIds().contains(subTask.getTaskId()));
        assertEquals(List.of(subTask), taskManager.getAllSubTasksFromEpic(epic.getTaskId()));
    }

    @Test
    void shouldUpdateSubtaskAndRecalculateEpicStatus() {
        Epic epic = createEpicWithManager();
        SubTask subTask = createSubTask("Sub", epic, 9, 30);
        taskManager.createSubTask(subTask);

        subTask.setTaskStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subTask);

        assertEquals(TaskStatus.DONE, taskManager.getEpicTaskById(epic.getTaskId()).getTaskStatus());
    }

    @Test
    void shouldRemoveSubtaskAndUpdateEpic() {
        Epic epic = createEpicWithManager();
        SubTask subTask = createSubTask("Sub", epic, 9, 30);
        taskManager.createSubTask(subTask);
        subTask.setTaskStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subTask);

        taskManager.removeSubTaskById(subTask.getTaskId());

        assertNull(taskManager.getSubTasks().get(subTask.getTaskId()));
        assertTrue(taskManager.getAllSubTasksFromEpic(epic.getTaskId()).isEmpty());
        assertTrue(epic.getSubTaskIds().isEmpty());
        assertEquals(TaskStatus.NEW, taskManager.getEpicTaskById(epic.getTaskId()).getTaskStatus());
    }

    @Test
    void shouldRemoveAllSubtasksAndClearEpicLinks() {
        Epic epic = createEpicWithManager();
        SubTask subTask1 = createSubTask("Sub 1", epic, 9, 30);
        SubTask subTask2 = createSubTask("Sub 2", epic, 10, 30);
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);

        taskManager.removeAllSubTasks();

        assertTrue(taskManager.getAllSubTasks().isEmpty());
        assertTrue(epic.getSubTaskIds().isEmpty());
        assertEquals(TaskStatus.NEW, epic.getTaskStatus());
    }

    @Test
    void shouldReturnEmptySubtaskListForUnknownEpic() {
        ArrayList<SubTask> subtasks = taskManager.getAllSubTasksFromEpic(999);

        assertNotNull(subtasks);
        assertTrue(subtasks.isEmpty());
    }

    @Test
    void epicStatusShouldBeNewWhenAllSubtasksNew() {
        Epic epic = createEpicWithManager();
        taskManager.createSubTask(createSubTask("Sub 1", epic, 9, 30));
        taskManager.createSubTask(createSubTask("Sub 2", epic, 10, 30));

        assertEquals(TaskStatus.NEW, taskManager.getEpicTaskById(epic.getTaskId()).getTaskStatus());
    }

    @Test
    void epicStatusShouldBeDoneWhenAllSubtasksDone() {
        Epic epic = createEpicWithManager();
        SubTask subTask1 = createSubTask("Sub 1", epic, 9, 30);
        SubTask subTask2 = createSubTask("Sub 2", epic, 10, 30);
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);

        subTask1.setTaskStatus(TaskStatus.DONE);
        subTask2.setTaskStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subTask1);
        taskManager.updateSubTask(subTask2);

        assertEquals(TaskStatus.DONE, taskManager.getEpicTaskById(epic.getTaskId()).getTaskStatus());
    }

    @Test
    void epicStatusShouldBeInProgressWhenSubtasksAreNewAndDone() {
        Epic epic = createEpicWithManager();
        SubTask newSubTask = createSubTask("New sub", epic, 9, 30);
        SubTask doneSubTask = createSubTask("Done sub", epic, 10, 30);
        taskManager.createSubTask(newSubTask);
        taskManager.createSubTask(doneSubTask);

        doneSubTask.setTaskStatus(TaskStatus.DONE);
        taskManager.updateSubTask(doneSubTask);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicTaskById(epic.getTaskId()).getTaskStatus());
    }

    @Test
    void epicStatusShouldBeInProgressWhenSubtasksAreInProgress() {
        Epic epic = createEpicWithManager();
        SubTask subTask1 = createSubTask("Sub 1", epic, 9, 30);
        SubTask subTask2 = createSubTask("Sub 2", epic, 10, 30);
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);

        subTask1.setTaskStatus(TaskStatus.IN_PROGRESS);
        subTask2.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubTask(subTask1);
        taskManager.updateSubTask(subTask2);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicTaskById(epic.getTaskId()).getTaskStatus());
    }

    @Test
    void shouldUpdateEpicTimeAndDurationBySubtasks() {
        Epic epic = createEpicWithManager();
        taskManager.createSubTask(createSubTask("Sub 1", epic, 10, 60));
        taskManager.createSubTask(createSubTask("Sub 2", epic, 9, 30));

        assertEquals(LocalDateTime.of(2026, 1, 1, 9, 0), epic.getStartTime());
        assertEquals(LocalDateTime.of(2026, 1, 1, 11, 0), epic.getEndTime());
        assertEquals(Duration.ofMinutes(90), epic.getDuration());
    }

    @Test
    void shouldReturnEpicSubtasks() {
        Epic epic = createEpicWithManager();
        SubTask subTask1 = createSubTask("Sub 1", epic, 9, 30);
        SubTask subTask2 = createSubTask("Sub 2", epic, 10, 30);
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);

        List<SubTask> subtasks = taskManager.getEpicSubTasks(epic);

        assertEquals(2, subtasks.size());
        assertTrue(subtasks.contains(subTask1));
        assertTrue(subtasks.contains(subTask2));
    }

    @Test
    void shouldReturnPrioritizedTasksSortedByStartTime() {
        Task task = createTask("Task", 10, 30);
        Epic epic = createEpicWithManager();
        SubTask subTask = createSubTask("Sub", epic, 9, 30);
        taskManager.createDefaultTask(task);
        taskManager.createSubTask(subTask);

        List<Task> prioritizedTasks = new ArrayList<>(taskManager.getPrioritizedTasks());

        assertEquals(List.of(subTask, task), prioritizedTasks);
    }

    @Test
    void shouldDetectOverlappingTimeIntervals() {
        Task first = createTask("First", 9, 60);
        Task second = createTask("Second", 9, 30);

        assertTrue(taskManager.isTasksOverlap(first, second));
    }

    @Test
    void shouldNotDetectOverlapWhenIntervalsTouchByBoundary() {
        Task first = createTask("First", 9, 60);
        Task second = createTask("Second", 10, 30);

        assertFalse(taskManager.isTasksOverlap(first, second));
    }

    @Test
    void shouldPreventCreatingOverlappingTasks() {
        taskManager.createDefaultTask(createTask("First", 9, 60));
        Task overlappingTask = createTask("Second", 9, 30);

        assertThrows(IllegalArgumentException.class, () -> taskManager.createDefaultTask(overlappingTask));
    }

    protected Epic createEpicWithManager() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.createEpicTask(epic);
        return epic;
    }

    protected Task createTask(String title, int hour, long duration) {
        return new Task(title, "Desc", LocalDateTime.of(2026, 1, 1, hour, 0), duration);
    }

    protected SubTask createSubTask(String title, Epic epic, int hour, long duration) {
        return new SubTask(title, "Desc", epic.getTaskId(), LocalDateTime.of(2026, 1, 1, hour, 0), duration);
    }
}
