package cj.aws.sts;

import cj.Utils;
import cj.aws.AWSIdentity;
import cj.aws.AWSRoleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;

public class RoleIdentity
        extends AWSIdentity {
    static final Logger log = LoggerFactory.getLogger(RoleIdentity.class);
    static final String ROLE_SESSION_REGEX = "[^a-zA-Z0-9\\w+=,.@-]";
    private final AWSRoleConfig roleCfg;
    private final StsClient sts;

    private static final String roleSessionName(String str) {
        return str.replaceAll(ROLE_SESSION_REGEX, "-");
    }

    public RoleIdentity(AWSRoleConfig roleCfg, StsClient sts){
        this.roleCfg = roleCfg;
        this.sts = sts;
    }

    public AssumeRoleResponse assumeRole() {
        var req = assumeRoleRequest();
        return sts.assumeRole(req);
    }

    private AssumeRoleRequest assumeRoleRequest() {
        var stamp = Utils.nowStamp();
        var sessionName = roleSessionName(roleCfg.alias() + stamp);
        var req = AssumeRoleRequest.builder()
                .roleArn(roleCfg.arn())
                .roleSessionName(sessionName)
                .build();
        return req;
    }

    public boolean canAssume() {
        try {
            assumeRole();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public AwsCredentialsProvider toCredentialsProvider() {
        return StsAssumeRoleCredentialsProvider.builder()
                .stsClient(sts)
                .refreshRequest(assumeRoleRequest())
                .build();
    }

    public static RoleIdentity of(AWSRoleConfig awsRole, StsClient sts) {
        return new RoleIdentity(awsRole, sts);
    }
}
