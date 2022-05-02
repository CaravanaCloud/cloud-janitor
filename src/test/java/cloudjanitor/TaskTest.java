package cloudjanitor;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TaskTest {
    @Inject
    protected Tasks tasks;

    @Inject
    protected Configuration config;

    @BeforeAll
    public void disableDryRun(){
        config.setDryRun(false);

    }
}