package cj.secrets.ssm;

import cj.TaskTest;
import io.quarkus.test.junit.QuarkusTest;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class SecretsExportTaskTest extends TaskTest {

    @Inject
    SecretsExportTask task;

    public void testSimpleExport(){
        submit(task);
    }
}