package cj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public enum Options {
    level(s -> setProperty("quarkus.log.level", s)),
    capabilities(s -> setProperty("cj.capabilities", s)),
    version(s -> setProperty("cj.showVersion", "true")),
    help(s -> setProperty("cj.showHelp", "true")),;
    private static final Logger log = LoggerFactory.getLogger(Options.class);
    private final Consumer<String> parser;

    Options(Consumer<String> parser) {
        this.parser = parser;
    }

    public static Options of(String s) {
        return valueOf(s.toLowerCase());
    }

    public void parse(String value) {
        log.debug("Parsing option {} with value {}", this, value);
        parser.accept(value);
    }

    private static void setProperty(String key, String value) {
        System.setProperty(key, value);
    }


}
