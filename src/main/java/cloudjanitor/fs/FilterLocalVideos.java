package cloudjanitor.fs;

import cloudjanitor.BaseTask;

import javax.enterprise.context.Dependent;

import static cloudjanitor.Output.Local.FilesMatch;
@Dependent
public class FilterLocalVideos extends BaseTask {

    @Override
    public void apply() {
        var files = FSUtils.filterLocalVideos();
        success(FilesMatch, files);
    }


}
