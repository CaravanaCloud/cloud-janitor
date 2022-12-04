package cj;

public interface Output {
    enum shell implements Output {
        exitCode, stderr, stdout

    }

    enum fs implements Output {
        paths
    }

    enum local implements Output {
        FilesMatch

    }

    enum sample implements Output{
        Message, UpperMessage
    }


}
