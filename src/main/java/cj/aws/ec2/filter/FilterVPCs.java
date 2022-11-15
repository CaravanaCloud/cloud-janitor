package cj.aws.ec2.filter;

import cj.Input;
import cj.Output;
import cj.aws.AWSFilter;
import cj.aws.AWSInput;
import software.amazon.awssdk.services.ec2.model.DescribeVpcsRequest;
import software.amazon.awssdk.services.ec2.model.Vpc;

import javax.enterprise.context.Dependent;
import java.util.List;

@Dependent
public class FilterVPCs extends AWSFilter {

    @Override
    public void apply() {
        var vpcs = filterResources();
        success(Output.aws.VPCMatch, vpcs);
    }

    private boolean matchVPCId(Vpc vpc){
        return vpc.vpcId().equals(getTargetVpcId());
    }

    private String getTargetVpcId() {
        var targetVpcId = getInputString(AWSInput.targetVPCId);
        return targetVpcId;
    }

    private boolean matchName(Vpc vpc) {
        var matchName = vpc.tags().stream()
                .anyMatch(tag -> tag.key().equals("Name")
                        && matchName(tag.value()));
        return matchName;
    }

    private List<Vpc> findAll(){
        var ec2 = aws().ec2();
        var request = DescribeVpcsRequest.builder().build();
        var resources = ec2.describeVpcs(request).vpcs();
        return resources;
    }

    protected List<Vpc> filterResources() {
        var resources = findAll();
        var matches = resources.stream();
        if (getTargetVpcId() != null)
            matches = matches.filter(this::matchVPCId);
        var prefix = aws().config().filterPrefix();
        if (prefix.isPresent())
            matches = matches.filter(this::matchName);
        var result= matches.toList();
        debug("Matched {}/{} VPCs",  result.size(), resources.size());
        return result;
    }

    public void setTargetVPC(String vpcId) {
        getInputs().put(AWSInput.targetVPCId, vpcId);
    }
}
