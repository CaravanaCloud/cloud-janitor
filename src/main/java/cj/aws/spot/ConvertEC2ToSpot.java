package cj.aws.spot;

import cj.Utils;
import cj.aws.AWSWrite;
import software.amazon.awssdk.services.ec2.model.*;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

import static cj.Input.aws.targetInstanceId;
import static cj.Utils.msToStr;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;

@Named("convert-to-spot")
@Dependent
public class ConvertEC2ToSpot extends AWSWrite {
    @Override
    public void apply() {
        var targetInstanceIdInput = inputString(targetInstanceId);
        if (targetInstanceIdInput.isEmpty()) {
            error("Instance id not provided");
            return;
        }
        var targetInstanceId = targetInstanceIdInput.get();
        debug("Converting instance to spot: {}", targetInstanceId);
        stopInstance(targetInstanceId);
        var imageId = createImage(targetInstanceId);
        var newInstanceId = runMoreLikeThis(targetInstanceId, imageId);
        // does not work for ENI 0 swapENIs(targetInstanceId, newInstanceId);
        debug("Instance {} replaced with {}, check application health.", targetInstanceId, newInstanceId);

    }

    private void swapENIs(String targetInstanceId, String newInstanceId) {
        var targetInstance = getInstance(targetInstanceId);
        var newInstance = getInstance(newInstanceId);
        var targetENI = targetInstance.networkInterfaces().get(0).networkInterfaceId();
        var newENI = newInstance.networkInterfaces().get(0).networkInterfaceId();
        debug("Swapping ENIs {} and {}", targetENI, newENI);
        var targetENIAttachment = getENIAttachment(targetENI);
        var newENIAttachment = getENIAttachment(newENI);
        var targetAttachmentId = targetENIAttachment.attachmentId();
        var newAttachmentId = newENIAttachment.attachmentId();
        debug("AttachmentIds {} and {}", targetAttachmentId, newAttachmentId);
        detachENI(targetAttachmentId);
        detachENI(newAttachmentId);
        attachENI(newInstanceId, targetENI);
        attachENI(targetInstanceId, newENI);
    }

    private void attachENI(String instanceId, String eniId) {
        var ec2 = aws().ec2();
        debug("Attaching ENI {} to {}", eniId, instanceId);
        ec2.attachNetworkInterface(AttachNetworkInterfaceRequest.builder()
                .instanceId(instanceId)
                .networkInterfaceId(eniId)
                .build());
    }

    private void detachENI(String targetAttachmentId) {
        var ec2 = aws().ec2();
        var detachRequest = DetachNetworkInterfaceRequest.builder()
                .attachmentId(targetAttachmentId)
                .force(true)
                .build();
        debug("Detaching ENI {}", targetAttachmentId);
        ec2.detachNetworkInterface(detachRequest);
    }

    private NetworkInterfaceAttachment getENIAttachment(String targetENI) {
        var ec2 = aws().ec2();
        var targetENIAttachment = ec2.describeNetworkInterfaces(
                DescribeNetworkInterfacesRequest.builder()
                        .networkInterfaceIds(targetENI)
                        .build()
        ).networkInterfaces().get(0).attachment();
        return targetENIAttachment;
    }

    private Instance getInstance(String targetInstanceId) {
        var ec2 = aws().ec2();
        var targetInstance = ec2.describeInstances(
                DescribeInstancesRequest.builder()
                        .instanceIds(targetInstanceId)
                        .build()
        ).reservations().get(0).instances().get(0);
        debug("Got instance {}", targetInstance);
        return targetInstance;
    }

    private void waitInstanceStopped(String instanceId) {
        await().atMost(getConfig().largeAtMostTimeoutMs(), MILLISECONDS)
                .pollInterval(getConfig().largePollIntervalMs(), MILLISECONDS)
                .until(() -> instanceState(instanceId, "STOPPED"));
    }


    private void stopInstance(String targetInstanceId) {
        if(! canStop(targetInstanceId)) {
            debug("Instance already stopped");
            return;
        }
        debug("Stopping instance: {}", targetInstanceId);
        var ec2 = aws().ec2();
        var req = StopInstancesRequest.builder().instanceIds(targetInstanceId).build();
        ec2.stopInstances(req);
        debug("Waiting for instance {} to be stopped.", targetInstanceId);
        waitInstanceStopped(targetInstanceId);
    }

    private boolean canStop(String targetInstanceId) {
        var state = lookupInstanceStateName(targetInstanceId);
        boolean canStop = state.equalsIgnoreCase("RUNNING");
        return canStop;
    }

