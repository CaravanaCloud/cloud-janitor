package cj;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;

@ConfigMapping
@StaticInitSafe
public interface TemplateConfig {
    String template();
    String output();
}
