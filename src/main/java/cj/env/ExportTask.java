package cj.env;

import cj.BaseTask;
import cj.CJInput;
import cj.secrets.ssm.SecretsExportTask;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Dependent
@Named("export")
public class ExportTask extends BaseTask {
    @Inject
    SecretsExportTask putParametersTask;

    @Override
    public void apply() {
        info("Exporting environment");
        var properties = inputProperties();
        submit(putParametersTask
                .withInput(CJInput.properties, properties));
        info("Done exporting environment");
    }

    private Map<String, String> inputProperties() {
        var query = inputList(CJInput.query, String.class);
        if (query.size() <= 1) return Map.of();
        var props = new HashMap<String, String>();
        for (int i = 1; i < query.size(); i++) {
            var prop = query.get(i);
            var parts = prop.split("=");
            if (parts.length != 2) {
                warn("Invalid variable: " + prop);
            }
            props.put(parts[0], parts[1]);
        }
        return props;
    }
}