    private String runMoreLikeThis(String targetInstanceId, String imageId) {
        debug("Running one more {} from {}", targetInstanceId, imageId);
        var ec2 = aws().ec2();
        var descReq = DescribeInstancesRequest.builder()
                .instanceIds(targetInstanceId)
                .build();
        var descInstance = ec2.describeInstances(descReq)
                .reservations()
                .stream().flatMap(r -> r.instances().stream())
                .findAny();
        if (descInstance.isEmpty()){
            fail("Could not find instance "+targetInstanceId);
            return targetInstanceId;
        }
        var instance = descInstance.get();
        var instanceType = instance.instanceType();
        var subnetId = instance.subnetId();
        var securityGroups = instance.securityGroups();
        var securityGroupIds = securityGroups.stream().map( sg -> sg.groupId()).toList();
        var tags = instance.tags().stream().filter(t -> ! t.value().isEmpty()).toList();
        var tagSpec = TagSpecification.builder()
                .resourceType("instance")
                .tags(tags)
                .build();
        var privateIpAddress = instance.privateIpAddress();
        var eni = InstanceNetworkInterfaceSpecification.builder()
                .deviceIndex(0)
                .subnetId(subnetId)
                .groups(securityGroupIds)
                .build();
        var runReq = RunInstancesRequest.builder()
                .imageId(imageId)
                .instanceType(instanceType)
                .networkInterfaces(eni)
                .tagSpecifications(tagSpec)
                .minCount(1)
                .maxCount(1)
                .build();
        var runInstances = ec2.runInstances(runReq).instances();
        if (runInstances.isEmpty()){
            fail("Could not run instance");
            return targetInstanceId;
        }
        var newInstance = runInstances.get(0);
        var newInstanceId = newInstance.instanceId();
        debug("Waiting for instance {} to be available", newInstanceId);
        waitInstanceRunning(newInstanceId);
        return newInstanceId;
    }

    private String createImage(String targetInstance) {
        var ec2 = aws().ec2();
        var timeStamp = Utils.nowStamp();
        var imageName = targetInstance+"_"+timeStamp;
        var req = CreateImageRequest.builder()
                .instanceId(targetInstance)
                .noReboot(true)
                .name(imageName)
                .build();
        var imageId = ec2.createImage(req).imageId();
        debug("Created image {} from instance {}.", imageId, targetInstance);
        awaitImageAvailable(imageId);
        debug("Image available. Creating replacement instance");
        return imageId;
    }

    private void awaitImageAvailable(String imageId) {
        var atMost = getConfig().largeAtMostTimeoutMs();
        var pollInterval = getConfig().largePollIntervalMs();
        debug("Awaiting for {} to be available ({}|{}).", imageId, msToStr(pollInterval), msToStr(atMost));
        await().atMost(atMost, MILLISECONDS)
                .pollInterval(pollInterval, MILLISECONDS)
                .until(() -> imageIsAvailable(imageId));
    }

    private boolean imageIsAvailable(String imageId) {
        var ec2 = aws().ec2();
        var req = DescribeImagesRequest.builder()
                .imageIds(imageId)
                .build();
        var images = ec2.describeImages(req).images();
        if (images.size() == 1){
            var image = images.get(0);
            if (image.imageId().equals(imageId)){
                var state = image.state();
                debug("Image {} state is {}", imageId, state.name());
                if (ImageState.AVAILABLE.equals(state)){
                    return true;
                }
            }
        }
        debug("Image {} not yet ready", imageId);
        return false;
    }




    private void waitInstanceRunning(String newInstanceId) {
        await().atMost(getConfig().largeAtMostTimeoutMs(), MILLISECONDS)
                .pollInterval(getConfig().largePollIntervalMs(), MILLISECONDS)
                .until(() -> instanceState(newInstanceId, "RUNNING"));
    }

    private String lookupInstanceStateName(String instanceId){
        var req = DescribeInstancesRequest.builder()
                .instanceIds(instanceId)
                .build();
        var describeInstances = aws().ec2().describeInstances(req);
        var instance = describeInstances.reservations()
                .stream()
                .flatMap( rs -> rs.instances().stream())
                .filter( i -> i.instanceId().equals(instanceId))
                .findAny();
        if (instance.isPresent()){
            var i = instance.get();
            var state = i.state().name().name();
            debug("Instance {} state is currently {}", instanceId, state);
            return state;
        }
        return null;
    }

    private boolean instanceState(String instanceId, String targetState) {
        var instanceState = targetState != null
                && targetState.equals(lookupInstanceStateName(instanceId));
        return instanceState;
    }

    


}
