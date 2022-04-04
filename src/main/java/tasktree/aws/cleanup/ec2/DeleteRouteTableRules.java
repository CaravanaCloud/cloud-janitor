package tasktree.aws.cleanup.ec2;
import software.amazon.awssdk.services.ec2.model.DeleteRouteRequest;
import software.amazon.awssdk.services.ec2.model.DeleteRouteTableRequest;
import software.amazon.awssdk.services.ec2.model.Route;
import software.amazon.awssdk.services.ec2.model.RouteTable;
import tasktree.aws.AWSDelete;

public class DeleteRouteTableRules extends AWSDelete<RouteTable> {

    public DeleteRouteTableRules(RouteTable resource) {
        super(resource);
    }

    @Override
    public void cleanup(RouteTable resource) {
        if (! isMainRouteTable(resource)) {
            deleteRoutes(resource);
            // deleteRouteTable(resource);
        } else{
            log().debug("Not deleting main route table rules {}", resource.routeTableId());
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
        log().debug("Found route {}", route);
        var gatewayId = route.gatewayId();
        if ("local".equals(gatewayId)) return;
        log().debug("Deleting route {}", route.toString());
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
        log().debug("Deleting route table rules {}", resource.routeTableId());
        var request = DeleteRouteTableRequest.builder()
                .routeTableId(resource.routeTableId())
                .build();
        newEC2Client().deleteRouteTable(request);
    }

    @Override
    protected String getResourceType() {
        return "Route Table Rules";
    }
}
