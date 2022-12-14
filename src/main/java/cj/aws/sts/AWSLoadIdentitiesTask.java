package cj.aws.sts;

import cj.BaseTask;
import cj.aws.AWSClientsManager;
import cj.aws.AWSIdentity;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Stream;

import static cj.aws.AWSOutput.Identities;

@Dependent
public class AWSLoadIdentitiesTask extends BaseTask {

    @Inject
    AWSClientsManager awsManager;

    @Override
    public void apply() {
            trace("Loading AWS identities.");
            var defaultIdentity = DefaultIdentity.of();
            awsManager.defaultIdentity(defaultIdentity);
            var ids = Stream.concat(
               Stream.of(defaultIdentity),
               loadRoles(defaultIdentity).stream())
                    .toList();
            trace("{} AWS identities loaded: {}", ids.size(), ids);
            success(Identities, ids);

    }

    private List<? extends cj.aws.AWSIdentity> loadRoles(DefaultIdentity identity) {
        var rolesCfg = configuration().raw().aws().roles();
        if (rolesCfg.isEmpty()) return List.of();
        var roles = rolesCfg.get()
                .stream()
                .map(this::roleConfigToIdentity)
                .toList();
        var canAssume = roles
                .stream()
                //TODO: Check if role can be assumed.filter(role -> canAssumeRole(identity, role))
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
            //role.assumeRole(sts);
            return true;
        }catch (Exception e){
            warn("Failed to assume role: {}", role.roleArn());
            return false;
        }
    }

}
