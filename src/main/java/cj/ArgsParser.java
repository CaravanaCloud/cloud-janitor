package cj;

import cj.fs.TaskFiles;

import java.util.Optional;
import java.util.stream.Stream;

public class ArgsParser {
    static ArgsParser instance;

    private ArgsParser(){
    }

    public static synchronized  ArgsParser of() {
        if (instance == null) {
            instance = new ArgsParser();
        }
        return instance;
    }

    public String[] parse(String... args) {
        var parsed = Stream.of(args).map(this::parseToken)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toArray(String[]::new);
        return parsed;
    }

    private Optional<String> parseToken(String s) {
        if (s == null) return Optional.empty();
        if (s.startsWith("-cj:")){
            var tokens = s.split(":");
            var flag = tokens[1];
            var kv = flag.split("=");
            try {
                var option = Options.of(kv[0]);
                var value = kv.length > 1 ? kv[1]:null;
                option.parse(value);
                return Optional.empty();
            }catch (IllegalArgumentException e){
                System.out.println("Unknown option: "+ flag);
            }
        }
        return Optional.of(s);
    }

    private static void loadLocalQuarkusConfig() {
        var localConfigDir = TaskFiles.getLocalConfigDir();
        var localConfigFile = localConfigDir.resolve("application.yaml");
        if (localConfigFile.toFile().exists()) {
            var configLocation = localConfigDir.toAbsolutePath().toString();
            // System.out.println("Local configuration file found at " + configLocation);
            System.setProperty("quarkus.config.locations", configLocation);
        }
    }

}
