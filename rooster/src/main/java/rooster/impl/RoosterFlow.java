package rooster.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rooster.spi.Flow;

public class RoosterFlow implements Flow {
    private static Logger log = LoggerFactory.getLogger(RoosterFlow.class);

    @Override
    public void run() {
        log.info("🐣");
    }
    
}
