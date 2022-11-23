package cj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;


@ApplicationScoped
public class Logs {
    @Produces
    @SuppressWarnings("unused")
    Logger produceLogger(InjectionPoint ip) {
        var ipName = ip.getMember().getDeclaringClass().getName();
        return LoggerFactory.getLogger(loggerName(ipName));
    }

    public static String loggerName(String name) {
        @SuppressWarnings("redundant")
        var LoggerName = name.split("_")[0];
        return LoggerName;
    }

}
