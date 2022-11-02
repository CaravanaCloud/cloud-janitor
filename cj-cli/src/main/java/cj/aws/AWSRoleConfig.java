package cj.aws;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.Optional;

@ConfigMapping
public interface AWSRoleConfig {
    @WithName("arn")
    String arn();

    @WithName("alias")
    Optional<String> alias();

}
