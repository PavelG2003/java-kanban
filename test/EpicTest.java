import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    Epic epic;

    @BeforeEach
    void createEpic() {
        epic = new Epic("Epic title", "Epic description");
    }

    @Test
    void getSubTaskIds() {

        ArrayList<Integer> subTaskIds = epic.getSubTaskIds();

        assertNotNull(subTaskIds, "Список подзадач не должен быть null");
        assertTrue(subTaskIds.isEmpty(), "Новый эпик должен содержать пустой список подзадач");
    }

    @Test
    void addSubTaskId() {

        epic.addSubTaskId(1);
        epic.addSubTaskId(2);

        ArrayList<Integer> subTaskIds = epic.getSubTaskIds();

        assertEquals(2, subTaskIds.size(), "Должно быть добавлено две подзадачи");
        assertTrue(subTaskIds.contains(1), "Список должен содержать id 1");
        assertTrue(subTaskIds.contains(2), "Список должен содержать id 2");
    }

    @Test
    void removeSubTask() {

        epic.addSubTaskId(1);
        epic.addSubTaskId(2);
        epic.addSubTaskId(3);

        epic.removeSubTask(2);

        ArrayList<Integer> subTaskIds = epic.getSubTaskIds();

        assertEquals(2, subTaskIds.size(), "После удаления должна остаться 2 подзадачи");
        assertFalse(subTaskIds.contains(2), "Подзадача с id 2 должна быть удалена");
    }

    @Test
    void clearSubTasks() {

        epic.addSubTaskId(1);
        epic.addSubTaskId(2);

        epic.clearSubTasks();

        assertTrue(epic.getSubTaskIds().isEmpty(), "Список подзадач должен быть очищен");
    }

    @Test
    void epicsAreEqualIfIdsAreEqual() {
        Epic epic2 = new Epic("Epic 2", "Description 2");

        epic.setTaskId(10);
        epic2.setTaskId(10);

        assertEquals(epic, epic2, "Эпики должны быть равны при одинаковом id");
        assertEquals(epic.hashCode(), epic2.hashCode(),
                "HashCode должен совпадать у равных эпиков");
    }

    @Test
    void epicsAreNotEqualIfIdsAreDifferent() {
        Epic epic2 = new Epic("Epic", "Description");

        epic.setTaskId(1);
        epic2.setTaskId(2);

        assertNotEquals(epic, epic2, "Эпики с разными id не должны быть равны");
    }

    @Test
    void epicCannotContainItselfAsSubtask() {
        epic.setTaskId(1);

        epic.addSubTaskId(1);

        assertFalse(epic.getSubTaskIds().contains(epic.getTaskId()),
                "Epic не должен содержать самого себя в списке подзадач");
    }
}
