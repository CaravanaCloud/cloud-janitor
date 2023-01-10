package cj.aws.cr;

import javax.inject.Named;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ConfigEvent;
import software.amazon.awssdk.services.config.ConfigClient;
import software.amazon.awssdk.services.config.model.ComplianceType;
import software.amazon.awssdk.services.config.model.Evaluation;
import software.amazon.awssdk.services.config.model.PutEvaluationsRequest;

@Named("LookupAttributionTagsLambda")
public class LookupAttributionTagsLambda
        implements RequestHandler<ConfigEvent, Void> {
    @Override
    public Void handleRequest(ConfigEvent event, Context context) {
        var log = context.getLogger();
        log.log("== Looking up attribution tags for resource. ==");
        log.log("Event class: "+ event.getClass().getName());
        log.log("Invoking event: " + event.getInvokingEvent());
        log.log("Event data: ");
        log.log(event.toString());
/*
        var evaluation = Evaluation.builder()
                .complianceType(ComplianceType.COMPLIANT)
                .complianceResourceId(event.getInvokingEvent())
                .build();
        try (var config = configClient()){
            doPutEvaluations(config, event, evaluation);
        }

 */
        return null;
    }

    private void doPutEvaluations(ConfigClient config, ConfigEvent event, Evaluation evaluation) {
        var req = PutEvaluationsRequest.builder()
                .evaluations(evaluation)
                .resultToken(event.getResultToken())
                .build();
        var resp = config.putEvaluations(req);
        // Ends the function execution if any evaluation results are not successfully reported.
        if (resp.failedEvaluations().size() > 0) {
            throw new RuntimeException(String.format(
                    "The following evaluations were not successfully reported to AWS Config: %s",
                    resp.failedEvaluations()));
        }
    }

    private ConfigClient configClient() {
        var config = ConfigClient.builder()
                .build();
        return config;
    }


}
