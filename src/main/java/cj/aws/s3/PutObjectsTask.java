package cj.aws.s3;

import cj.Input;
import cj.aws.AWSWrite;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.UploadFileRequest;
import software.amazon.awssdk.transfer.s3.UploadRequest;

import javax.enterprise.context.Dependent;
import javax.inject.Named;
import java.nio.file.Path;

import static cj.Input.aws.*;
import static cj.Input.fs.*;

@Dependent
@Named("aws-put-objects")
public class PutObjectsTask extends AWSWrite {
    @Override
    public void apply() {
        var bucketName = expectInputString(targetBucketName);
        var paths = inputList(Input.fs.paths, Path.class);
        var prefix = inputString(s3Prefix).orElse("");
        var s3tm = aws().s3tm();
        debug("Putting {} files to {}/{}", paths.size(), bucketName, prefix);
        paths.forEach(path -> upload(s3tm,bucketName,prefix, path));
        debug("Put objects finished");
    }

    private void upload(S3TransferManager s3tm, String bucket,String prefix, Path path) {
        var key = path.getFileName().toFile().getName();
        if (prefix != null){
            key = prefix + "/" + key;
        }
        var put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        var upf = UploadFileRequest.builder()
                .source(path)
                .putObjectRequest(put)
                .build();

        s3tm.uploadFile(upf);
        debug("File {} uploaded to s3://{}/{}/{}", path, bucket,prefix, key);
    }
}
