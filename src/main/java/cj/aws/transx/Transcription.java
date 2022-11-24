package cj.aws.transx;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import software.amazon.awssdk.services.transcribe.model.TranscriptionJob;

public class Transcription {    
    static DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    LocalDateTime createTime = LocalDateTime.now();
    Path sourcePath;
    String bucketName;
    String sourceKey;
    String prefix = "aws-transcribe";

    TranscriptionJob transcriptionJob;


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