package cj;

import cj.aws.ec2.DeleteEmptyVPCTest;
import cj.aws.ec2.DeleteVPCWithPrivateSubnetsTest;
import cj.aws.ec2.DeleteVPCWithRouteTablesTest;
import cj.aws.sts.GetCallerIdentityTask;
import cj.hello.HelloTask;
import cj.simple.HelloTaskTest;
import cj.simple.ToUppperTaskTest;

/**
 * This class suggests an order to learn how Cloud Janitor works by reading the tests.
 * Really, it does not do anything else.
 */
@SuppressWarnings("all")
public class LearningFromTests {
    Class[] simpleTasks = new Class[]{
            HelloTask.class,
            HelloTaskTest.class,
            ToUppperTaskTest.class
    };

    Class[] awsBasics = {
            GetCallerIdentityTask.class
    };

    Class[] awsEmptyDeletes = {
            DeleteEmptyVPCTest.class
    };

    Class[] populatedTests = {
            DeleteVPCWithPrivateSubnetsTest.class,
            DeleteVPCWithRouteTablesTest.class
    };
}
