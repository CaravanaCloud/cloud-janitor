package tasktree;

import tasktree.spi.Task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class Result{
    String taskName;
    ResultType type;
    String description;
    LocalDateTime startTime;
    LocalDateTime endTime;

    static final DateTimeFormatter iso = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Result(String name,
                  ResultType type,
                  String description,
                  LocalDateTime start,
                  LocalDateTime end) {
        this.taskName = name;
        this.type = type;
        this.description = description;
        this.startTime = start;
        this.endTime =end;
    }

    private static Result result(Task task,
                                 ResultType type,
                                 String description,
                                 LocalDateTime start,
                                 LocalDateTime end){
        return new Result(task.getName(),
                type,
                description,
                start,
                end);
    }
    private static Result result(Task task, ResultType type, String description){
        return result(task,
                type,
                description,
                task.getStartTime(),
                task.getEndTime());
    }

    private static Result result(Task task, ResultType type){
        return result(task, type, task.getDescription());
    }

    public static Result empty(Task task) {
        return result(task, ResultType.EMPTY);
    }


    public static Result dryRun(Task task) {
        return result(task, ResultType.DRY_RUN);
    }

    public static Result success(Task task) {
        return result(task, ResultType.SUCCESS);
    }

    public static Result failure(Task task, Exception e) {
        return result(task, ResultType.FAILURE, e.getMessage());
    }

    public static String[] getHeader() {
        return new String[]{"start",
                "task",
                "type",
                "description"};
    }

    public String startTimeISO() {
        return Optional.ofNullable(startTime)
                        .map(ldt -> iso.format(ldt))
                        .orElse("");
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public ResultType getType() {
        return type;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getDescription() {
        return description;
    }

    public Iterable<String> toRecord() {
        return List.of(
          startTimeISO(),
                taskName,
                type.toString(),
                description
        );
    }
}
