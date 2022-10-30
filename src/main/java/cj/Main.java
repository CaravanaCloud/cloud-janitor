package cj;

import cj.fs.FSUtils;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;

@QuarkusMain
public class Main {

    public static void main(String[] args) {
        loadLocalQuarkusConfig();
        Quarkus.run(CommandJanitor.class, args);
    }

    private static void loadLocalQuarkusConfig() {
        var localConfigDir = FSUtils.getLocalConfigDir();
        var localConfigFile = localConfigDir.resolve("application.yaml");
        if (localConfigFile.toFile().exists()){
            var configLocation = localConfigDir.toAbsolutePath().toString();
            //System.out.println("Local configuration file found at " + configLocation);
            System.setProperty("quarkus.config.locations", configLocation);
        }else {
            //System.err.println("Local configuration file not found.");
        }
    }
}
