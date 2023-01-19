package cj.secrets.ssm;

import cj.CJInput;
import cj.aws.AWSTask;
import software.amazon.awssdk.services.ssm.SsmClient;

import javax.enterprise.context.Dependent;
import java.util.Map;

@Dependent
public class PutParametersTask extends AWSTask {
    @Override
    public void apply() {
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
        info("Putting parameter: {} / {} = {}", key, paramKey, value);
    }

    private String paramKeyName(String key) {
        return key;
    }

}
