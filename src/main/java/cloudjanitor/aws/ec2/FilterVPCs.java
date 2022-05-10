package cloudjanitor.aws.ec2;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.ec2.model.DescribeVpcsRequest;
import software.amazon.awssdk.services.ec2.model.Vpc;
import cloudjanitor.aws.AWSFilter;
import cloudjanitor.spi.Task;

import javax.enterprise.context.Dependent;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Dependent
public class FilterVPCs extends AWSFilter {
    @ConfigProperty(name = "cj.aws.vpc.id")
    Optional<String> targetVpcId;

    @Override
    public void runSafe() {
        var vpcs = filterResources();
        success("aws.vpc.matches", vpcs);
        log().debug("VPCs filtered region={} target={} found={}",
                aws().getRegion(),
                targetVpcId.orElse(""),
                vpcs.size());
    }

    private boolean matchVPCId(Vpc vpc){
        return vpc.vpcId().equals(targetVpcId.get());
    }

    private boolean matchName(Vpc vpc) {
        var matchName = vpc.tags().stream()
                .anyMatch(tag -> tag.key().equals("Name")
                        && matchName(tag.value()));
        return matchName;
    }

    private List<Vpc> findAll(){
        var ec2 = aws().getEC2Client();
        var request = DescribeVpcsRequest.builder().build();
        var resources = ec2.describeVpcs(request).vpcs();
        return resources;
    }

    protected List<Vpc> filterResources() {
        var matches = findAll().stream();
        if (targetVpcId.isPresent())
            matches = matches.filter(this::matchVPCId);
        if (awsFilterPrefix.isPresent())
            matches = matches.filter(this::matchName);
        var result= matches.toList();
        return result;
    }

    public void setTargetVPC(String vpcId) {
        targetVpcId  = Optional.of(vpcId);
    }
}
