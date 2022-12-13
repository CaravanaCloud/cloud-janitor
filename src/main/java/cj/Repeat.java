package cj;

import cj.repeat.RepeatNeverTask;
import cj.repeat.RepeatOnceTask;
import cj.repeat.RepeatPerIdentityTask;
import cj.repeat.RepeatPerRegionTask;
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
        var instance =  switch (repeat) {
            case never -> neverInstance;
            case once -> onceInstance;
            case each_identity -> perIdentityInstance;
            case each_identity_region -> perRegionInstance;
        };
        var object = instance.get();
        if (object instanceof Task task)
            return task;
        else throw new IllegalArgumentException("Unsupported instance type: " + object.getClass());
    }

}
