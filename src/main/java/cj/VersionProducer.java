package cj;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class VersionProducer {
    private static String version;

    @Produces
    @Version
    public static synchronized String getVersion() {
        if (version == null){
            version = readVersionFile();
        }
        return version;
    }

    private static String readVersionFile() {
        return readFileResource("/version.txt");
    }

    private static String readFileResource(String path) {
        try(var in = VersionProducer.class
                .getResourceAsStream(path);
            var reader = new java.io.BufferedReader(new java.io.InputStreamReader(in))){
            if(reader == null) return null;
            return reader.readLine();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
