package cj.aws;

import cj.Configuration;
import cj.Tasks;
import cj.aws.sts.AWSLoadIdentitiesTask;
import org.slf4j.Logger;
import software.amazon.awssdk.regions.Region;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

@ApplicationScoped
public class AWSClientsManager {
    @Inject
    Logger log;


    @Inject
    Tasks tasks;

    @Inject
    Instance<AWSClients> clientsInstance;

    @Inject
    Configuration config;

    @Inject
    Instance<AWSLoadIdentitiesTask> loadIds;
    Map<AWSClientIdentity, AWSClients> clientsById = new HashMap<>();

    List<AWSIdentity> awsIdentities = null;


    public AWSClients of(AWSIdentity identity, Region region) {
        if (region == null){
            region = defaultRegion(config.aws());
        }
        var key = AWSClientIdentity.of(identity, region);
        var clients= clientsById.get(key);
        if (clients == null ){
            log.trace("Creating new AWSClients for {} - {}", identity, region);
            clients = clientsInstance.get();
            clients.setClientIdentity(key);
            clientsById.put(key, clients);
        }else {
            log.trace("Using cached AWSClients for {} - {}", identity, region);
        }
        return clients;
    }

    private Region awsCLIRegion() {
        var exec = tasks.exec("aws", "configure", "get", "region");
        var regionName = exec.stdout().trim();
        @SuppressWarnings("UnnecessaryLocalVariable")
        var region = ofNullable(regionName).map(Region::of).orElse(null);
        return region;
    }

    public Region defaultRegion() {
        return defaultRegion(config.aws());
    }
    protected Region defaultRegion(AWSConfiguration config){
        var defaultRegion = config.defaultRegion();
        if (defaultRegion != null){
            return Region.of(defaultRegion);
        }
        var cliRegion = awsCLIRegion();
        if (cliRegion != null){
            return cliRegion;
        }
        return Region.US_EAST_1;
    }

    public AWSIdentity defaultIdentity() {
        var ids = identities();
        if (ids.isEmpty()) return null;
        var id = ids.get(0);
        return id;
    }

    public synchronized List<? extends AWSIdentity> identities() {
        if (awsIdentities == null){
            awsIdentities = tasks.submit(loadIds.get())
                    .outputList(AWSOutput.Identities, AWSIdentity.class);
            if (awsIdentities != null && awsIdentities.size() > 0){
                log.info("Loaded {} AWS identities", awsIdentities.size());
            }else{
                log.warn("No AWS identities found.");
                awsIdentities = List.of();
            }
        }
        return awsIdentities;
    }


    public List<Region> regions() {
        return config.aws().regionsList().stream().map(Region::of).toList();
    }
}
