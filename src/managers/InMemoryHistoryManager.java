package managers;

import tasks.Task;

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
    public void remove(int id){
        tasks.removeById(id);
    };

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
        private Map<Integer, Node> nodeByTaskId = new HashMap<>();

        public int size() {
            return nodeByTaskId.size();
        }

        public void removeById(int id) {
            Node node = nodeByTaskId.get(id);
            if (node != null) {
                removeNode(node);
            }
        }

        public void clear() {
            for (Node x = head; x != null; ) {
                nodeByTaskId.remove(x.item.getId());
                Node next = x.next;
                x.item = null;
                x.next = null;
                x.prev = null;
                x = next;
            }
            head = tail = null;
        }

        public List<Task> getTasks() {
            List<Task> tasks = new ArrayList<>();

            Node x = head;
            while (x != null) {
                tasks.add(x.item);
                x = x.next;
            }

            return tasks;
        }

        void linkLast(Task task) {

            removeById(task.getId());

            Node l = tail;
            Node newNode = new Node(l, task, null);

            tail = newNode;
            if (l == null)
                head = newNode;
            else
                l.next = newNode;

            nodeByTaskId.put(task.getId(), newNode);
        }

        void removeNode(Node x) {
            final Task elem = x.item;
            final Node next = x.next;
            final Node prev = x.prev;

            nodeByTaskId.remove(elem.getId());

            if (prev == null) {
                head = next;
            } else {
                prev.next = next;
                x.prev = null;
            }

            if (next == null) {
                tail = prev;
            } else {
                next.prev = prev;
                x.next = null;
            }

            x.item = null;
        }

        private static class Node {
            Node next;
            Node prev;
            Task item;

            Node(Node prev, Task item, Node next) {
                this.prev = prev;
                this.next = next;
                this.item = item;
            }
        }
    }
}