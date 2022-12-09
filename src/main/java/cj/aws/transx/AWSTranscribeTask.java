package cj.aws.transx;

import cj.TaskDescription;
import cj.TaskMaturity;
import cj.aws.AWSWrite;
import cj.aws.s3.AWSGetBucketTask;
import cj.spi.Task;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.transcribe.model.*;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static cj.TaskMaturity.Level.experimental;
import static cj.aws.AWSOutput.S3Bucket;


@Named("aws-transcribe")
@Dependent
@TaskDescription("Transcribe videos from local directory")
@TaskMaturity(experimental)
@SuppressWarnings("unused")
public class AWSTranscribeTask extends AWSWrite {
    @Inject
    AWSGetBucketTask getDataBucket;


    @Override
    public Task getDependency() {
        return getDataBucket;
    }

    @Override
    public void apply() {
        var files = findFiles("mp4");
        checkpoint("Found {} videos to transcribe: {}", files.size(), files);
        tryParallel(files, this::transcribe);
    }



    private synchronized void transcribe(Path path) {
        var tc = new Transcription();
        tc.sourcePath = path;
        var bucketIn = getDataBucket.outputAs(S3Bucket, Bucket.class)
                    .map(Bucket::name);
        if(bucketIn.isEmpty()){
            throw new IllegalStateException("No bucket found");
        }
        tc.bucketName = bucketIn.get();
        putObject(tc);
        requestTranscribe(tc);
        awaitTranscribe(tc);
        download(tc);
        debug("Transcription completed {}", tc);
    }

    private void download(Transcription tc) {
        debug("Downloading transcription {}", tc);
        try(var s3tx = aws().s3tm()) {
            var job = tc.transcriptionJob;
            var transcriptUri = job.transcript().transcriptFileUri();
            var srtPath = tc.getLangOutputSrtFilePath();
            var download = s3tx.downloadFile(b -> b.destination(srtPath)
                    .getObjectRequest(req -> req.bucket(tc.bucketName)
                    .key(tc.getOutputSrtKey())));

            download.completionFuture().join();
            var srtCopyPath = tc.getNolangOutputSrtFilePath();
            try {
                Files.copy(srtPath, srtCopyPath);
            } catch (IOException e) {
                error("Failed to copy {} to {}", srtPath, srtCopyPath);
            }
            debug("Transcription downloaded. {}", srtPath.toAbsolutePath().toString());
        }
    }

    private void awaitTranscribe(Transcription tc) {
        awaitUntil(() -> transcriptionCompleted(tc));
    }

    private boolean transcriptionCompleted(Transcription tc) {
        try(var transcribe = aws().transcribe()) {
            var req = GetTranscriptionJobRequest.builder()
                    .transcriptionJobName(tc.transcriptionJobName())
                    .build();
            var job = transcribe.getTranscriptionJob(req).transcriptionJob();
            var completionTime = job.completionTime();
            var completed = completionTime != null;
            debug("Waiting for transcription to complete. Completed {} ? {}", tc.transcriptionJobName(), completed);
            tc.transcriptionJob = job;
            return completed;
        }
    }

    private void requestTranscribe(Transcription tc) {
        debug("Downloading transcripton result for {}", tc.transcriptionJobName());
        try (var transcribe = aws().transcribe()){
            var jobName = tc.transcriptionJobName();
            var req = StartTranscriptionJobRequest.builder()
                    .transcriptionJobName(jobName)
                    .media(Media.builder()
                            .mediaFileUri(tc.sourceMediaUri())
                            .build())
                    .identifyLanguage(true)
                    .outputBucketName(tc.bucketName)
                    .outputKey(tc.getOutputKey())
                    .subtitles(Subtitles.builder()
                            .formats(SubtitleFormat.SRT)
                            .build())
                    .build();
            var job = transcribe.startTranscriptionJob(req).transcriptionJob();
            tc.transcriptionJob = job;
            info("Transcription job started {}", job.transcriptionJobName());
        }
    }

    private void putObject(Transcription tc) {
        var bucketName = tc.bucketName;
        var prefix = tc.prefix;
        var path = tc.sourcePath;
        var objKey = path.getFileName().toString();
        debug("putObject[{}] => s3://{}/{}/{}",path, bucketName, prefix, objKey);

        try(var s3 = aws().s3()){                       
            var sourceKey = prefix+objKey;
            Map<String, String> metadata = new HashMap<>();
            metadata.put("x-amz-meta-source", "cloud-janitor");
            var req = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(sourceKey)
                .metadata(metadata)
                .build();
            byte[] bytes = Files.readAllBytes(path);
            var res = s3.putObject(req, RequestBody.fromBytes(bytes));
            var etag = res.eTag();
            /* sounds good, doesnt work
            var upload = s3.uploadFile(b -> b.source(path)
                    .putObjectRequest(r -> r.bucket(bucketName).key(sourceKey) ) );
            var completeUpload = upload.completionFuture().join();
             */
            var objUrl = "s3://%s/%s%s".formatted(bucketName, prefix, objKey);       
            tc.sourceKey = objKey;
            debug("Uploaded {} as {}: etag {}", path, objUrl, etag);
        }catch(Exception ex){
            throw fail("Failed to put object to S3", ex);
        }
    }


}
