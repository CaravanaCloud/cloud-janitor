package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.*;

public class DeleteInternetGateway extends AWSDelete<InternetGateway> {

    public DeleteInternetGateway(InternetGateway resource) {
        super(resource);
    }

    @Override
    public void cleanup(InternetGateway resource) {
        deleteAttachments(resource);
        deleteInternetGateway(resource);
    }

    private void deleteAttachments(InternetGateway resource) {
        resource.attachments().stream().forEach(att -> deleteAttachment(resource, att));
    }

    private void deleteAttachment(InternetGateway resource, InternetGatewayAttachment att) {
        log().debug("Detaching {}",att);
        var request = DetachInternetGatewayRequest.builder()
                .internetGatewayId(resource.internetGatewayId())
                .vpcId(att.vpcId())
                .build();
        newEC2Client().detachInternetGateway(request);
    }

    private void deleteInternetGateway(InternetGateway resource) {
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


}

