package cj.aws.transx;

import cj.TaskDescription;
import cj.TaskMaturity;
import cj.Tasks;
import cj.aws.AWSWrite;
import cj.aws.s3.AWSGetBucketTask;
import cj.aws.s3.PutObjectsTask;
import cj.fs.TaskFiles;
import cj.spi.Task;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.*;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.*;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static cj.TaskMaturity.Level.experimental;
import static cj.aws.AWSInput.s3Prefix;
import static cj.aws.AWSInput.targetBucketName;
import static cj.fs.FSInput.paths;


@Dependent
@Named("aws-translate")
@TaskMaturity(experimental)
@TaskDescription("Translates files using AWS Translate")
@SuppressWarnings("unused")
public class AWSTranslateTask extends AWSWrite {

    @Inject
    Tasks tasks;

    @Inject
    AWSGetBucketTask getDataBucket;

    @Inject
    PutObjectsTask putObjects;

    @Override
    public List<Task> getDependencies() {
        return List.of(getDataBucket);
    }

    @Override
    public void apply() {
        info("Translating files");
        var files = findFiles("srt")
                .stream().filter(f -> getLanguageCode(f) != null)
                .toList();
        checkpoint("Found {} files to translate: {}", files.size(), files);
        tryParallel(files, this::translate);
    }

    public String getLanguageCode(Path path) {
        var fileName = path.getFileName().toString();
        var nameTokens = fileName.split("\\.");
        if (nameTokens.length > 2) {
            var langCode = nameTokens[1];
            if (isSupportedLanguage(langCode)) {
                return langCode;
            }
        }
        return null;
    }

    static final String[] supported = new String[]{
        "af",
        "sq",
        "am",
        "ar",
        "hy",
        "az",
        "bn",
        "bs",
        "bg",
        "ca",
        "zh",
        "zh-TW",
        "hr",
        "cs",
        "da",
        "fa-AF",
        "nl",
        "en",
        "et",
        "fa",
        "tl",
        "fi",
        "fr",
        "fr-CA",
        "ka",
        "de",
        "el",
        "gu",
        "ht",
        "ha",
        "he",
        "hi",
        "hu",
        "is",
        "id",
        "ga",
        "it",
        "ja",
        "kn",
        "kk",
        "ko",
        "lv",
        "lt",
        "mk",
        "ms",
        "ml",
        "mt",
        "mr",
        "mn",
        "no",
        "ps",
        "pl",
        "pt",
        "pt-PT",
        "pa",
        "ro",
        "ru",
        "sr",
        "si",
        "sk",
        "sl",
        "so",
        "es",
        "es-MX",
        "sw",
        "sv",
        "ta",
        "te",
        "th",
        "tr",
        "uk",
        "ur",
        "uz",
        "vi",
        "cy"
    };

    static Set<String> supportedSet = Set.of(supported);
    private boolean isSupportedLanguage(String langCode) {
        var fullMatch = supportedSet.contains(langCode);
        if (!fullMatch){
            var tokens = langCode.split("-");
            if (tokens.length > 1) {
                var lang = tokens[0];
                var langMatch = supportedSet.contains(lang);
                return langMatch;
            }
        }
        return false;
    }

    private void translate(Path path) {
        debug("Translating {}", path);
        var sourceLang = getLanguageCode(path);
        var targetLangs = getTargetLangs(sourceLang);
        tryParallel(targetLangs, t -> this.translate(path, sourceLang, t));
    }

    private List<String> getTargetLangs(String sourceLang) {
        var lagsMap = config().translate().languages();
        var result = lagsMap.get(sourceLang);
        if(result != null){
            var split = result.split(",");
            return List.of(split);
        }
        return List.of();
    }

    enum FileType {
        SRT
    }
    protected FileType getFileType(Path path){
        if (path.getFileName().toString().endsWith("srt")){
            return FileType.SRT;
        }
        return null;
    }
    private void translate(Path path, String fromLang, String toLang) {
        debug("Translating {} from {} to {}", path, fromLang, toLang);
        try(var translate = aws().translate()){
            if (getFileType(path) == FileType.SRT) {
                translateSRT(path, fromLang, toLang, translate);
            }
        }
    }

    private void translateSRT(Path path,
                              String fromLang,
                              String toLang,
                              TranslateClient translate) {
        try (var lines = java.nio.file.Files.lines(path)) {
            StringBuilder content = new StringBuilder();
            lines.forEach(line -> {
                boolean bypass = line.isBlank() || startsWithNumber(line);
                if (bypass){
                    content.append(line);
                } else {
                    var req = TranslateTextRequest
                            .builder()
                            .sourceLanguageCode(fromLang)
                            .targetLanguageCode(toLang)
                            .text(line)
                            .build();
                    var translatedText = translate.translateText(req).translatedText();
                    content.append(translatedText);
                }
                content.append("\n");
            });
            Path dir = path.getParent();
            String basename = TaskFiles.basename(path);
            String filename = String.format("%s.%s.srt", basename, toLang);
            Path file = dir.resolve(filename);
            TaskFiles.writeFile(file, content.toString());
        } catch (IOException e) {
           throw fail("Failed to translate SRT", e);
        }
        debug("SRT translated to {}", path);
    }

    private boolean startsWithNumber(String line) {
        if (line == null || line.isBlank()){
            return false;
        }
        return Character.isDigit(line.charAt(0));
    }

    private void waitTranslate(String jobId) {
        awaitUntilLong(() -> translateDone(jobId));
    }

    private boolean translateDone(String jobId) {
        try(var translate = aws().translate()){
          return translateDone(translate, jobId);
        }
    }

    private boolean translateDone(TranslateClient translate, String jobId) {
        var req = DescribeTextTranslationJobRequest.builder().jobId(jobId).build();
        var job = translate.describeTextTranslationJob(req).textTranslationJobProperties();
        var status = job.jobStatus();
        debug("Translation {} status is {}", jobId, status);
        return switch (status){
            case UNKNOWN_TO_SDK_VERSION,
                    STOPPED,
                    STOP_REQUESTED,
                    FAILED,
                    COMPLETED_WITH_ERROR,
                    COMPLETED ->  true;
            case SUBMITTED,
             IN_PROGRESS -> false;
        };
    }


    private String startTranslation(String sourceLang, String[] targetLangCodesArr, String contentTypeIn, String bucketName, Role role, String prefix) {
        try(var translate = aws().translate()){
            return startTranslation(translate, sourceLang, targetLangCodesArr, contentTypeIn, bucketName, role, prefix);
        }
    }

    private String startTranslation(TranslateClient translate, String sourceLang, String[] targetLangCodesArr, String contentTypeIn, String bucketName, Role role, String prefix) {
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
        try(var iam = aws().iam()){
            return getRole(iam, roleName);
        }
    }

    private Role getRole(IamClient iam, String roleName) {
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
