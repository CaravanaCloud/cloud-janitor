package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.*;
import tasktree.Configuration;
import tasktree.aws.AWSTask;
import tasktree.spi.Task;

public class DeleteInternetGateway extends AWSTask {
    private final InternetGateway resource;

    public DeleteInternetGateway(Configuration config, InternetGateway resource) {
        super(config);
        this.resource = resource;
    }

    @Override
    public void run() {
        deleteAttachments();
        deleteInternetGateway();
    }

    private void deleteAttachments() {
        resource.attachments().stream().forEach(this::deleteAttachment);
    }

    private void deleteAttachment(InternetGatewayAttachment att) {
        log().info("Detaching {}",att);
        var request = DetachInternetGatewayRequest.builder()
                .internetGatewayId(resource.internetGatewayId())
                .vpcId(att.vpcId())
                .build();
        newEC2Client().detachInternetGateway(request);
    }

    private void deleteInternetGateway() {
        log().info("Deleting internet gateway {}", resource.internetGatewayId());
        var request = DeleteInternetGatewayRequest.builder()
                .internetGatewayId(resource.internetGatewayId())
                .build();
        newEC2Client().deleteInternetGateway(request);
    }
}

