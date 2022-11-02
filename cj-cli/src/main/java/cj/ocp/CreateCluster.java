package cj.ocp;

import cj.BaseTask;
import cj.shell.ShellTask;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import static cj.Input.shell.*;

@Dependent
@Named("ocp-create-cluster")
public class CreateCluster extends BaseTask {

    @Inject
    ShellTask shellTask;

    @Override
    public void apply() {
        debug("ocp-create-cluster");
        submit(shellTask.withInput(cmd, "ls -liah ."));
        //var result = shellTask.getOutputString(Output.shell.sdtout);
        debug("");
    }
}
