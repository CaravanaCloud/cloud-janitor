package cj.aws.sts;

import cj.BaseTask;
import cj.Utils;
import cj.aws.AWSClientsManager;
import cj.aws.AWSIdentity;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

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

    @Inject
    AWSClientsManager awsManager;

    @Override
    public void apply() {
        var identityIn = submit(getDefaultIdentity)
                .outputAs(CallerIdentity, SimpleIdentity.class);
        if (identityIn.isEmpty()){
            warn("Failed to load default AWS identity.");
        }
        else {
            var identity = identityIn.get();
            trace("Loading further AWS identities using default identity");
            var ids = Stream.concat(
               Stream.of(identity),
               loadRoles(identity).stream()).toList();
            trace("{} aws identities loaded: {}", ids.size(), ids);
            success(Identities, ids);
        }
    }

    private List<? extends cj.aws.AWSIdentity> loadRoles(SimpleIdentity identity) {
        var rolesCfg = configuration().raw().aws().roles();
        if (rolesCfg.isEmpty()) return List.of();
        var roles = rolesCfg.get()
                .stream()
                .map(this::roleConfigToIdentity)
                .toList();
        var canAssume = roles
                .stream()
                .filter(role -> canAssumeRole(identity, role))
                .toList();
        return roles;
    }

    private RoleIdentity roleConfigToIdentity(cj.aws.AWSRoleConfig roleCfg) {
        return new RoleIdentity(roleCfg);
    }

    private boolean canAssumeRole(AWSIdentity identity, RoleIdentity role) {
        var region = awsManager.defaultRegion();
        var aws = awsManager.of(identity, region);
        try(var sts = aws.sts()){
            //TODO: Assume role on usage to avoid disconnected pool exception
            role.assumeRole(sts);
            return true;
        }catch (Exception e){
            warn("Failed to assume role: {}", role.roleArn());
            return false;
        }
    }

}
