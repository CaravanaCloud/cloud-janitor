package cj.aws.ec2.delete;

import cj.Output;
import cj.aws.AWSInput;
import cj.aws.AWSWrite;
import cj.aws.ec2.filter.FilterRouteTables;
import cj.spi.Task;
import software.amazon.awssdk.services.ec2.model.RouteTable;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@Dependent
public class DeleteRouteTables extends AWSWrite {
    @Inject
    FilterRouteTables filterRouteTables;

    @Inject
    Instance<DeleteRouteTable> delRouteTable;

    @Override
    public Task getDependency() {
        return delegate(filterRouteTables);
    }

    @Override
    public void apply() {
        var routeTables = filterRouteTables.outputList(Output.aws.RouteTablesMatch, RouteTable.class);
        routeTables.forEach(this::deleteRouteTable);
    }

    private void deleteRouteTable(RouteTable routeTable) {
        var delRoute = delRouteTable.get().withInput(AWSInput.routeTable, routeTable);
        submit(delRoute);
    }
}
