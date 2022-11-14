package cj;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class InMemoryConfigSource implements ConfigSource {
    public static final Map<String, String> configuration = new HashMap<>();

    @Override
    public String getValue(final String propertyName) {
        if(propertyName.equals("cj.dryRun")){
            System.out.println("");
        }
        return configuration.get(propertyName);
    }

    @Override
    public String getName() {
        return CommandJanitor.class.getSimpleName();
    }

    @Override
    public Set<String> getPropertyNames() {
        return configuration.keySet();
    }

    @Override
    public int getOrdinal() {
        return 5;
    }
}
