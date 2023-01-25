package cj.secrets.ssm;

import cj.OS;
import cj.TaskAction;
import cj.TaskDomain;
import cj.aws.AWSTask;
import cj.fs.TaskFiles;
import software.amazon.awssdk.services.resourcegroupstaggingapi.ResourceGroupsTaggingApiClient;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.*;

import javax.enterprise.context.Dependent;
import javax.inject.Named;
import java.nio.file.Path;
import java.util.List;

@TaskDomain("secrets")
@TaskAction("import")
@Named("secrets-import")
@Dependent
public class SecretsImportTask extends SecretsTask {

    @Override
    public void apply() {
        info("Importing secrets from AWS SSM Parameter Store");
        var cwd = TaskFiles.cwd();
        var username = OS.username();
        var scope = Path.of(cwd).toFile().getName();
        getParameters(username, scope);
        debug("Completed import of [{}] secrets to user [{}] scope [{}]", 0, username, scope);
    }

    private void getParameters(String username, String scope) {
        try(var ssm=aws().ssm(); ){
            var params = getParameters(ssm, username, scope);
            var buf = new StringBuilder("\n");
            params.stream().map(this::toExportString).forEach(buf::append);
            debug("# BEGIN ENVRC USER[%s] SCOPE[%s]".formatted(username, scope));
            info(buf.toString());
            debug("# END ENVRC");
        }
    }

    private String toExportString(Parameter parameter) {
        var fullName = parameter.name();
        var name = fullName.substring(fullName.lastIndexOf('/') + 1);
        var value = parameter.value();
        var result = "export %s='%s'\n".formatted(name, value);
        return result;
    }

    private List<Parameter> getParameters(SsmClient ssm, String username, String scope) {
        return findParametersByTag(ssm, username, scope);
    }

    private List<Parameter> findParametersByTag(SsmClient ssm, String username, String scope) {
        var path = "/%s/%s".formatted(
                normalize(username),
                normalize(scope));
        info("Fetching parameters under path [{}]", path);
        var req = GetParametersByPathRequest.builder()
                .path(path)
                .build();
        var resp = ssm.getParametersByPath(req);
        var params = resp.parameters();
        debug("Found {} parameters for user {} scope {}", params.size(), username, scope);
        return params;
    }

}
