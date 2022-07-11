package cloudjanitor.aws.ec2;

import cloudjanitor.aws.AWSWrite;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.ec2.model.*;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import static cloudjanitor.Output.AWS.InternetGatewayMatch;

@Dependent
public class DeleteInternetGateways extends AWSWrite {

    @Inject
    FilterInternetGateways filterIGWs;

    @Override
    public Task getDependency() {
        return filterIGWs;
    }

    public void apply(){
        var igws = filterIGWs.outputList(InternetGatewayMatch, InternetGateway.class);
        igws.forEach(this::cleanup);
    }

    public void cleanup(InternetGateway resource) {
        deleteAttachments(resource);
        deleteInternetGateway(resource);
    }

    private void deleteAttachments(InternetGateway resource) {
        resource.attachments().stream().forEach(att -> deleteAttachment(resource, att));
    }

    private void deleteAttachment(InternetGateway resource, InternetGatewayAttachment att) {
        log().debug("Detaching InternetGatewayAttachment {} from {}", att, resource);
        var request = DetachInternetGatewayRequest.builder()
                .internetGatewayId(resource.internetGatewayId())
                .vpcId(att.vpcId())
                .build();
        aws().ec2(getRegion()).detachInternetGateway(request);
    }

    private void deleteInternetGateway(InternetGateway resource) {
        log().debug("Deleting internet gateway {}", resource.internetGatewayId());
        var request = DeleteInternetGatewayRequest.builder()
                .internetGatewayId(resource.internetGatewayId())
                .build();
        aws().ec2(getRegion()).deleteInternetGateway(request);
    }

}

