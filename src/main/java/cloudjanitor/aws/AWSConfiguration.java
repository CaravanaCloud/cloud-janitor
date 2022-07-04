package cloudjanitor.aws;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;

@ConfigMapping(prefix = "cj.aws")
@StaticInitSafe
public interface AWSConfiguration {
    @WithName("region")
    @WithDefault("us-east-1")
    String defaultRegion();

    @ConfigProperty(name = "cj.aws.filter.prefix")
    Optional<String> filterPrefix();

}
