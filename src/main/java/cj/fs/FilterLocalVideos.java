package cj.fs;

import cj.BaseTask;

import javax.enterprise.context.Dependent;

import static cj.Output.Local.FilesMatch;
@Dependent
public class FilterLocalVideos extends BaseTask {

    @Override
    public void apply() {
        var files = FSUtils.filterLocalVideos();
        success(FilesMatch, files);
    }


}
