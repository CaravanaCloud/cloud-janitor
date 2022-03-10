package tasktree.aws.cleanup;

import software.amazon.awssdk.services.ec2.model.DeleteRouteRequest;
import software.amazon.awssdk.services.ec2.model.DeleteRouteTableRequest;
import software.amazon.awssdk.services.ec2.model.Route;
import software.amazon.awssdk.services.ec2.model.RouteTable;
import tasktree.Configuration;

public class DeleteRouteTable extends AWSDelete {
    private final RouteTable resource;

    public DeleteRouteTable(RouteTable resource) {
        this.resource = resource;
    }

    @Override
    public void run() {
        if (! isMainRouteTable()) {
            deleteRoutes();
            deleteRouteTable();
        } else{
            log().debug("Not deleting main route table {}", resource.routeTableId());
        }
    }

    private boolean isMainRouteTable() {
        boolean isMain = resource.associations().stream().anyMatch(assoc -> assoc.main());
        return isMain;
    }

    private void deleteRoutes() {
        resource.routes().stream().forEach(this::deleteRoute);
    }

    private void deleteRoute(Route route) {
        log().debug("Found rounte {}", route);
        var gatewayId = route.gatewayId();
        if ("local".equals(gatewayId)) return;
        log().info("Deleting route {}", route.toString());
        var request = DeleteRouteRequest.builder()
                .destinationCidrBlock(route.destinationCidrBlock())
                .routeTableId(resource.routeTableId())
                .build();
        newEC2Client().deleteRoute(request);
    }

    private void deleteRouteTable() {
        log().debug("Deleting route table {}", resource.routeTableId());
        var request = DeleteRouteTableRequest.builder()
                .routeTableId(resource.routeTableId())
                .build();
        newEC2Client().deleteRouteTable(request);
    }

    @Override
    public String toString() {
        return super.toString("Route Table",resource.routeTableId());
    }
}
