package cloudjanitor.aws.ec2;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.ec2.model.DescribeVpcsRequest;
import software.amazon.awssdk.services.ec2.model.Vpc;
import cloudjanitor.Input;
import cloudjanitor.Output;
import cloudjanitor.aws.AWSFilter;
import cloudjanitor.spi.Task;

import javax.enterprise.context.Dependent;
import java.util.List;
import java.util.Optional;

@Dependent
public class FilterVPCs extends AWSFilter {

    @Override
    public void runSafe() {
        var vpcs = filterResources();
        success(Output.AWS.VPCMatch, vpcs);
        log().debug("VPCs filtered region={} target={} found={}",
                aws().getRegion(),
                getTargetVpcId(),
                vpcs.size());
    }

    private boolean matchVPCId(Vpc vpc){
        return vpc.vpcId().equals(getTargetVpcId());
    }

    private String getTargetVpcId() {
        var targetVpcId = inputString(Input.AWS.TargetVpcId);
        return targetVpcId;
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
        if (getTargetVpcId() != null)
            matches = matches.filter(this::matchVPCId);
        if (awsFilterPrefix.isPresent())
            matches = matches.filter(this::matchName);
        var result= matches.toList();
        return result;
    }

    public void setTargetVPC(String vpcId) {
        getInputs().put(Input.AWS.TargetVpcId, vpcId);
    }
}
