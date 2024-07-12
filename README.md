# Трекер задач

Трекер для эффективной организации совместной работы над задачами.

![Image](java-kanban.png)

На доске могут присутствовать три типа задач: обычная задача, эпичная задача и подзадачи. Обычная задача - это несложная задача, которую не требуется разбивать на подзадачи. Например, "проверить почту". Эпичная задача (или эпик) - это сложная задача, которая разбивается на подзадачи. Например, "исправить замечание" может быть разбито на "сварить кофе", "почитать хабр", "сделать фикс". Подзадачи - это то, из чего состоит эпик. 

Задачи, эпики и подзадачи могут иметь статусы ```To do```, ```In progress``` и ```Done```. При этом если у обычных задач и подзадач статусы можно установить непосредственно, то у эпиков статусы рассчитываются по следующему алгоритму:
1. если у эпика нет подзадач или все они имеют статус ```To do```, то статус должен быть ```To do```.
2. если все подзадачи имеют статус ```Done```, то и эпик считается завершённым — со статусом ```Done```.
3. во всех остальных случаях статус должен быть ```In progress```.

## Сценарии использования

### Сохранение задач

```java
import managers.InMemoryTaskManager;
import managers.TaskManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;// Инициализация менеджера

TaskManager manager = new InMemoryTaskManager();

// Сохранение обычной задачи
Task task = new Task("обычная скучная задача", "какое-то описание");
int taskId = manager.saveTask(task);

// Сохранение эпика
Epic epic = new Epic("сделать что-нибудь эпичное", "тут должно быть эпичное описание");
int epicId = manager.saveEpic(epic);

// Сохранение первой подзадачи эпика
Subtask subtask1 = new Subtask("подзадача эпика №1", "сделать первую часть эпичного");
int subtaskId1 = manager.saveSubtask(subtask1);

// Сохранение второй подзадачи эпика
Subtask subtask2 = new Subtask("подзадача эпика №2", "сделать вторую часть эпичного");
int subtaskId2 = manager.saveSubtask(subtask2);
```

### Получение задач

```java
// Предполагается, что мы заранее создали задачи, эпики и подзадачи

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;// Получаем все обычные задачи:

List<Task> tasks = manager.getTasks();

// Получаем обычную задачу по идентификатору
Task task = manager.getTaskById(taskId);

// Получаем все эпики:
List<Epic> epics = manager.getEpics();

// Получаем эпик по идентификатору
Epic epic = manager.getEpicById(epicId);

// Получаем все подзадачи:
List<Subtask> subtasks = manager.getSubtasks();

// Получаем подзадачу по идентификатору
Subtask subtask = manager.getSubtaskById(subtaskId);

// Получаем подзадачи эпика
List<Subtask> subtasks = manager.getSubtasksOfEpic(epic);

// Получаем эпик для подзадачи 
Epic epic = manager.getEpicOfSubtask(subtask);
```

### Обновление задач

```java
// Предполагается, что мы заранее создали задачи, эпики и подзадачи
// Новая задача перепишет старую с тем же идентификатором

// Обновление задачи 
manager.updateTask(task);

// Обновление эпика 
manager.updateEpic(epic);

// Обновление подзадачи 
manager.updateSubtask(subtask);
```

### Удаление задач

```java
// Предполагается, что мы заранее создали задачи, эпики и подзадачи

// Удаление всех задач 
manager.removeTasks();

// Удаление задачи по id 
manager.removeTaskById(taskId);

// Удаление всех эпиков 
manager.removeEpics();

// Удаление эпика по id
manager.removeEpicById(epicId);

// Удаление всех подзадач 
manager.removeSubtasks();

// Удаление подзадачи по id 
manager.removeSubtaskById(subtaskId);
```