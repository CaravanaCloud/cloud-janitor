package tasktree.spi;

import org.slf4j.Logger;
import tasktree.Configuration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

@ApplicationScoped
public class Probes {
    static final ServiceLoader<Probe> loader = ServiceLoader.load(Probe.class);

    public static Stream<Probe> byProviders() {
        return byProviders(true).stream();
    }

    public static List<Probe> byProviders(boolean refresh) {
        if (refresh) {
            loader.reload();
        }
        var result = new ArrayList<Probe>();
        loader.iterator().forEachRemaining(result::add);
        return result;
    }

    @Inject
    @Any
    Instance<Probe> probes;

    @Inject
    Logger log;

    @Inject
    Configuration config;

    public Stream<Probe> stream() {
        return Stream.concat(byProviders(), byCDI());
    }

    private Stream<Probe> byCDI() {
        return probes.stream();
    }

    public void run(String[] args) {
        config.parse(args);
        log.info(config.toString());
        stream().filter(this::filter)
                .forEach(this::call);
    }

    private boolean filter(Probe p) {
        return p.filter(config.getRoot());
    }

    private void call(Probe probe) {
        try {
            var sample = probe.call();
            log.info(probe.getName() + " -> " + sample);
        } catch (Exception e) {
            log.error("🐓 Error", e);
        }
    }
}
