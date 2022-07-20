package cloudjanitor;

public interface Errors {

    enum Type implements Errors {
        Message,
        Exception
    }
}
