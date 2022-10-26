package cj.aws.sts;

import cj.Input;
import cj.aws.AWSIdentity;
import cj.aws.AWSTask;
import cj.aws.DefaultAWSIdentity;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static cj.Output.AWS.Identities;
import static cj.Output.AWS.CallerIdentity;
import static java.util.List.of;

@Dependent
public class LoadAWSIdentitiesTask extends AWSTask {
    @Inject
    Instance<GetCallerIdentityTask> callerIdTaskInstance;

    @Override
    public void apply() {
        var id = loadDefaultIdentity();
        if (! id.isPresent()){
            error("Failed to load AWS identity.");
        }
        else {
            trace("Loading AWS identities using default identity");
                var ids = Stream.concat(
                Stream.of(id.get()),
                loadRoles().stream());
                var result = ids
                        .filter(AWSIdentity::hasCallerIdentity)
                        .toList();
                logger().debug("{} aws identities loaded: {}", result.size(), result);
                success(Identities, result);
        }
    }

    private AWSIdentity withCallerIdentity(AWSIdentity awsIdentity) {
        var task = (GetCallerIdentityTask) submit(
                callerIdTaskInstance
                        .get()
                        .withInput(Input.AWS.identity, awsIdentity));
        var callerId = task.outputAs(CallerIdentity, cj.aws.sts.CallerIdentity.class);
        awsIdentity.withCallerIdentity(callerId);
        return awsIdentity;
    }


    private List<? extends AWSIdentity> loadRoles() {
        var roles = aws().config().roles();
        if(roles.isPresent()){
            var sts = aws().sts();
            var workingRoles = roles.get().stream()
                    .map(r -> RoleIdentity.of(r,sts))
                    .filter(RoleIdentity::canAssume)
                    .map(this::withCallerIdentity)
                    .toList();
            return workingRoles;
        }
        return List.of();
    }


    private Optional<AWSIdentity> loadDefaultIdentity() {
        var id = DefaultAWSIdentity.of();
        id = withCallerIdentity(id);
        if (id.hasCallerIdentity()){
            setIdentity(id);
            return Optional.of(id);
        }
        return Optional.empty();
    }
}
