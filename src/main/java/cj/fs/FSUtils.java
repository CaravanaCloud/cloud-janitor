package cj.fs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class FSUtils {
    private static final Logger log = LoggerFactory.getLogger(FSUtils.class);

    public static Path getLocalConfigDir() {
        return getApplicationDir().resolve("config");
    }

    public static String cwd() {
        return System.getProperty("user.dir");
    }

    public static void writeFile(Path path, String content) {
        try {
            Files.writeString(path, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isEmptyDir(Path path) {
        if (Files.isDirectory(path)) {
            try (Stream<Path> entries = Files.list(path)) {
                return entries.findFirst().isEmpty();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    public static Path getLookupPath() {
        return getCurrentDir();
    }

    public static String basename(Path path) {
        var name = path.getFileName();
        var names = name.toString().split("\\.");
        @SuppressWarnings("UnnecessaryLocalVariable")
        var base = names[0];
        return base;
    }

    public static void writeEnv(String varName, Path varValue) {
        var envFile = getCurrentDir().resolve(".env");
        if (! envFile.toFile().exists()) {
            try {
                Files.createFile(envFile);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        var content = "%s=%s".formatted(varName, varValue);
        try {
            Files.write(envFile,
                    content.getBytes(),
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    static class FilterVisitor extends SimpleFileVisitor<Path> {
        List<Path> results = new ArrayList<>();
        String extension;

        public FilterVisitor(String extension) {
            this.extension = extension;
        }

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs){
            log.trace("visiting file: {}", path);
            if (filterMatch(path)){
                results.add(path);
            }
            return FileVisitResult.CONTINUE;
        }

        private boolean filterMatch(Path path) {
            var match=true;
            if (extension != null){
                match = path.toFile().getName().endsWith(extension);
            }
            log.trace("Path {} match? {}", path, match);
            return match;
        }

        public List<Path> getResults() {
            return results;
        }
    }

    public static List<Path> findByExtension(Path dir, String extension) {
        if(dir.toFile().exists()){
            log.debug("Looking for files on {}", dir);
            var visitor = new FilterVisitor(extension);
            try{
                Files.walkFileTree(dir, visitor);
            }catch (IOException e){
                log.error(e.getMessage(), e);
            }
            @SuppressWarnings("VariableTypeCanBeExplicit")
            var results = visitor.getResults();
            return  results;
        }else{
            log.debug("Directory does not exist, visitor not started. {}", dir);
            return List.of();
        }
    }

    public static List<Path> filterLocalVideos(){
        var extension = "mp4";
        var currentDirMatch = findByExtension(getCurrentDir(), extension);
        return Stream.of(currentDirMatch)
                .flatMap(Collection::stream)
                .toList();
    }

    private static Path getCurrentDir() {
        Path userPath = Paths.get(System.getProperty("user.dir"));
        return userPath;
    }

    public static Path getApplicationDir() {
        Path homePath = getHomePath();
        var configPath = homePath.resolve(".config");
        var appPath = resolve(configPath, "cloud-janitor");
        return appPath;
    }

    public static Path getTaskDir(String context, String dirName){
        return resolve(getContextPath(context), dirName);
    }

    public static Path getContextPath(String context){
        return resolve(getDataDir(), context);
    }

    public static Path getDataDir(){
        return resolve(getApplicationDir(), "data");
    }

    public static Path resolve(Path parent, String target) {
        if (parent == null || target == null) {
            throw new IllegalArgumentException("Parent and target must not be null");
        }
        var dataPath = parent.resolve(target);
        var dataDir = dataPath.toFile();
        if (! dataDir.exists()){
            dataDir.mkdirs();
        }
        return dataPath;
    }

    public static Path getVideosDir() {
        Path homePath = getHomePath();
        var path = homePath.resolve("Videos");
        return path;
    }


    private static Path getHomePath() {
        var home = System.getProperty("user.home");
        var homePath = Path.of(home);
        return homePath;
    }
}
