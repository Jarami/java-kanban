public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        Task task1 = new Task("task1", "Some description of task 1");
        Task task2 = new Task("task2", "Some description of task 2");

        Epic epic1 = new Epic("epic1", "Some description of epic 1");

        SubTask subtask11 = new SubTask("subtask11", "Some description of subtask 1 for epic1");
        SubTask subtask12 = new SubTask("subtask12", "Some description of subtask 2 for epic1");
        epic1.addSubTask(subtask11);
        epic1.addSubTask(subtask12);

        Epic epic2 = new Epic("epic2", "Some description of epic 2");
        SubTask subtask21 = new SubTask("subtask21", "Some description of subtask 1 for epic2");
        epic1.addSubTask(subtask21);

        System.out.println("task1 = " + task1);
        System.out.println("task2 = " + task2);

        System.out.println("epic1 = " + epic1);
        System.out.println("subtask11 = " + subtask11);
        System.out.println("subtask12 = " + subtask12);

        System.out.println("epic2 = " + epic2);
        System.out.println("subtask21 = " + subtask21);
    }
}
