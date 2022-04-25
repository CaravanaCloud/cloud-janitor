package cloudjanitor.aws;

public class AWSWrite<T> extends AWSTask<T> {
    @Override
    public boolean isWrite() {
        return true;
    }
}
