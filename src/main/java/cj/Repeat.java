package cj;

import cj.aws.repeat.RepeatPerIdentityTask;
import cj.aws.repeat.RepeatPerRegionTask;
import cj.spi.Task;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import java.util.List;

import static cj.TaskRepeat.once;

@ApplicationScoped
public class Repeat {
    @Inject
    Configuration config;

    public Task forQuery(String[] query) {
        var taskConfig = config.taskConfigForQuery(query);
        var repeat = taskConfig
                .flatMap(TaskConfiguration::repeat)
                .orElse(once);
        var queryList = List.of(query);
        var repeater = taskForRepeater(repeat)
                .withInput(CJInput.query, queryList);
        return repeater;
    }

    @Inject
    Instance<RepeatOnceTask> onceInstance;

    @Inject
    Instance<RepeatNeverTask> neverInstance;

    @Inject
    Instance<RepeatPerIdentityTask> perIdentityInstance;

    @Inject
    Instance<RepeatPerRegionTask> perRegionInstance;


    private Task taskForRepeater(TaskRepeat repeat) {
        var task =  instanceFor(repeat).get();
        return task;
    }

    private Instance<? extends Task> instanceFor(TaskRepeat repeat) {
        return switch (repeat) {
            case never -> neverInstance;
            case once -> onceInstance;
            case each_identity -> perIdentityInstance;
            case each_identity_region -> perRegionInstance;
        };
    }

}
