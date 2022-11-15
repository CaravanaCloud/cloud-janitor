package cj;

import java.util.Optional;

public interface Input {
    enum ocp implements Input {
        clusterName,

        clusterProfile;
    }
    enum cj implements Input {
        task,
        tasks,
        fixTask,
        dryRun
    }

    enum shell implements Input {
        cmd,
        cmds,
        timeout;
        public static final Long DEFAULT_TIMEOUT_MINS = 5L;
    }


    enum fs implements Input {
        glob,
        globPath,
        paths
    }
    enum local implements Input {
        message, fileExtension

    }

    enum aws implements Input {
        targetVPCId,
        vpcCIDR,
        routeTable,
        targetLoadBalancerArn,
        targetLoadBalancerName,
        targetNatGatewayId,
        targetNetworkInterfaceId,
        targetNetworkInterface,
        resourceRecordSet,
        targetRouteTable,
        targetTargetGroup,
        targetVPCEndpoint,
        address,
        targetBucketName,
        targetRegion,
        identity,
        targetInstanceId,
        awsClients,
        targetAddress,
        s3Prefix,
        targetLanguages,
        sourceLanguage,
        contentType, securityGroupRule
    }
}
