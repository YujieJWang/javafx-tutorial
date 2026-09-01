import java.io.IOException;

import johnny.JohnnyException;
import johnny.command.Command;
import johnny.command.Parser;
import johnny.storage.Storage;
import johnny.task.Task;
import johnny.task.TaskList;

public class Duke {

    private final Storage storage;
    private TaskList tasks;
    private Command lastCommand;

    public Duke() {
        storage = new Storage("./data/johnny.txt");
        try {
            tasks = new TaskList(storage.load());
        } catch (Exception e) {
            tasks = new TaskList();
        }
    }

    private void saveTasks() {
        try {
            storage.save(tasks);
        } catch (IOException e) {
            // silently fail in GUI
        }
    }

    public String getResponse(String input) {
        try {
            Command command = Parser.parseCommand(input);
            String arguments = Parser.parseArguments(input);
            lastCommand = command;

            switch (command) {
            case BYE:
                return "Bye bye! See you again soon.";
            case LIST:
                return formatTaskList();
            case MARK: {
                int idx = Parser.parseTaskIndex(arguments, tasks.size());
                tasks.get(idx).markAsDone();
                saveTasks();
                return "Nice! I've marked this task as done:\n  " + tasks.get(idx);
            }
            case UNMARK: {
                int idx = Parser.parseTaskIndex(arguments, tasks.size());
                tasks.get(idx).markAsNotDone();
                saveTasks();
                return "OK, I've marked this task as not done yet:\n  " + tasks.get(idx);
            }
            case DELETE: {
                int idx = Parser.parseTaskIndex(arguments, tasks.size());
                Task removed = tasks.delete(idx);
                saveTasks();
                return "Noted. I've removed this task:\n  " + removed
                        + "\nNow you have " + tasks.size() + " tasks in the list.";
            }
            case TODO: {
                tasks.add(Parser.parseTodo(arguments));
                saveTasks();
                return taskAddedMessage();
            }
            case DEADLINE: {
                tasks.add(Parser.parseDeadline(arguments));
                saveTasks();
                return taskAddedMessage();
            }
            case EVENT: {
                tasks.add(Parser.parseEvent(arguments));
                saveTasks();
                return taskAddedMessage();
            }
            default:
                throw new JohnnyException("I'm sorry, but I'm not too sure what that means :(");
            }
        } catch (JohnnyException e) {
            lastCommand = Command.UNKNOWN;
            return "OOPS!!! " + e.getMessage();
        }
    }

    public Command getCommandType() {
        return lastCommand;
    }

    private String taskAddedMessage() {
        Task last = tasks.get(tasks.size() - 1);
        return "Got it. I've added this task:\n  " + last
                + "\nNow you have " + tasks.size() + " tasks in the list.";
    }

    private String formatTaskList() {
        if (tasks.size() == 0) {
            return "Your task list is empty.";
        }
        StringBuilder sb = new StringBuilder("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            sb.append("\n").append(i + 1).append(".").append(tasks.get(i));
        }
        return sb.toString();
    }
}
