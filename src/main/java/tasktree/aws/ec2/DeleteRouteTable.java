package tasktree.aws.ec2;

import software.amazon.awssdk.services.ec2.model.DeleteRouteRequest;
import software.amazon.awssdk.services.ec2.model.DeleteRouteTableRequest;
import software.amazon.awssdk.services.ec2.model.Route;
import software.amazon.awssdk.services.ec2.model.RouteTable;
import tasktree.aws.AWSCleanup;

public class DeleteRouteTable extends AWSCleanup<RouteTable> {

    public DeleteRouteTable(RouteTable resource) {
        super(resource);
    }

    @Override
    public void cleanup(RouteTable resource) {
        if (! isMainRouteTable(resource)) {
            //deleteRoutes(resource);
            try {
                deleteRouteTable(resource);
            }catch (Exception ex){
                log().error("Failed to delete route table", ex);
                throw new RuntimeException(ex);
            }
        } else{
            log().debug("Not deleting main route table {}", resource.routeTableId());
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
        log().debug("Found rounte {}", route);
        var gatewayId = route.gatewayId();
        if ("local".equals(gatewayId)) return;
        log().info("Deleting route {}", route.toString());
        var builder = DeleteRouteRequest.builder()
                .routeTableId(resource.routeTableId());
        if (route.destinationCidrBlock() != null) {
            builder.destinationCidrBlock(route.destinationCidrBlock());
        }else if (route.destinationPrefixListId() != null) {
            builder.destinationPrefixListId(route.destinationPrefixListId());
        }
        var request = builder.build();
        newEC2Client().deleteRoute(request);
    }

    private void deleteRouteTable(RouteTable resource) {
        log().debug("Deleting route table {}", resource.routeTableId());
        var request = DeleteRouteTableRequest.builder()
                .routeTableId(resource.routeTableId())
                .build();
        newEC2Client().deleteRouteTable(request);
    }

    @Override
    protected String getResourceType() {
        return "Route Table";
    }
}
