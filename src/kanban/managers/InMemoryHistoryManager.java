package kanban.managers;

import kanban.tasks.Task;

import java.util.*;

class InMemoryHistoryManager implements HistoryManager {

    private final TaskLinkedList tasks;

    InMemoryHistoryManager() {
        tasks = new TaskLinkedList();
    }

    @Override
    public synchronized void add(Task task) {
        tasks.linkLast(task);
    }

    @Override
    public void remove(int id) {
        tasks.removeById(id);
    }

    @Override
    public void clear() {
        tasks.clear();
    }

    @Override
    public List<Task> getHistory() {
        return tasks.getTasks();
    }

    private static class TaskLinkedList {
        private Node head;
        private Node tail;
        private final Map<Integer, Node> nodeByTaskId = new HashMap<>();

        public void removeById(int id) {
            Node node = nodeByTaskId.get(id);
            if (node != null) {
                removeNode(node);
            }
        }

        public void clear() {
            Node node = head;
            while (node != null) {
                nodeByTaskId.remove(node.item.getId());
                Node next = node.next;
                node.item = null;
                node.next = null;
                node.prev = null;
                node = next;
            }
            head = tail = null;
        }

        public List<Task> getTasks() {
            List<Task> tasks = new ArrayList<>();

            Node node = tail;
            while (node != null) {
                tasks.add(node.item);
                node = node.prev;
            }

            return tasks;
        }

        void linkLast(Task task) {

            removeById(task.getId());

            Node oldTail = tail;
            Node newNode = new Node(oldTail, task, null);

            tail = newNode;
            if (oldTail == null)
                head = newNode;
            else
                oldTail.next = newNode;

            nodeByTaskId.put(task.getId(), newNode);
        }

        void removeNode(Node node) {
            final Task task = node.item;
            final Node next = node.next;
            final Node prev = node.prev;

            nodeByTaskId.remove(task.getId());

            if (prev == null) {
                head = next;
            } else {
                prev.next = next;
                node.prev = null;
            }

            if (next == null) {
                tail = prev;
            } else {
                next.prev = prev;
                node.next = null;
            }

            node.item = null;
        }

        private static class Node {
            Node next;
            Node prev;
            Task item;

            Node(Node prev, Task item, Node next) {
                this.prev = prev;
                this.item = item;
                this.next = next;
            }
        }
    }
}