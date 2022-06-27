package cloudjanitor.aws.ec2;

import cloudjanitor.Input;
import cloudjanitor.aws.AWSFilter;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.ec2.model.DescribeRouteTablesRequest;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.RouteTable;

import javax.enterprise.context.Dependent;
import java.util.List;

import static cloudjanitor.Input.AWS.TargetVpcId;

@Dependent
public class CleanupRouteTables extends AWSFilter {
    @Override
    public void runSafe() {
        var routeTables = filterRouteTables();
        routeTables.forEach(this::deleteRouteTable);
    }

    private List<RouteTable> filterRouteTables() {
        var req = DescribeRouteTablesRequest
                .builder()
                .filters(Filter.builder()
                        .name("vpc-id")
                        .values(inputString(TargetVpcId))
                        .build())
                .build();
        var rtbs = aws()
                .getEC2Client()
                .describeRouteTables(req)
                .routeTables();
        log().info("Found RTBs {} {}", rtbs.size(), rtbs);
        return rtbs;
    }

    private void deleteRouteTable(RouteTable routeTable) {
        //TODO
        log().info("DELETING RTB " + routeTable.routeTableId());
    }
}
