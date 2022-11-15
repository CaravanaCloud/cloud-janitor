package cj.fs;

import cj.Output;
import cj.SafeTask;

import javax.enterprise.context.Dependent;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static cj.fs.FSInput.*;
import static cj.fs.FSUtils.cwd;

/**
 *
 * @see java.nio.file.FileSystem#getPathMatcher(String) getPathMatcher
 */
@Dependent
@Named("glob-files")
public class GlobFilesTask extends SafeTask {
    @Override
    public void apply() {
        var inGlob = expectInputString(glob);
        var inPath = inputString(globPath).orElse(cwd());
        try {
            var paths = match(inGlob, inPath);
            success(Output.fs.paths, paths);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Path> match(String globInput, String location) throws IOException {
        debug("Walking file tree [{}] [{}]", globInput, location);
        var visitor = new GlobVisitor(globInput, location);
        Files.walkFileTree(Paths.get(location), visitor);
        return visitor.result;
    }

    class GlobVisitor extends SimpleFileVisitor<Path>{
        private final String globIn;
        private final String pathIn;
        final PathMatcher pathMatcher;
        private final List<Path> result = new ArrayList<>();
        public GlobVisitor(String globIn,
                           String pathIn){
            this.globIn = "glob:" + globIn;
            this.pathIn = pathIn;

            this.pathMatcher = globIn != null ?
                    FileSystems.getDefault().getPathMatcher(this.globIn) :
                    null;
        }


        @Override
        public FileVisitResult visitFile(Path path,
                BasicFileAttributes attrs) throws IOException {
            if (pathMatcher != null &&
                    pathMatcher.matches(path)) {
                debug("+ " + path.toAbsolutePath());
                result.add(path);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc)
                    throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
}