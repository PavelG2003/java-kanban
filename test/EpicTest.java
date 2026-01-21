import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    void getSubTaskIds() {
        Epic epic = new Epic("Epic title", "Epic description");

        ArrayList<Integer> subTaskIds = epic.getSubTaskIds();

        assertNotNull(subTaskIds, "Список подзадач не должен быть null");
        assertTrue(subTaskIds.isEmpty(), "Новый эпик должен содержать пустой список подзадач");
    }

    @Test
    void addSubTaskId() {
        Epic epic = new Epic("Epic title", "Epic description");

        epic.addSubTaskId(1);
        epic.addSubTaskId(2);

        ArrayList<Integer> subTaskIds = epic.getSubTaskIds();

        assertEquals(2, subTaskIds.size(), "Должно быть добавлено две подзадачи");
        assertTrue(subTaskIds.contains(1), "Список должен содержать id 1");
        assertTrue(subTaskIds.contains(2), "Список должен содержать id 2");
    }

    @Test
    void removeSubTask() {
        Epic epic = new Epic("Epic title", "Epic description");

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
        Epic epic = new Epic("Epic title", "Epic description");

        epic.addSubTaskId(1);
        epic.addSubTaskId(2);

        epic.clearSubTasks();

        assertTrue(epic.getSubTaskIds().isEmpty(), "Список подзадач должен быть очищен");
    }

    @Test
    void epicsAreEqualIfIdsAreEqual() {
        Epic epic1 = new Epic("Epic 1", "Description 1");
        Epic epic2 = new Epic("Epic 2", "Description 2");

        epic1.setTaskId(10);
        epic2.setTaskId(10);

        assertEquals(epic1, epic2, "Эпики должны быть равны при одинаковом id");
        assertEquals(epic1.hashCode(), epic2.hashCode(),
                "HashCode должен совпадать у равных эпиков");
    }

    @Test
    void epicsAreNotEqualIfIdsAreDifferent() {
        Epic epic1 = new Epic("Epic", "Description");
        Epic epic2 = new Epic("Epic", "Description");

        epic1.setTaskId(1);
        epic2.setTaskId(2);

        assertNotEquals(epic1, epic2, "Эпики с разными id не должны быть равны");
    }

    @Test
    void epicCannotContainItselfAsSubtask() {
        Epic epic = new Epic("Epic", "Description");
        epic.setTaskId(1);

        epic.addSubTaskId(1);

        assertFalse(epic.getSubTaskIds().contains(epic.getTaskId()),
                "Epic не должен содержать самого себя в списке подзадач");
    }



}
