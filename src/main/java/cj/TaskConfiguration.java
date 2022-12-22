package cj;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;

import java.util.List;
import java.util.Optional;

@ConfigMapping
@StaticInitSafe
public interface TaskConfiguration {

    String name();

    Optional<String> description();

    Optional<String> maturity();

    //TODO: Check if Optional is needed here
    Optional<List<InputConfiguration>> inputs();

    //TODO: Check if Optional is needed here
    //TODO: Consider multiple bypasses (create cluster, destroy cluster, ...)
    Optional<List<String>> bypass();

    Optional<TaskRepeat> repeat();

    Optional<InstallConfig> install();

    List<TemplateConfig> templates();
}
