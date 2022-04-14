package tasktree.aws.ec2;

import software.amazon.awssdk.services.elasticloadbalancing.model.DeleteLoadBalancerRequest;
import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerDescription;
import tasktree.aws.AWSDelete;

public class DeleteLoadBalancerDescription extends AWSDelete<LoadBalancerDescription> {

    public DeleteLoadBalancerDescription(LoadBalancerDescription resource) {
        super(resource);
    }

    @Override
    public void cleanup(LoadBalancerDescription resource) {
        log().debug("Deleting Classic ELB {}", resource.loadBalancerName());
        var request = DeleteLoadBalancerRequest.builder()
                .loadBalancerName(resource.loadBalancerName())
                .build();
        aws.getELBClient(getRegionOrDefault()).deleteLoadBalancer(request);
    }

    @Override
    protected String getResourceType() {
        return "Classic Load Balancer";
    }
}
