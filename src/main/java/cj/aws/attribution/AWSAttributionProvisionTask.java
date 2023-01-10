package cj.aws.attribution;

import cj.TaskDescription;
import cj.TaskMaturity;
import cj.TaskRepeater;
import cj.aws.AWSIdentity;
import cj.aws.AWSOutput;
import cj.aws.AWSTask;
import cj.aws.s3.AWSGetBucketTask;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.QueryExecutionContext;
import software.amazon.awssdk.services.athena.model.ResultConfiguration;
import software.amazon.awssdk.services.athena.model.StartQueryExecutionRequest;
import software.amazon.awssdk.services.cloudtrail.CloudTrailClient;
import software.amazon.awssdk.services.cloudtrail.model.CreateTrailRequest;
import software.amazon.awssdk.services.cloudtrail.model.GetTrailRequest;
import software.amazon.awssdk.services.cloudtrail.model.StartLoggingRequest;
import software.amazon.awssdk.services.s3.model.Bucket;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static cj.TaskMaturity.Level.*;
import static cj.TaskRepeat.*;
import static cj.aws.AWSInput.bucketPolicy;
import static cj.aws.AWSInput.targetBucketName;

//TODO: Auto sign-up to aws config
@Dependent
@Named("aws-attribution-provision")
@TaskDescription("Provisions attribution resources (bucket, trail and table)")
@TaskMaturity(experimental)
@TaskRepeater(each_identity_region)
public class AWSAttributionProvisionTask extends AWSTask {
    @Inject
    AWSGetBucketTask getBucketTask;

    @Override
    public void applyIdentity(AWSIdentity id){
        debug("Provisioning resources for AWS Attribution on region {}", region());
        var trailName = composeName(accountId(), regionName());
        try (var cloudtrail = aws().cloudtrail()){
            getTrailForRegion(cloudtrail, region(), trailName);
        }
    }

    private String getTrailForRegion(CloudTrailClient cloudtrail, Region region, String trailName) {
        debug("Looking for trail {} in region {}", trailName, region);
        var req = GetTrailRequest.builder()
                .name(trailName)
                .build();
        try {
            var trail = cloudtrail.getTrail(req).trail();
            var arn = trail.trailARN();
            debug("Found trail {} in region {}", arn, region);
            return success(arn);
        }catch (Exception e) {
            error("Error getting trail for region {}: {}", region, e.getMessage());
            var newTrailArn = createTrailForRegion(cloudtrail, region, trailName);
            return success(newTrailArn);
        }
    }
    private String createTrailForRegion(CloudTrailClient cloudtrail, Region region, String trailName) {
        var policyTemplate = """
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "AWSCloudTrailAclCheck20150319",
            "Effect": "Allow",
            "Principal": {
                "Service": "cloudtrail.amazonaws.com"
            },
            "Action": "s3:GetBucketAcl",
            "Resource": "arn:aws:s3:::$BUCKET_NAME"
        },
        {
            "Sid": "AWSCloudTrailWrite20150319",
            "Effect": "Allow",
            "Principal": {
                "Service": "cloudtrail.amazonaws.com"
            },
            "Action": "s3:PutObject",
            "Resource": "arn:aws:s3:::$BUCKET_NAME/*"
        }
    ]
}
""";
        var accountId = identityInfo().accountId();
        var bucketName = composeName("attribution", accountId, region.toString());
        var keyPrefix = "attribution";
        var policy = policyTemplate
                .replace("$ACCOUNT_ID", accountId)
                .replace("$TRAIL_NAME", trailName)
                .replace("$BUCKET_NAME", bucketName)
                .replace("$REGION_NAME", regionName())
                .replace("$ACCOUNT_ID", accountId)
                .replace("$BUCKET_PREFIX", keyPrefix);
        log().debug(policy);
        var task = getBucketTask
                .withInput(targetBucketName, bucketName)
                .withInput(bucketPolicy, policy);
        task = submit(task);
        var bucketIn = task.outputAs(AWSOutput.S3Bucket, Bucket.class);
        if (bucketIn.isEmpty())
            throw fail("Attribution bucket not found for region {}", region);
        var bucket = bucketIn.get();
        var req = CreateTrailRequest.builder()
                .name(trailName)
                .s3BucketName(bucketName)
                .s3KeyPrefix(keyPrefix)
                .isMultiRegionTrail(false)
                .includeGlobalServiceEvents(false)
                .isOrganizationTrail(false)
                .build();
        try {
            var resp = cloudtrail.createTrail(req);
            var result = resp.trailARN();
            debug("Created trail {} in region {}", trailName, region);
            startLogging(cloudtrail, trailName);
            createTableForTrail(trailName, bucketName, keyPrefix);
            return success(result);
        }catch (Exception e) {
            e.printStackTrace();
            throw fail("Failed to create trail for region %s", region);
        }
    }

