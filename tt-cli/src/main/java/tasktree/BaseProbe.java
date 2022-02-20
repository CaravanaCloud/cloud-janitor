package tasktree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tasktree.spi.Probe;
import tasktree.spi.Probes;
import tasktree.spi.Sample;

public class BaseProbe implements Probe {
    final Logger log = LoggerFactory.getLogger(Probes.class);

    @Override
    public Sample call() {
        log.info("🐣");
        return Sample.empty();
    }
}
