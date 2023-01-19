package cj.aws;

import cj.*;
import cj.aws.sts.AWSLoadIdentitiesTask;
import cj.aws.sts.DefaultIdentity;
import org.slf4j.Logger;
import software.amazon.awssdk.regions.Region;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class AWSClientsManager {
    @Inject
    Logger log;


    @Inject
    Tasks tasks;

    @Inject
    Instance<AWSClients> clientsInstance;

    @Inject
    CJConfiguration config;

    @Inject
    Instance<AWSLoadIdentitiesTask> loadIds;

    @Inject
    Shell shell;
    Map<AWSClientIdentity, AWSClients> clientsById = new HashMap<>();

    List<AWSIdentity> awsIdentities = null;
    private AWSIdentity defaultIdentity;

    private Region defaultRegion;
    private final Map<AWSIdentity, AWSIdentityInfo> infoMap = new HashMap<>();


    public AWSClients of(AWSIdentity identity, Region region) {
        if (region == null){
            region = defaultRegion(config.aws());
            log.warn("Requested AWS clients without region. Using default region {}", region);
        }
        var key = AWSClientIdentity.of(identity, region);
        var clients= clientsById.get(key);
        if (clients == null ){
            log.trace("Creating new AWSClients for {} - {}", identity, region);
            clients = clientsInstance.get();
            //TODO: Check repeat identity poassing clients.setCredentialsProvider(identity.toCredentialsProvider(sts));
            clients.setRegion(region);
            clientsById.put(key, clients);
        }else {
            log.trace("Using cached AWSClients for {} - {}", identity, region);
        }
        return clients;
    }

    private Region awsCLIRegion() {
        var exec = shell.exec("aws", "configure", "get", "region");
        var regionName = exec.stdout().trim();
        @SuppressWarnings("UnnecessaryLocalVariable")
        var region = Optional.of(regionName)
                .map(Region::of)
                .orElse(null);
        return region;
    }

    public Region defaultRegion() {
        Region region = defaultRegion(config.aws());
        return region;
    }
    protected synchronized Region defaultRegion(AWSConfiguration config){
        if (defaultRegion != null)
            return defaultRegion;
        var configRegion = config.defaultRegion();
        if (configRegion != null){
            defaultRegion = Region.of(configRegion);
            return defaultRegion;
        }
        var envRegion = System.getenv("AWS_REGION");
        if (envRegion != null){
            defaultRegion = Region.of(envRegion);
            return defaultRegion;
        }
        var cliRegion = awsCLIRegion();
        if (cliRegion != null){
            defaultRegion = cliRegion;
            return defaultRegion;
        }
        defaultRegion =  Region.US_EAST_1;
        return defaultRegion;
    }

    public synchronized AWSIdentity defaultIdentity() {
        if (defaultIdentity != null)
            return defaultIdentity;
        var ids = identities();
        if (ids.isEmpty()) return null;
        var id = ids.get(0);
        return id;
    }

    public synchronized List<? extends AWSIdentity> identities() {
        if (awsIdentities == null){
            awsIdentities = tasks.submitTask(loadIds.get())
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

    public void defaultIdentity(DefaultIdentity identity) {
        this.defaultIdentity = identity;
    }

    public AWSIdentityInfo putInfo(AWSIdentity id, String accountId, String accountAlias, String userARN) {
        return putInfo(id, AWSIdentityInfo.of(userARN, accountId, accountAlias));
    }
    public AWSIdentityInfo putInfo(AWSIdentity id, AWSIdentityInfo info) {
        return infoMap.put(id, info);
    }
    public AWSIdentityInfo getInfo(AWSIdentity id) {
        var info = infoMap.get(id);
        if (info == null){
            log.warn("Failed to load user info for {}", id);
        }
        return info;
    }






}
