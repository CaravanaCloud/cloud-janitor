package cj.secrets.ssm;

import cj.CJInput;
import cj.OS;
import cj.TaskAction;
import cj.TaskDomain;
import cj.aws.AWSTask;
import cj.fs.TaskFiles;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.*;

import javax.enterprise.context.Dependent;
import javax.inject.Named;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@TaskDomain("secrets")
@TaskAction("export")
@Named("secrets-export")
@Dependent
public class SecretsExportTask extends SecretsTask {

    @Override
    public void apply() {
        info("Exporting secrets to AWS SSM Parameter Store");
        var cwd = TaskFiles.cwd();
        var username = OS.username();
        var scope = Path.of(cwd).toFile().getName();
        var envrc = TaskFiles.loadEnvrc();
        putParameters(envrc, username, scope);
        debug("Completed export of [{}] secrets to user [{}] scope [{}]", envrc.size(), username, scope);
    }

    private void putParameters(Map<String, String> parameters, String username, String scope) {
        info("Putting parameters: {}", parameters);
        try(var ssm= aws().ssm()) {
            parameters.forEach((k, v) -> {
                putParameter(ssm, k, v, username, scope);
            });
        }
    }

    private void putParameter(SsmClient ssm, String key, String value, String username, String scope) {
        if (key == null || key.isBlank()) {
            warn("Skipping empty key");
            return;
        }
        if (value == null || value.isBlank()) {
            warn("Skipping empty value for key [{}]", key);
            return;
        }
        var paramKey = paramKeyName(username, scope, key);
        info("Putting secure string: {} := [{}]",  paramKey, value.length());
        var usernameTag = Tag.builder().key(usernameTagName()).value(username).build();
        var scopeTag = Tag.builder().key(scopeTagName()).value(scope).build();
        var nameTag = Tag.builder().key(nameTagName()).value(key).build();
        var tags = List.of(nameTag, usernameTag, scopeTag);
        var req = PutParameterRequest.builder()
                .name(paramKey)
                .value(value)
                .type(ParameterType.STRING)
                .overwrite(true)
                .build();
        var resp = ssm.putParameter(req);
        debug("Put parameter response: {}", resp);
        var treq = AddTagsToResourceRequest.builder()
                .tags(tags)
                .resourceType(ResourceTypeForTagging.PARAMETER)
                .resourceId(paramKey)
                .build();
        var tres = ssm.addTagsToResource(treq);
        debug("Put parameter tags ok");
    }


}
