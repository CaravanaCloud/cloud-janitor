package cloudjanitor.marvin;

import org.slf4j.Logger;
import cloudjanitor.ReadTask;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

@Named("marvin")
@Dependent
public class MarvinTask extends ReadTask {
    @Inject
    Logger log;

    @Override
    public void apply() {
        log.warn("Don't Panic!");
    }
}
