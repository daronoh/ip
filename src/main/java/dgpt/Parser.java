package dgpt;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.List;

import dgpt.exception.IncorrectInputException;
import dgpt.exception.TaskNotFoundException;
import dgpt.task.Task;
import dgpt.task.TaskList;

/**
 * The Parser class is responsible for interpreting user input and converting it into commands
 * that interact with the TaskList and Ui components of the Dgpt application.
 * It parses the input string, identifies the command, and performs the corresponding action
 * on the task list while providing feedback to the user interface.
 */
public class Parser {

    /**
     * Parses the user input and executes the corresponding command on the task list.
     * This method identifies the command type and performs the appropriate action,
     * such as marking or unmarking tasks, adding new tasks, or deleting tasks.
     * It also handles exceptions related to incorrect input and task operations.
     *
     * @param text The user input string that contains the command and its parameters.
     * @param taskList The TaskList instance where tasks are managed.
     * @return A string representing the response given by Dgpt after taking in an input.
     * @throws IncorrectInputException If the input format is incorrect or missing required parameters.
     * @throws TaskNotFoundException If the command refers to a task that cannot be found.
     */

    public static String parse(String text, TaskList taskList, Storage storage) throws IncorrectInputException,
            TaskNotFoundException {

        String[] inputs = text.split(" ", 2);
        String command = inputs[0];

        switch (command) {
        case "list" -> {
            if (inputs.length != 1) {
                throw new IncorrectInputException("You should not have anything after your request. "
                        + "(e.g. \"list\")");
            }

            return Ui.listUi(taskList);
        }
        case "mark" -> {
            if (inputs.length != 2) {
                throw new IncorrectInputException("You should have only 1 number after your request. "
                        + "(e.g. \"mark 1\")");
            }

            int index = Integer.parseInt(inputs[1]) - 1;

            if (index < 0 || index >= taskList.getSize()) {
                throw new TaskNotFoundException("There doesn't seem to be a Task at that position.");
            }

            Task currTask = taskList.markTask(index);

            return Ui.markUi(currTask);
        }
        case "unmark" -> {
            if (inputs.length != 2) {
                throw new IncorrectInputException("You should have only 1 number after your request. "
                        + "(e.g. \"unmark 1\")");
            }

            int index = Integer.parseInt(inputs[1]) - 1;

            if (index < 0 || index >= taskList.getSize()) {
                throw new TaskNotFoundException("There doesn't seem to be a Task at that position.");
            }

            Task currTask = taskList.unmarkTask(index);

            return Ui.unmarkUi(currTask);
        }
        case "todo" -> {
            if (inputs.length != 2) {
                throw new IncorrectInputException("You should have a description after your request. "
                        + "(e.g. \"todo your_description\")");
            }

            String description = inputs[1];
            Task addedTask = taskList.addToDoToList(description);
            int sizeOfList = taskList.getSize();

            return Ui.addTaskUi(addedTask, sizeOfList);
        }
        case "deadline" -> {
            if (inputs.length != 2) {
                throw new IncorrectInputException("You should have a description after your request. "
                        + "(e.g. \"todo your_description /by your_deadline\")");
            }

            String[] parts = inputs[1].split(" /by ");

            if (parts.length != 2) {
                throw new IncorrectInputException("You should have a timing after your request. "
                        + "(e.g. \"todo your_description /event your_deadline\")");
            }

            try {
                String description = parts[0];
                String deadline = parts[1];

                Task addedTask = taskList.addDeadlineToList(description, deadline);
                int sizeOfList = taskList.getSize();

                return Ui.addTaskUi(addedTask, sizeOfList);
            } catch (DateTimeParseException e) {
                return Ui.errorUi(e);
            }
        }
        case "event" -> {
            if (inputs.length != 2) {
                throw new IncorrectInputException("You should have a description after your request. "
                        + "(e.g. \"todo your_description /from your_start_time /to your_end_time\")");
            }

            String[] parts = inputs[1].split(" /");
            if (parts.length != 3) {
                throw new IncorrectInputException("You should have 2 timings after your request. "
                        + "(e.g. \"todo your_description /from your_start_time /to your_end_time\")");
            }

            try {

                String description = parts[0];
                String startTime = parts[1].substring(5);
                String endTime = parts[2].substring(3);

                Task addedTask = taskList.addEventToList(description, startTime, endTime);
                int sizeOfList = taskList.getSize();


                return Ui.addTaskUi(addedTask, sizeOfList);
            } catch (DateTimeParseException e) {
                return Ui.errorUi(e);
            }
        }
        case "delete" -> {
            if (inputs.length != 2) {
                throw new IncorrectInputException("You should have only 1 number after your request. "
                        + "(e.g. \"delete 1\")");
            }

            int index = Integer.parseInt(inputs[1]) - 1;

            if (index < 0 || index >= taskList.getSize()) {
                throw new TaskNotFoundException("There doesn't seem to be a Task at that position.");
            }

            Task deletedTask = taskList.deleteTask(index);
            int size = taskList.getSize();

            return Ui.deleteUi(deletedTask, size);
        }

        case "find" -> {
            if (inputs.length != 2) {
                throw new IncorrectInputException("You should input what you're searching for after \"find\""
                        + " (e.g. \"find task\")");
            }

            String keyword = inputs[1];
            List<Task> matchingTasks = taskList.findTasks(keyword);

            return Ui.findUi(matchingTasks);
        }

        case "save" -> {
            try {
                storage.save(taskList);
                return Ui.saveUi();
            } catch (IOException e) {
                return Ui.errorUi(e);
            }
        }

        case "recurring" -> {
            if (inputs.length != 2) {
                throw new IncorrectInputException("You should have a description after your request. "
                        + "(e.g. \"todo your_description /event your_frequency\")");
            }

            String[] parts = inputs[1].split(" /every ", 2);

            if (parts.length != 2) {
                throw new IncorrectInputException("You should have a timing after your request. "
                        + "(e.g. \"todo your_description /event your_frequency\")");
            }

            String description = parts[0];
            String frequency = parts[1];
            Task addedTask = taskList.addRecurringToList(description, frequency);
            int sizeOfList = taskList.getSize();

            return Ui.addTaskUi(addedTask, sizeOfList);
        }
        default -> {
            return Ui.unknownUi();
        }
        }
    }
}
