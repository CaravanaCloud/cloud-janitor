package cloudjanitor.aws;

import java.util.List;
import java.util.Optional;

public abstract class AWSCleanup extends AWSTask {
    @Override
    public boolean isWrite() {
        return false;
    }
}