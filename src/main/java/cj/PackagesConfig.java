package cj;


import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;

import java.util.List;
import java.util.Optional;

@ConfigMapping
@StaticInitSafe
public interface PackagesConfig {
    Optional<String> darwin_amd64();
    Optional<String> darwin_arm64();
    Optional<String> linux_amd64();
    Optional<String> linux_arm64();
    Optional<String> linux_arm7();
    Optional<String> windows_amd64();

    List<ExecutableConfig> executables();

    default Optional<String> byOSandArch(){
        var os = OS.of();
        var arch = Arch.of();
        var pkg = switch (os) {
            case linux -> switch (arch) {
                case amd64 -> linux_amd64();
                case arm -> linux_arm64();
                case arm7 -> linux_arm7();
                default -> Optional.empty();
            };
            case mac -> switch (arch) {
                case amd64 -> darwin_amd64();
                case arm -> darwin_arm64();
                default -> Optional.empty();
            };
            case windows -> switch (arch) {
                case amd64 -> windows_amd64();
                default -> Optional.empty();
            };
            default -> Optional.empty();
        };
        return (Optional<String>) pkg;
    }


}
