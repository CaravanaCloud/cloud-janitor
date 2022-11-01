package cj.aws.ec2;

import cj.Input;
import cj.aws.AWSWrite;
import software.amazon.awssdk.services.ec2.model.*;

import javax.enterprise.context.Dependent;


@Dependent
public class DeleteRouteTable extends AWSWrite {


    @Override
    public void apply() {
        RouteTable resource = getInput(Input.aws.routeTable, RouteTable.class);
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
            try {
                aws().ec2().deleteRoute(request);
                debug("Deleted route {} / {}", resource.routeTableId(), route.toString());
            }catch (Ec2Exception ex){
                error("Failed to delete route {} / {}", resource.routeTableId(), route.toString());
                fail(ex);
            }
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
