open module cj {
    requires info.picocli;
    requires org.slf4j;
    requires awaitility;
    requires jakarta.enterprise.cdi.api;
    requires jakarta.inject.api;
    requires quarkus.core;
    requires smallrye.config.core;
    requires smallrye.config.common;
    requires smallrye.config.source.yaml;
    requires qute.core;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.http;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.awscore;
    requires software.amazon.awssdk.services.athena;
    requires software.amazon.awssdk.services.ec2;
    requires software.amazon.awssdk.services.route53;
    requires software.amazon.awssdk.services.elasticloadbalancing;
    requires software.amazon.awssdk.services.elasticloadbalancingv2;
    requires software.amazon.awssdk.services.iam;
    requires software.amazon.awssdk.services.cloudformation;
    requires software.amazon.awssdk.services.sts;
    requires software.amazon.awssdk.services.transcribe;
    requires software.amazon.awssdk.services.translate;
    requires software.amazon.awssdk.services.s3;
    requires software.amazon.awssdk.transfer.s3;

}