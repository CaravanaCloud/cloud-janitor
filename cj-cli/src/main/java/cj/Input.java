package cj;

public interface Input {
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
