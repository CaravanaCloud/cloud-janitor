package cj;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping
@StaticInitSafe
public interface ReportConfiguration {
    @WithName("enabled")
    @WithDefault("false")
    boolean enabled();
}
