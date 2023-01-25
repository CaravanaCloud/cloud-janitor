package cj.secrets.ssm;

import cj.aws.AWSTask;

public abstract class SecretsTask extends AWSTask  {

    protected String normalize(String value) {
        var result = value.replaceAll("\\W", "_").toUpperCase();
        return result;
    }

    protected String paramKeyName(String username, String scope, String key) {
        var separator = separator();
        var fqkn = separator
                + normalize(username)
                + separator
                + normalize(scope)
                + separator
                + normalize(key);
        return fqkn;
    }

    protected String separator() {
        return "/";
    }
    protected String nameTagName() {
        return "cj-secrets-name";
    }

    protected String scopeTagName() {
        return "cj-secrets-scope";
    }

    protected String usernameTagName() {
        return "cj-secrets-username";
    }

}
