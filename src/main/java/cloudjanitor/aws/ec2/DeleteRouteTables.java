package cloudjanitor.aws.ec2;

import cloudjanitor.Input;
import cloudjanitor.Output;
import cloudjanitor.aws.AWSWrite;
import cloudjanitor.aws.AWSWrite;
import cloudjanitor.spi.Task;
import software.amazon.awssdk.services.ec2.model.DescribeRouteTablesRequest;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.RouteTable;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;

import static cloudjanitor.Input.AWS.TargetVpcId;

@Dependent
public class DeleteRouteTables extends AWSWrite {
    @Inject
    FilterRouteTables filterRouteTables;

    @Inject
    Instance<DeleteRouteTable> delRouteTable;

    @Override
    public List<Task> getDependencies() {
        return List.of(
                filterRouteTables.withInput(TargetVpcId, inputString(TargetVpcId)));
    }

    @Override
    public void runSafe() {
        var routeTables = filterRouteTables.outputList(Output.AWS.RouteTablesMatch, RouteTable.class);
        routeTables.forEach(this::deleteRouteTable);
    }


    private void deleteRouteTable(RouteTable routeTable) {
        var delRoute = delRouteTable.get().withInput(Input.AWS.RouteTable, routeTable);
        runTask(delRoute);
    }
}
