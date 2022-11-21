package cj.aws.s3;

import cj.aws.AWSWrite;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

@Named("copy-bucket")
@Dependent
@SuppressWarnings("unused")
public class CopyBucketTask extends AWSWrite {
    @Override
    public void apply() {
        throw new RuntimeException("Not implemented");
    }
}
