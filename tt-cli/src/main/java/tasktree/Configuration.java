package tasktree;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;

@ApplicationScoped
public class Configuration {
    @Inject
    Logger log;

    @ConfigProperty(name = "tt.root", defaultValue = "help")
    String root;

    public String getRoot() {
        return root;
    }

    @Override
    public String toString() {
        return "Configuration{%s}".formatted(root);
    }

    public void parse(String[] args) {
        log.info("Args: %s".formatted(Arrays.toString(args)) );
        if (args.length > 0) {
            root = args[0];
        }
    }
}
