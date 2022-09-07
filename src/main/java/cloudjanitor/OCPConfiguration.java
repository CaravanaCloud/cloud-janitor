package cloudjanitor;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.Optional;

@ConfigMapping
@StaticInitSafe
public interface OCPConfiguration {
    @WithName("baseDomain")
    Optional<String> baseDomain();
}
