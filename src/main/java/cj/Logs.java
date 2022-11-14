package cj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

public class Logs {
    @Produces
    Logger produceLogger(InjectionPoint ip) {
        var ipName = ip.getMember().getDeclaringClass().getName();
        return LoggerFactory.getLogger(loggerName(ipName));
    }

    public static final String loggerName(String name) {
        var LoggerName = name.split("_")[0];
        return LoggerName;
    }

}
