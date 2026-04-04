import org.junit.jupiter.api.Test;
import task.Task;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void tasksAreEqualIfIdsAreEqual() {
        Task task1 = new Task("Title 1", "Description 1");
        Task task2 = new Task("Title 2", "Description 2");

        task1.setTaskId(1);
        task2.setTaskId(1);

        assertEquals(task1, task2, "Задачи должны быть равны при одинаковом id");
        assertEquals(task1.hashCode(), task2.hashCode(),
                "HashCode должен совпадать у равных объектов");
    }

    @Test
    void tasksAreNotEqualIfIdsAreDifferent() {
        Task task1 = new Task("Title", "Description");
        Task task2 = new Task("Title", "Description");

        task1.setTaskId(1);
        task2.setTaskId(2);

        assertNotEquals(task1, task2, "Задачи с разными id не должны быть равны");
    }

}
