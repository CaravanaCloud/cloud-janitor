package cloudjanitor.aws.model;

import software.amazon.awssdk.services.ec2.model.IpPermission;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;

public record IpPermissionModel(
        SecurityGroup securityGroup,
        IpPermission ipPerm,
        boolean isEgress){}
