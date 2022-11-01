package cj.aws.transx;

import cj.SafeTask;
import cj.fs.GlobFilesTask;
import cj.spi.Task;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.util.List;

import static cj.Output.fs.paths;

@Dependent
@Named("aws-translate")
public class TranslateFilesTask extends SafeTask {
    @Inject
    GlobFilesTask globFiles;

    @Override
    public List<Task> getDependencies() {
        return List.of(globFiles);
    }

    @Override
    public void apply() {
        var files = globFiles.outputList(paths, Path.class);
        debug("Traslating {} files", files.size());
    }
}