    private void startLogging(CloudTrailClient cloudtrail, String trailName) {
        var req = StartLoggingRequest.builder()
                .name(trailName)
                .build();
        var resp = cloudtrail.startLogging(req);
        debug("Started logging for trail {}", trailName);
    }


    private void createTableForTrail(String trailName, String bucketName, String bucketPrefix) {
        debug("Creating athena table for trail");
        var ddlTemplate = """
CREATE EXTERNAL TABLE $TABLE_NAME (
    eventVersion STRING,
    userIdentity STRUCT<
        type: STRING,
        principalId: STRING,
        arn: STRING,
        accountId: STRING,
        invokedBy: STRING,
        accessKeyId: STRING,
        userName: STRING,
        sessionContext: STRUCT<
            attributes: STRUCT<
                mfaAuthenticated: STRING,
                creationDate: STRING>,
            sessionIssuer: STRUCT<
                type: STRING,
                principalId: STRING,
                arn: STRING,
                accountId: STRING,
                username: STRING>,
            ec2RoleDelivery: STRING,
            webIdFederationData: MAP<STRING,STRING>>>,
    eventTime STRING,
    eventSource STRING,
    eventName STRING,
    awsRegion STRING,
    sourceIpAddress STRING,
    userAgent STRING,
    errorCode STRING,
    errorMessage STRING,
    requestParameters STRING,
    responseElements STRING,
    additionalEventData STRING,
    requestId STRING,
    eventId STRING,
    resources ARRAY<STRUCT<
        arn: STRING,
        accountId: STRING,
        type: STRING>>,
    eventType STRING,
    apiVersion STRING,
    readOnly STRING,
    recipientAccountId STRING,
    serviceEventDetails STRING,
    sharedEventID STRING,
    vpcEndpointId STRING,
    tlsDetails STRUCT<
        tlsVersion: STRING,
        cipherSuite: STRING,
        clientProvidedHostHeader: STRING>
)
COMMENT 'CloudTrail table for bucket'
ROW FORMAT SERDE 'org.apache.hive.hcatalog.data.JsonSerDe'
STORED AS INPUTFORMAT 'com.amazon.emr.cloudtrail.CloudTrailInputFormat'
OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
LOCATION 's3://$BUCKET_NAME/$BUCKET_PREFIX/AWSLogs/$ACCOUNT_ID/CloudTrail/'
TBLPROPERTIES ('classification'='cloudtrail');
""";
        var tableName = composeNameAlt("attribution",
                accountId() ,
                regionName());
        var ddl = ddlTemplate.replace("$TABLE_NAME", tableName)
                .replace("$BUCKET_NAME", bucketName)
                .replace("$BUCKET_PREFIX", bucketPrefix)
                .replace("$ACCOUNT_ID", accountId());
        try(var athena = aws().athena()){
            var req = StartQueryExecutionRequest.builder()
                    .queryString(ddl)
                    .queryExecutionContext(QueryExecutionContext.builder()
                            .database("default")
                            .build())
                    .resultConfiguration(ResultConfiguration.builder()
                            .outputLocation("s3://" + bucketName + "/" + bucketPrefix + "/athena/")
                            .build())
                    .build();
            debug("Creating athena attribution table: {}", tableName);
            debug(ddl);
            checkpoint("Creating athena attribution table");
            athena.startQueryExecution(req);
            debug("Athena table created for trail {}", trailName);
            if (isValid(athena, tableName)){
                debug("Athena table is valid");
            }else {
                warn("Athena table is NOT valid");
            }
        }
    }

    private boolean isValid(AthenaClient athena, String tableName) {
        try(var conn = getConnection()){
            return conn.isValid(60);
        } catch (SQLException e) {
            fail(e);
            return false;
        }
    }

    //TODO: Support multiple identities, consider 7using the profile credentials provider
    // (Pg. 30) https://s3.amazonaws.com/athena-downloads/drivers/JDBC/SimbaAthenaJDBC-2.0.35.1000/docs/Simba+Amazon+Athena+JDBC+Connector+Install+and+Configuration+Guide.pdf
    private Connection getConnection() throws SQLException {
        var props = new Properties();
        props.put("AwsCredentialsProviderClass",
                "com.simba.athena.amazonaws.auth.DefaultAWSCredentialsProviderChain");
        var url = "jdbc:awsathena://AwsRegion=" + regionName();
        return DriverManager.getConnection(url, props);
    }
}
