package cj.aws.ec2.delete;

import cj.Input;
import cj.aws.AWSInput;
import cj.aws.AWSWrite;
import software.amazon.awssdk.services.ec2.model.DeleteRouteRequest;
import software.amazon.awssdk.services.ec2.model.DeleteRouteTableRequest;
import software.amazon.awssdk.services.ec2.model.Route;
import software.amazon.awssdk.services.ec2.model.RouteTable;

import javax.enterprise.context.Dependent;

@Dependent
public class DeleteRouteTableRules extends AWSWrite {
    @Override
    public void apply() {
        var rtb = getInput(AWSInput.targetRouteTable, RouteTable.class);
        if (! isMainRouteTable(rtb)) {
            try {
                deleteRoutes(rtb);
            }catch (Exception e){
                error("Fail to delete Route Table Rules for {}", rtb.routeTableId());
                error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
            // deleteRouteTable(resource);
        } else{
            debug("Not deleting main route table rules {}", rtb.routeTableId());
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
        debug("Deleting route {}", route);
        var gatewayId = route.gatewayId();
        if ("local".equals(gatewayId)) {
            debug("Refusing to delete local route");
            return;
        }
        var builder = DeleteRouteRequest
                .builder()
                .routeTableId(resource.routeTableId());
        if (route.destinationCidrBlock() != null) {
            debug("Using destination cidr block to delete route {}", route);
            builder.destinationCidrBlock(route.destinationCidrBlock());
        }else if (route.destinationPrefixListId() != null) {
            debug("Using destination prefix list id to delete route {}", route);
            builder.destinationPrefixListId(route.destinationPrefixListId());
        }
        var request = builder.build();
        try {
            aws().ec2().deleteRoute(request);
        }catch (Exception ex){
            error("Failed to delete route {}", route);
            error(ex.getMessage(),ex);
            throw new RuntimeException(ex);
        }
    }

    private void deleteRouteTable(RouteTable resource) {
        debug("Deleting route table rules {}", resource.routeTableId());
        var request = DeleteRouteTableRequest.builder()
                .routeTableId(resource.routeTableId())
                .build();
        aws().ec2().deleteRouteTable(request);
    }
}
