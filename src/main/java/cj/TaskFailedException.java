package cj;

public class TaskFailedException extends RuntimeException {
    public TaskFailedException(String message) {
        super(message);
    }
}
