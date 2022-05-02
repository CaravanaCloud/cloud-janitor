package cloudjanitor.aws;

public class AWSWrite extends AWSTask {
    @Override
    public boolean isWrite() {
        return true;
    }
}
