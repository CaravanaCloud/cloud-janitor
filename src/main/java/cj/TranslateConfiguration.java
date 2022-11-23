package cj;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;

import java.util.Map;

@ConfigMapping
@StaticInitSafe
public interface TranslateConfiguration {
    Map<String, String> languages();
}
