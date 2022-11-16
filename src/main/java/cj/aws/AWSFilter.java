package cj.aws;

public abstract class AWSFilter extends AWSTask {

    protected boolean matchName(String name){
        var prefix = aws().config().filterPrefix();
        if (prefix.isEmpty()) return true;
        if (name == null) return false;
        return name.startsWith(prefix.get());
    }


    protected boolean hasFilterPrefix() {
        return aws().config().filterPrefix().isPresent();
    }

}
