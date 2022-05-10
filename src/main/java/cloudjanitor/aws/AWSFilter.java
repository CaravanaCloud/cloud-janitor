package cloudjanitor.aws;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.regions.Region;
import cloudjanitor.Configuration;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public abstract class AWSFilter extends AWSTask {
    @ConfigProperty(name = "cj.aws.filter.prefix")
    protected Optional<String> awsFilterPrefix;

    @Override
    public boolean isWrite() {
        return false;
    }

    protected boolean matchName(String name){
        if (awsFilterPrefix.isEmpty()) return true;
        if (name == null) return false;
        return name.startsWith(awsFilterPrefix.get());
    }


    protected boolean hasFilterPrefix() {
        return !awsFilterPrefix.isEmpty();
    }

}
