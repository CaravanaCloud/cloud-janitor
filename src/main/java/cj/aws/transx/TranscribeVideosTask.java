package cj.aws.transx;

import cj.aws.AWSWrite;
import cj.aws.s3.GetDataBucket;
import cj.fs.FilterLocalVideos;
import cj.spi.Task;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.transcribe.model.*;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static cj.Input.local.fileExtension;
import static cj.Output.AWS.S3Bucket;
import static cj.Output.Local.FilesMatch;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;


@Named("aws-transcribe-videos")
@Dependent
public class TranscribeVideosTask extends AWSWrite {
    @Inject
    FilterLocalVideos filterFiles;

    @Inject
    GetDataBucket getDataBucket;

    static final String prefix = "transcribe/";

    @Override
    public List<Task> getDependencies() {
        return List.of(
                filterFiles.withInput(fileExtension, "mp4"),
                getDataBucket
        );
    }

    @Override
    public void apply() {
        var files = filterFiles.outputList(FilesMatch, Path.class);
        debug("Found {} videos to transcribe: {}", files.size(), files);
        files.stream().parallel().forEach(this::transcribe);
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
        var s3tx = aws().s3tx();
        var job = tc.transcriptionJob;
        var transcriptUri = job.transcript().transcriptFileUri();
        var srtPath = tc.getOutputSrtFilePath();
        var download = s3tx.downloadFile(b -> b.destination(srtPath)
                        .getObjectRequest(req -> req.bucket(tc.bucketName)
                                .key(tc.getOutputSrtKey())));
        download.completionFuture().join();
        debug("Transcription downloaded. {}", srtPath.toAbsolutePath().toString());
    }

    private void awaitTranscribe(Transcription tc) {
            await().atMost(10, MINUTES)
                    .pollInterval(getPollInterval(), SECONDS)
                    .until(() -> transcriptionCompleted(tc));
    }

    static final Random rand = new Random();
    private int getPollInterval() {
        var variance = 0.10;
        var pollInterval = 30.00;
        var noise = rand.nextDouble() * variance;
        var signal = 1 - noise;
        pollInterval *= signal;
        return Double.valueOf(pollInterval).intValue();
    }

    private boolean transcriptionCompleted(Transcription tc) {
        var transcribe = aws().transcribe();
        var req = GetTranscriptionJobRequest.builder()
                .transcriptionJobName(tc.transcriptionJobName())
                .build();
        var job = aws().transcribe().getTranscriptionJob(req).transcriptionJob();
        var completionTime = job.completionTime();
        var completed = completionTime != null;
        debug("Waiting for transcription to complete. Completed {} ? {}" , tc.transcriptionJobName(), completed);
        tc.transcriptionJob = job;
        return completed;
    }

    private void requestTranscribe(Transcription tc) {
        var transcribe = aws().transcribe();
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


    private void putObject(Transcription tc) {
        var s3 = aws().s3tx();
        var bucketName = getDataBucket.outputAs(S3Bucket, Bucket.class).map(Bucket::name).get();
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
            var jobName = validKey(jobBaseName);
            return jobName;
        }
        public String sourceMediaUri() {
            var uri = "s3://%s/%s%s".formatted(bucketName, prefix, sourceKey);
            return uri;
        }

        public Path getOutputSrtFilePath() {
            var sourceName = sourcePath.getFileName().toString();
            var filebase = sourceName.substring(0, sourceName.lastIndexOf('.'));
            var langCode = transcriptionJob.languageCode().toString();
            var srtFile = "%s.%s.srt".formatted(filebase, langCode);
            var srtPath = sourcePath.getParent().resolve(srtFile);
            return srtPath;
        }

        @Override
        public String toString() {
            return Map.of("jobName", transcriptionJobName()).toString();
        }
    }
}
