package cloudjanitor.aws.ec2;

import cloudjanitor.Input;
import cloudjanitor.aws.AWSWrite;
import software.amazon.awssdk.services.ec2.model.DeleteRouteRequest;
import software.amazon.awssdk.services.ec2.model.DeleteRouteTableRequest;
import software.amazon.awssdk.services.ec2.model.Route;
import software.amazon.awssdk.services.ec2.model.RouteTable;

import javax.enterprise.context.Dependent;


@Dependent
public class DeleteRouteTable extends AWSWrite {


    @Override
    public void apply() {
        RouteTable resource = getInput(Input.AWS.RouteTable, RouteTable.class);
        if (! isMainRouteTable(resource)) {
            deleteRoutes(resource);
            try {
                deleteRouteTable(resource);
            }catch (Exception ex){
                error("Failed to delete route table", ex);
                throw new RuntimeException(ex);
            }
        } else{
            debug("Not deleting main route table {}", resource.routeTableId());
        }
    }

    private boolean isMainRouteTable(RouteTable resource) {
        boolean isMain = resource.associations().stream().anyMatch(assoc -> assoc.main());
        return isMain;
    }

    private void deleteRoutes(RouteTable resource) {
        resource.routes().stream().forEach(r -> deleteRoute(resource, r));
    }

    private void deleteRoute(RouteTable resource, Route route) {
        var gatewayId = route.gatewayId();
        if ("local".equals(gatewayId)) {
            debug("Not deleting local route {} / {}", resource.routeTableId(), route.toString());
            return;
        }else{
            var builder = DeleteRouteRequest.builder()
                    .routeTableId(resource.routeTableId());
            if (route.destinationCidrBlock() != null) {
                builder.destinationCidrBlock(route.destinationCidrBlock());
            }else if (route.destinationPrefixListId() != null) {
                builder.destinationPrefixListId(route.destinationPrefixListId());
            }
            var request = builder.build();
            aws().ec2().deleteRoute(request);
            debug("Deleted route {} / {}", resource.routeTableId(), route.toString());
        }
    }

    private void deleteRouteTable(RouteTable resource) {
        debug("Deleting route table {}", resource.routeTableId());
        var request = DeleteRouteTableRequest.builder()
                .routeTableId(resource.routeTableId())
                .build();
        aws().ec2(getRegion()).deleteRouteTable(request);
    }
}
