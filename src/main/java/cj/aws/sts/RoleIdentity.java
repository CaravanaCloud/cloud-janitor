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

public class RoleIdentity
        implements AWSIdentity {
    static final Logger log = LoggerFactory.getLogger(RoleIdentity.class);
    private final AWSRoleConfig roleCfg;

    public RoleIdentity(AWSRoleConfig roleCfg){
        this.roleCfg = roleCfg;
    }

    @Override
    public AwsCredentialsProvider toCredentialsProvider(StsClient sts) {
        return StsAssumeRoleCredentialsProvider.builder()
                .stsClient(sts)
                .refreshRequest(assumeRoleRequest())
                .build();
    }

    private AssumeRoleRequest assumeRoleRequest() {
        var stamp = Utils.nowStamp();
        var sessionName = "cloud-janitor-session-"+System.currentTimeMillis();
        var req = AssumeRoleRequest.builder()
                .roleArn(roleCfg.arn())
                .roleSessionName(sessionName)
                .build();
        return req;
    }


    public static RoleIdentity of(AWSRoleConfig awsRole) {
        return new RoleIdentity(awsRole);
    }

    public String roleArn() {
        return roleCfg.arn();
    }

    public String roleName() {
        return roleCfg.alias().orElse(roleCfg.arn());
    }

    @Override
    public String toString() {
        return "RoleIdentity{" +
                "arn=" + roleCfg.arn() +
                '}';
    }
}
