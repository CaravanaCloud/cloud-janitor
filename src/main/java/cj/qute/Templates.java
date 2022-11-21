package cj.qute;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class Templates {
    @Inject
    Engine engine;

    @Inject
    Logger log;

    public Template getTemplate(String location) {
        if (engine == null) {
            log.warn("Failed to inject template engine, trying global engine.");
            engine = GlobalQuoteEngine.engine;
            if (engine == null) {
                throw new RuntimeException("Failed to resolve template engine");
            }
        }
        if (location == null) {
            log.error("Template requested for null location");
            throw new IllegalArgumentException("Template requested for null location");
        }
        try {
            return engine.getTemplate(location);
        } catch (Exception ex) {
            log.error("Failed to get template engine for {}", location);
            throw new RuntimeException(ex);
        }
    }
}
