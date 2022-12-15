package cj;

import cj.BaseTask;
import cj.CJInput;
import cj.Tasks;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class RepeatNeverTask extends BaseTask {
    @Inject
    Tasks tasks;

    @Override
    public void apply() {
        var query = inputList(CJInput.query, String.class);
        debug("Skipping query: {}", query);
    }
}