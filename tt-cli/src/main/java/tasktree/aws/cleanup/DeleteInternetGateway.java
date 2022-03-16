package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.*;
import tasktree.Configuration;

public class DeleteInternetGateway extends AWSDelete {
    private final InternetGateway resource;

    public DeleteInternetGateway(InternetGateway resource) {
        this.resource = resource;
    }

    @Override
    public void runSafe() {
        deleteAttachments();
        deleteInternetGateway();
    }

    private void deleteAttachments() {
        resource.attachments().stream().forEach(this::deleteAttachment);
    }

    private void deleteAttachment(InternetGatewayAttachment att) {
        log().debug("Detaching {}",att);
        var request = DetachInternetGatewayRequest.builder()
                .internetGatewayId(resource.internetGatewayId())
                .vpcId(att.vpcId())
                .build();
        newEC2Client().detachInternetGateway(request);
    }

    private void deleteInternetGateway() {
        log().debug("Deleting internet gateway {}", resource.internetGatewayId());
        var request = DeleteInternetGatewayRequest.builder()
                .internetGatewayId(resource.internetGatewayId())
                .build();
        newEC2Client().deleteInternetGateway(request);
    }

    @Override
    protected String getResourceType() {
        return "Internet Gateway";
    }

    @Override
    public String getResourceDescription() {
        return resource.internetGatewayId();
    }

}

