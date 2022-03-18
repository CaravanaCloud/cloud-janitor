package tasktree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

public class Loggers {
    @Produces
    Logger produceLogger(InjectionPoint ip) {
        return LoggerFactory.getLogger(ip.getMember().getDeclaringClass().getName());
    }
}
