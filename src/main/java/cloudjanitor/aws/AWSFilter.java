package cloudjanitor.aws;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.regions.Region;
import cloudjanitor.Configuration;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public abstract class AWSFilter extends AWSTask {

    @Override
    public boolean isWrite() {
        return false;
    }

    protected boolean matchName(String name){
        var prefix = aws().config().filterPrefix();
        if (prefix.isEmpty()) return true;
        if (name == null) return false;
        return name.startsWith(prefix.get());
    }


    protected boolean hasFilterPrefix() {
        return aws().config().filterPrefix().isPresent();
    }

}
