package cj;

public abstract class ReadTask extends BaseTask {

    @Override
    public boolean isWrite(){
        return false;
    }
}
