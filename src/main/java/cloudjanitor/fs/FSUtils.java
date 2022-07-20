package cloudjanitor.fs;

import cloudjanitor.Input;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FSUtils {
    private static final Logger log = LoggerFactory.getLogger(FSUtils.class);

    static class FilterVisitor extends SimpleFileVisitor<Path> {
        List<Path> results = new ArrayList<>();
        Optional<String> extension = Optional.empty();

        public FilterVisitor(Optional<String> extension) {
            this.extension = extension;
        }

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
            if (filterMatch(path)){
                results.add(path);
            }
            return FileVisitResult.CONTINUE;
        }

        private boolean filterMatch(Path path) {
            var match=true;
            if (extension.isPresent()){
                match = match && path.toFile().getName().endsWith(extension.get());
            }
            log.trace("Found path {} match? {}", path, match);
            return match;
        }

        public List<Path> getResults() {
            return results;
        }
    }

    public static List<Path> filterLocalFiles(Path dir, Optional<String> extension) {
        if(dir.toFile().exists()){
            log.debug("Starting visitor on {}", dir);
            var visitor = new FilterVisitor(extension);
            try{
                Files.walkFileTree(dir, visitor);
            }catch (IOException e){
                log.error(e.getMessage(), e);
            }
            var results = visitor.getResults();
            return  results;
        }else{
            log.debug("Directory does not exist, visitor not started. {}", dir);
            return List.of();
        }
    }

    public static List<Path> filterLocalVideos(){
        var extension = Optional.of("mp4");
        var dataDirMatch = filterLocalFiles(getDataDir(), extension);
        var videosDirMatch = filterLocalFiles(getVideosDir(), extension);
        var currentDirMatch = filterLocalFiles(getCurrentDir(), extension);
        return Stream.of(dataDirMatch, videosDirMatch, currentDirMatch)
                .flatMap(Collection::stream)
                .toList();
    }

    private static Path getCurrentDir() {
        Path userPath = Paths.get(System.getProperty("user.dir"));
        return userPath;
    }

    public static Path getDataDir() {
        Path homePath = getHomePath();
        var dataPath = homePath.resolve(".cj");
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
