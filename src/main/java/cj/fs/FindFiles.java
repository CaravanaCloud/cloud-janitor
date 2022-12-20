package cj.fs;

import cj.BaseTask;

import javax.enterprise.context.Dependent;

import static cj.Output.local.FilesMatch;
@Dependent
public class FindFiles extends BaseTask {

    @Override
    public void apply() {
        var path = TaskFiles.getLookupPath();
        inputString(FSInput.extension).ifPresent( extension -> {
            var files = TaskFiles.findByExtension(path, extension);
            success(FilesMatch, files);
        });
    }


}
