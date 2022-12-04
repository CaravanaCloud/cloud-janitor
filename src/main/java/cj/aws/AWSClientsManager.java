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

import static cj.aws.AWSInput.identity;
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
            log.debug("Creating new AWSClients for {} - {}", identity, region);
            clients = clientsInstance.get();
            clients.setClientIdentity(key);
            clientsById.put(key, clients);
        }else {
            log.trace("Using cached AWSClients for {} - {}", identity, region);
        }
        return clients;
    }

    private Region getCLIRegion() {
        var exec = tasks.exec("aws", "configure", "get", "region");
        var regionName = exec.stdout().trim();
        var region = ofNullable(regionName).map(Region::of).orElse(null);
        return region;
    }

    private Region getRegion(AWSConfiguration config) {
        var region = defaultRegion(config);
        if (region == null){
            region = getCLIRegion();
        }else {
            log.debug("No region found, fallback to us-east-1.");
            region = Region.US_EAST_1;
        }
        return region;
    }

    public Region defaultRegion() {
        return defaultRegion(config.aws());
    }
    protected static Region defaultRegion(AWSConfiguration config){
        var defaultRegion = config.defaultRegion();
        if (defaultRegion != null){
            return Region.of(defaultRegion);
        }
        return Region.US_EAST_1;
    }

    public AWSIdentity defaultIdentity() {
        var ids = identities();
        if (ids.isEmpty()) return null;
        var id = ids.get(0);
        return id;
    }

    private synchronized List<? extends AWSIdentity> identities() {
        if (awsIdentities == null){
            awsIdentities = tasks.submit(loadIds.get())
                    .outputList(AWSOutput.Identities, AWSIdentity.class);
            if (awsIdentities != null && awsIdentities.size() > 0){
                log.debug("Loaded {} AWS identities", awsIdentities.size());
            }else{
                log.warn("No AWS identities found.");
                awsIdentities = List.of();
            }
        }
        return awsIdentities;
    }


}
