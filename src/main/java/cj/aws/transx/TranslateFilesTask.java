package cj.aws.transx;

import cj.Output;
import cj.Tasks;
import cj.aws.AWSWrite;
import cj.aws.s3.GetDataBucketTask;
import cj.aws.s3.PutObjectsTask;
import cj.fs.GlobFilesTask;
import cj.spi.Task;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.*;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.translate.model.*;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import static cj.Input.aws.*;
import static cj.Input.fs.*;
import static cj.Output.aws.*;
import static org.awaitility.Awaitility.await;
import static software.amazon.awssdk.services.translate.model.JobStatus.COMPLETED;


@Dependent
@Named("aws-translate")
public class TranslateFilesTask extends AWSWrite {
    @Inject
    Tasks tasks;

    @Inject
    GlobFilesTask globFiles;

    @Inject
    GetDataBucketTask getDataBucket;

    @Inject
    PutObjectsTask putObjects;

    @Override
    public List<Task> getDependencies() {
        return List.of(globFiles,
                getDataBucket);
    }

    @Override
    public void apply() {
        var sourceLang = expectInputString(sourceLanguage);
        var targetLangCodesStr = expectInputString(targetLanguages);
        var targetLangCodesArr = targetLangCodesStr.split(",");
        var contentTypeIn = inputString(contentType).orElse("text/plain");
        var files = globFiles.outputList(Output.fs.paths, Path.class);
        debug("Traslating {} files from {} to {}", files.size(), sourceLang, targetLangCodesArr);
        if (files.isEmpty()){
            return;
        }
        var bucket = getDataBucket.outputAs(S3Bucket, Bucket.class);
        if (bucket.isPresent()){
            var bucketName = bucket.get().name();
            var role = checkTranslateDataRole(bucketName);
            debug("Uploading files to {}", bucketName);
            var prefix = putObjects(files, bucketName);

            debug("Creating translate job");
            var jobId = startTranslation(sourceLang, targetLangCodesArr, contentTypeIn, bucketName, role, prefix);

            debug("Wait translate job {}", jobId);
            waitTranslate(jobId);

            debug("Download translations");

            info("Translation job started: {}", jobId);
        }else {
            throw fail("Data bucket not found");
        }
    }

    private void waitTranslate(String jobId) {
        awaitUntilLong(() -> translateDone(jobId));
    }

    private boolean translateDone(String jobId) {
        var translate = aws().translate();
        var req = DescribeTextTranslationJobRequest.builder().jobId(jobId).build();
        var job = translate.describeTextTranslationJob(req).textTranslationJobProperties();
        var status = job.jobStatus();
        debug("Translation {} status is {}", jobId, status);
        switch (status){
            case UNKNOWN_TO_SDK_VERSION:
            case STOPPED:
            case STOP_REQUESTED:
            case FAILED:
            case COMPLETED_WITH_ERROR:
            case COMPLETED: return true;
            case SUBMITTED:
            case IN_PROGRESS: return false;
        }
        return false;
    }


    private String startTranslation(String sourceLang, String[] targetLangCodesArr, String contentTypeIn, String bucketName, Role role, String prefix) {
        var translate = aws().translate();
        var jobName = getExecutionId();
        var s3in = "s3://%s/%s".formatted(bucketName, prefix);
        var inCfg = InputDataConfig.builder()
                .s3Uri(s3in)
                .contentType(contentTypeIn)
                .build();

        var outPrefix = prefix +"-output";
        var s3out = "s3://%s/%s".formatted(bucketName, outPrefix);
        var outCfg = OutputDataConfig.builder()
                .s3Uri(s3out)
                .build();
        debug("Starting translation job");
        debug("s3in {}", s3in);
        debug("s3out {}", s3out);
        var jobReq = StartTextTranslationJobRequest.builder()
                .inputDataConfig(inCfg)
                .outputDataConfig(outCfg)
                .dataAccessRoleArn(role.arn())
                .sourceLanguageCode(sourceLang)
                .targetLanguageCodes(targetLangCodesArr)
                .jobName(jobName)
                .build();
        var jobId = translate.startTextTranslationJob(jobReq).jobId();
        return jobId;
    }

    private String putObjects(List<Path> files, String bucketName) {
        var prefix = tasks.getExecutionId();
        var task = putObjects
                .withInput(targetBucketName, bucketName)
                .withInput(s3Prefix, prefix)
                .withInput(paths, files);
        submit(task);
        return prefix;
    }

    private Role checkTranslateDataRole(String bucketName) {
        var roleName = bucketName+"-translate";
        var role = getRole(roleName);
        if (role == null) {
            role = createRole(roleName);
        }
        return role;
    }

    String trustPolicy = """
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "translate.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}            
            """;

    String permissionsPolicy = """
            {
              "Version": "2012-10-17",
              "Statement": [
                {
                  "Effect": "Allow",
                  "Action": "s3:*",
                  "Resource": "*" 
                }
              ]
            }
            """;
    private Role createRole(String roleName) {
        var req = CreateRoleRequest.builder()
                .roleName(roleName)
                .assumeRolePolicyDocument(trustPolicy)
                .build();
        var iam = aws().iam();
        var role = iam.createRole(req).role();
        debug("Role {} created", roleName);
        attachRolePolicy(iam, role);
        return role;
    }

    private void attachRolePolicy(IamClient iam, Role role) {
        var req = AttachRolePolicyRequest.builder()
                        .roleName(role.roleName())
                        .policyArn("arn:aws:iam::aws:policy/AmazonS3FullAccess")
                        .build();
        iam.attachRolePolicy(req);
        debug("Policy attached to role.");
    }

    private Role getRole(String roleName) {
        var iam = aws().iam();
        var req = GetRoleRequest
                .builder()
                .roleName(roleName)
                .build();
        try {
            var role = iam.getRole(req).role();
            debug("Role {} found.", role);
            return role;
        }catch (NoSuchEntityException ex){
            return null;
        }
    }
}
