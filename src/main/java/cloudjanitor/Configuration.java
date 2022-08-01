package cloudjanitor;

import cloudjanitor.aws.AWSConfiguration;
import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import cloudjanitor.aws.AWSClients;
import cloudjanitor.spi.Task;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ConfigMapping(prefix = "cj")
@StaticInitSafe
public interface Configuration {

    @WithDefault("marvin")
    @WithName("task")
    String taskName();

    @WithName("dryRun")
    @WithDefault("true")
    boolean dryRun();

    @WithName("waitBeforeRun")
    @WithDefault("1000")
    long waitBeforeRun();
    @WithName("inputs")
    Map<String, String> inputs();

    @WithName("aws")
    AWSConfiguration aws();


    default Path getApplicationPath(){
        var home = System.getProperty("user.home");
        var homePath = Path.of(home);
        var appPath = homePath.resolve(".cj");
        var appDir = appPath.toFile();
        if (! appDir.exists()){
            appDir.mkdirs();
        }
        return appPath;
    }

}
