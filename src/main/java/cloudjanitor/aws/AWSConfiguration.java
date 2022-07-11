package cloudjanitor.aws;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Optional;

@ConfigMapping(prefix = "cj.aws")
@StaticInitSafe
public interface AWSConfiguration {
    default String defaultRegion(){
        var regions = regions();
        if (regions.isPresent() &&
                !regions().get().isEmpty()){
            return regions.get().get(0);
        }
        return "us-east-1";
    }

    @WithName("filter.prefix")
    Optional<String> filterPrefix();

    @WithName("regions")
    Optional<List<String>> regions();
}
