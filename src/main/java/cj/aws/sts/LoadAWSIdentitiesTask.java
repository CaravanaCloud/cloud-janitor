package cj.aws.sts;

import cj.aws.AWSIdentity;
import cj.aws.AWSInput;
import cj.aws.AWSTask;
import cj.aws.DefaultAWSIdentity;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static cj.Output.aws.CallerIdentity;
import static cj.Output.aws.Identities;

@Dependent
public class LoadAWSIdentitiesTask extends AWSTask {
    @Inject
    Instance<GetCallerIdentityTask> callerIdTaskInstance;

    @Override
    public void apply() {
        var id = loadDefaultIdentity();
        if (id.isEmpty()){
            warn("Failed to load default AWS identity.");
        }
        else {
            trace("Loading further AWS identities using default identity");
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



    private List<? extends AWSIdentity> loadRoles() {
        var roles = aws().config().roles();
        if(roles.isPresent()){
            try(var sts = aws().sts()){
                @SuppressWarnings("redundant")
                var workingRoles = roles.get().stream()
                        .map(r -> RoleIdentity.of(r,sts))
                        .filter(RoleIdentity::canAssume)
                        .map(this::verifyIdentity)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList();
                return workingRoles;
            }
        }
        return List.of();
    }


    private Optional<AWSIdentity> loadDefaultIdentity() {
        return verifyIdentity(DefaultAWSIdentity.of());
    }

    private Optional<AWSIdentity> verifyIdentity(AWSIdentity id){
        var task = submitInstance(callerIdTaskInstance, AWSInput.identity, id);
        var callerId = task.outputAs(CallerIdentity, cj.aws.sts.CallerIdentity.class);
        if (callerId.isPresent()){
            id.setCallerIdentity(callerId.get());
            return Optional.of(id);
        }
        return Optional.empty();
    }
}
