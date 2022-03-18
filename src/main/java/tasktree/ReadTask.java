package tasktree;

import tasktree.BaseTask;

public abstract class ReadTask extends BaseTask {

    @Override
    public boolean isWrite(){
        return false;
    }
}
