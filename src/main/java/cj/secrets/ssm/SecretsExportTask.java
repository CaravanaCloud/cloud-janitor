package cj.secrets.ssm;

import cj.CJInput;
import cj.OS;
import cj.TaskAction;
import cj.TaskDomain;
import cj.aws.AWSTask;
import cj.fs.TaskFiles;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@TaskDomain("secrets")
@TaskAction("export")
@Dependent
public class SecretsExportTask extends AWSTask {

    @Override
    public void apply() {
        info("Exporting secrets to AWS SSM Parameter Store");
        var cwd = TaskFiles.cwd();
        var username = OS.username();
        var scope = cwd;
        var envrc = TaskFiles.loadEnvrc();
    }





    public void applyOld() {
        info("Putting parameters");
        var region = region();
        var params =  inputAs(CJInput.properties, Map.class)
                .map(m -> (Map<String, String>) m);
        params.ifPresent(this::putParameters);
    }

    private void putParameters(Map<String, String> parameters) {
        info("Putting parameters: {}", parameters);
        try(var ssm= aws().ssm()) {
            parameters.forEach((k, v) -> {
                putParameter(ssm, k, v);
            });
        }
    }

    private void putParameter(SsmClient ssm, String key, String value) {
        var paramKey = paramKeyName(key);
        info("Putting secure string: {} := [{}]",  paramKey, value.length());
        var req = PutParameterRequest.builder()
                .name(paramKey)
                .value(value)
                .type(ParameterType.SECURE_STRING)
                .build();
        var resp = ssm.putParameter(req);
        debug("Put parameter response: {}", resp);
    }

    private String paramKeyName(String key) {
        var username = OS.username();
        var scope = scope();
        var separator = separator();
        var fqkn = username + separator + scope + separator + key;
        fqkn = fqkn.toUpperCase();
        return fqkn;
    }

    private String separator() {
        return "__";
    }

    private String scope() {
        return inputString(CJInput.scope)
                .orElse("default");
    }

}
