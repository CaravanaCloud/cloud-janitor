package cloudjanitor;

import cloudjanitor.spi.Task;

import java.util.Optional;

public enum Output {
    AWS_ACCOUNT;

    public Optional<String> findString(Task task){
        return task.findString(this.name());
    }
}
