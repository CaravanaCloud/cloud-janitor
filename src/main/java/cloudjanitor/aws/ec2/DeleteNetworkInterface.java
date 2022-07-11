package cloudjanitor.aws.ec2;

import cloudjanitor.aws.AWSWrite;
import software.amazon.awssdk.services.ec2.model.DeleteNetworkInterfaceRequest;
import software.amazon.awssdk.services.ec2.model.DescribeNetworkInterfacesRequest;
import software.amazon.awssdk.services.ec2.model.NetworkInterface;

import javax.enterprise.context.Dependent;

import static cloudjanitor.Input.AWS.TargetNetworkInterface;

@Dependent
public class DeleteNetworkInterface extends AWSWrite {

    @Override
    public void apply() {
        var eni = getInput(TargetNetworkInterface , NetworkInterface.class);
        var eniId = eni.networkInterfaceId();
        if(canDelete(eni))
            try {
                log().debug("Deleting ENI {} {}", eniId,
                        name(eni),
                        eni.status());
                var request = DeleteNetworkInterfaceRequest.builder()
                        .networkInterfaceId(eni.networkInterfaceId())
                        .build();
                aws().ec2().deleteNetworkInterface(request);
            } catch (Exception ex){
                log().error("Failed to delete ENI {}", eniId);
                throw new RuntimeException(ex);
            }
        else{
            log().debug("ENI {} can't be deleted", eni.networkInterfaceId());
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
        try{
            var describe = aws().ec2(getRegion())
                    .describeNetworkInterfaces(req)
                    .networkInterfaces();
            if (! describe.isEmpty()){
                var eni = describe.get(0);
                var status = eni.status().toString();
                log().debug("ENI {} still exists with status {}", eni.networkInterfaceId(), status);
                boolean result = switch (status) {
                    case "detaching" -> false;
                    default -> true;
                };
                return result;
            }else{
                log().debug("ENI {} no longer exists.", resource.networkInterfaceId());
                return false;
            }
        }catch (Exception ex) {
            log().debug("Failed to describe ENI {}, assuming it no longer exists.", resource.networkInterfaceId());
            return false;
        }

    }

}
