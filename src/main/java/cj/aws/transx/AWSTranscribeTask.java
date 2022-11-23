package cj.aws.transx;

import cj.TaskDescription;
import cj.aws.AWSWrite;
import cj.aws.s3.GetDataBucketTask;
import cj.spi.Task;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.transcribe.model.*;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static cj.Output.aws.S3Bucket;


@Named("aws-transcribe")
@Dependent
@TaskDescription("Transcribe videos from local directory")
@SuppressWarnings("unused")
public class AWSTranscribeTask extends AWSWrite {
    @Inject
    GetDataBucketTask getDataBucket;

    static final String prefix = "transcribe/";

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



    private void transcribe(Path path) {
        var tc = new Transcription();
        tc.sourcePath = path;
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
        try(var s3 = aws().s3tm()){
            var bucketIn = getDataBucket.outputAs(S3Bucket, Bucket.class)
                    .map(Bucket::name);
            if(bucketIn.isEmpty()){
                throw new IllegalStateException("No bucket found");
            }
            var bucketName = bucketIn.get();
            var path = tc.sourcePath;
            var objKey = path.getFileName().toString();
            var sourceKey = prefix+objKey;
            var upload = s3.uploadFile(b -> b.source(path)
                    .putObjectRequest(r -> r.bucket(bucketName).key(sourceKey) ) );
            var completeUpload = upload.completionFuture().join();
            var objUrl = "s3://%s/%s%s".formatted(bucketName, prefix, objKey);
            tc.bucketName = bucketName;
            tc.sourceKey = objKey;
            debug("Uploaded {} as {} ", path, objUrl);
        }
    }

    static class Transcription {
        TranscriptionJob transcriptionJob;
        static DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime createTime = LocalDateTime.now();
        Path sourcePath;
        String bucketName;
        String sourceKey;


        public String getOutputSrtKey(){
            return getOutputKey() + ".srt";
        }
        public String getOutputKey(){
            var filename = sourcePath.getFileName().toString();
            var filebase = filename.substring(0, filename.lastIndexOf('.'));
            var outputkey = "%s%s".formatted(prefix,filebase);
            outputkey = validKey(outputkey);
            return outputkey;
        }

        private String validKey(String key) {
            return key.replaceAll("[^0-9a-zA-Z._-]", "_");
        }

        public String transcriptionJobName(){
            var jobBaseName = sourceKey + "-" +createTime.format(fmt);
            @SuppressWarnings("VariableTypeCanBeExplicit")
            var jobName = validKey(jobBaseName);
            return jobName;
        }
        public String sourceMediaUri() {
            @SuppressWarnings("UnnecessaryLocalVariable")
            var uri = "s3://%s/%s%s".formatted(bucketName, prefix, sourceKey);
            return uri;
        }

        public Path getLangOutputSrtFilePath() {
            var sourceName = sourcePath.getFileName().toString();
            var filebase = sourceName.substring(0, sourceName.lastIndexOf('.'));
            var langCode = transcriptionJob.languageCode().toString();
            var srtFile = "%s.%s.srt".formatted(filebase, langCode);
            @SuppressWarnings("UnnecessaryLocalVariable")
            var srtPath = sourcePath.getParent().resolve(srtFile);
            return srtPath;
        }

        public Path getNolangOutputSrtFilePath() {
            var sourceName = sourcePath.getFileName().toString();
            var filebase = sourceName.substring(0, sourceName.lastIndexOf('.'));
            var srtFile = "%s.srt".formatted(filebase);
            @SuppressWarnings("UnnecessaryLocalVariable")
            var srtPath = sourcePath.getParent().resolve(srtFile);
            return srtPath;
        }

        @Override
        public String toString() {
            return Map.of("jobName", transcriptionJobName()).toString();
        }
    }
}
