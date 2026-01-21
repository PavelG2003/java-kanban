public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Task firstTask = new Task("Домашка", "Надо решить 5 примеров по математике");
        Task secondTask = new Task("Уборка", "Надо помыть полы в двух комнатах и на кухне");

        Epic firstEpic = new Epic("Переезд", "Переезд в новую квартиру");

        taskManager.createDefaultTask(firstTask);
        taskManager.createDefaultTask(secondTask);

        taskManager.createEpicTask(firstEpic);
        int firstEpicId = firstEpic.getTaskId();
        SubTask firstEpicSub = new SubTask("Собрать вещи", "Собрать все вещи из старой квартиры и " +
                "перевезти в новую", firstEpicId);
        SubTask secondEpicSub = new SubTask("Заселиться", "Разложить все вещи в новой квартире",
                firstEpicId);

        taskManager.createSubTask(firstEpicSub);
        taskManager.createSubTask(secondEpicSub);

        System.out.println(taskManager.getAllEpicTasks());
        System.out.println(taskManager.getAllDefaultTasks());
        System.out.println(taskManager.getAllSubTasks());

        firstTask.setTaskStatus(TaskStatus.DONE);
        taskManager.updateDefaultTask(firstTask);
        System.out.println(firstTask);

        secondTask.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateDefaultTask(secondTask);
        System.out.println(secondTask);

        firstEpicSub.setTaskStatus(TaskStatus.DONE);
        taskManager.updateSubTask(firstEpicSub);
        System.out.println(firstEpicSub);
        System.out.println(firstEpic);

        secondEpicSub.setTaskStatus(TaskStatus.DONE);
        taskManager.updateSubTask(secondEpicSub);
        System.out.println(secondEpicSub);
        System.out.println(firstEpic);

        taskManager.removeDefaultTaskById(1);
        taskManager.removeEpicTaskById(3);
        System.out.println("Remove " + taskManager.getAllEpicTasks());

    }
}
