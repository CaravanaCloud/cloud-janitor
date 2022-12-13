package cj.aws.sts;

import cj.BaseTask;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Stream;

import static cj.aws.AWSOutput.CallerIdentity;
import static cj.aws.AWSOutput.Identities;

@Dependent
public class AWSLoadIdentitiesTask extends BaseTask {
    @Inject
    GetDefaultAWSIdentity getDefaultIdentity;

    @Override
    public void apply() {
        var id = submit(getDefaultIdentity)
                .outputAs(CallerIdentity, SimpleIdentity.class);
        if (id.isEmpty()){
            warn("Failed to load default AWS identity.");
        }
        else {
            trace("Loading further AWS identities using default identity");
            var ids = Stream.concat(
               Stream.of(id.get()),
               loadRoles().stream()).toList();
            trace("{} aws identities loaded: {}", ids.size(), ids);
            success(Identities, ids);
        }
    }

    private List<? extends cj.aws.AWSIdentity> loadRoles() {
        //TODO: 0 Load AWS roles from configuration
        return List.of();
    }


}
