package cloudjanitor.aws.ec2;

import software.amazon.awssdk.regions.Region;
import cloudjanitor.aws.AWSFilter;
import cloudjanitor.spi.Task;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Stream;

@Dependent
public class FilterRegion extends AWSFilter{
   @Inject
   FilterVPCs filterVPCs;
}
