package cj;

public interface Input {
    enum ocp implements Input {
        clusterName
    }
    enum cj implements Input {
        task,
        tasks,
        fixTask,
        dryRun
    }

    enum shell implements Input {
        cmd,
        cmds
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
