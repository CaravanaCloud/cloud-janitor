package cloudjanitor.aws.ec2;
import software.amazon.awssdk.services.ec2.model.DeleteRouteRequest;
import software.amazon.awssdk.services.ec2.model.DeleteRouteTableRequest;
import software.amazon.awssdk.services.ec2.model.Route;
import software.amazon.awssdk.services.ec2.model.RouteTable;
import cloudjanitor.aws.AWSCleanup;

public class DeleteRouteTableRules extends AWSCleanup<RouteTable> {

    public DeleteRouteTableRules(RouteTable resource) {
        super(resource);
    }

    @Override
    public void cleanup(RouteTable resource) {
        if (! isMainRouteTable(resource)) {
            try {
                deleteRoutes(resource);
            }catch (Exception e){
                log().error("Fail to delete Route Table Rules for {}", resource.routeTableId());
                log().error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
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
        log().debug("Deleting route {}", route);
        var gatewayId = route.gatewayId();
        if ("local".equals(gatewayId)) {
            log().debug("Refusing to delete local route");
        };
        var builder = DeleteRouteRequest
                .builder()
                .routeTableId(resource.routeTableId());
        if (route.destinationCidrBlock() != null) {
            log().debug("Using destination cidr block to delete route {}", route);
            builder.destinationCidrBlock(route.destinationCidrBlock());
        }else if (route.destinationPrefixListId() != null) {
            log().debug("Using destination prefix list id to delete route {}", route);
            builder.destinationPrefixListId(route.destinationPrefixListId());
        }
        var request = builder.build();
        try {
            aws().newEC2Client(getRegion()).deleteRoute(request);
        }catch (Exception ex){
            log().error("Failed to delete route {}", route);
            log().error(ex.getMessage(),ex);
            throw new RuntimeException(ex);
        }
    }

    private void deleteRouteTable(RouteTable resource) {
        log().debug("Deleting route table rules {}", resource.routeTableId());
        var request = DeleteRouteTableRequest.builder()
                .routeTableId(resource.routeTableId())
                .build();
        aws().newEC2Client(getRegion()).deleteRouteTable(request);
    }

    @Override
    protected String getResourceType() {
        return "Route Table Rules";
    }
}
