package tasktree.aws.cleanup.ec2;

import software.amazon.awssdk.services.ec2.model.DeleteNetworkInterfaceRequest;
import software.amazon.awssdk.services.ec2.model.DescribeNetworkInterfacesRequest;
import software.amazon.awssdk.services.ec2.model.NetworkInterface;
import tasktree.aws.AWSDelete;

import java.util.Optional;

public class DeleteNetworkInterface extends AWSDelete<NetworkInterface> {

    public DeleteNetworkInterface(NetworkInterface resource) {
        super(resource);
    }

    @Override
    public void cleanup(NetworkInterface resource) {
        var eniId = resource.networkInterfaceId();
        if(canDelete(resource)) {
            log().debug("Deleting ENI {} {}", eniId,
                    name(resource),
                    resource.status());
            var request = DeleteNetworkInterfaceRequest.builder()
                    .networkInterfaceId(resource.networkInterfaceId())
                    .build();
            newEC2Client().deleteNetworkInterface(request);
        }else{
            log().debug("ENI {} can't be deleted", resource.networkInterfaceId());
        }
    }

    private String name(NetworkInterface resource) {
        var tags = resource.tagSet();
        var name = tags.stream()
                .filter(t -> "Name".equals(t.key()))
                .map(t -> t.value())
                .findFirst()
                .orElse("");
        return name;
    }

    private boolean canDelete(NetworkInterface resource) {

        var req = DescribeNetworkInterfacesRequest.builder()
                .networkInterfaceIds(resource.networkInterfaceId())
                .build();
        var describe = newEC2Client().describeNetworkInterfaces(req)
                .networkInterfaces();
        if (! describe.isEmpty()){
            var eni = describe.get(0);
            var status = eni.status().toString();
            boolean result = switch (status) {
                case "detaching" -> false;
                default -> true;
            };
            return result;
        }else{
            log().debug("ENI {} no longer exists.", resource.networkInterfaceId());
            return false;
        }
    }

    @Override
    protected String getResourceType() {
        return "Network Interface";
    }

    @Override
    public Optional<Long> getWaitAfterRun() {
        return Optional.of(30_000L);
    }
}
