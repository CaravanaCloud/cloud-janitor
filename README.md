# Task Tree

"Task Tree" is a tool to automate complex maintenance and troubleshooting tasks in cloud computing. It's built considering the security and flexibility requirements found in operating real-world production workloads.

In cloud computing many responsibilities are shared between the provider and the consumer of services. This tool's goal is to share automations that customers and partners frequently need to build themselves, often with minimal differences.

As an example context, consider the task of cleaning up your AWS account after testing some drafts. Here's how *Task Tree* features can help:

**Resource Filtering** so that only the matched resources are deleted.

**Defensive Defaults** so that resources are only deleted when you disable the "dry run" flag.

**Dependency Resolution** so that deletes are cascaded to dependent resources.

**Logs & Reporting** so that you have a record of invocations and their results for queries and visualization.

**Flexible Runtime** so it can be used as an executable container, executable jar, native executable, github action or your preferred CI/CD tool.

**Easy to Operate** all configuration can be passed as environment variables, as well as other sources supported by quarkus.io.

Task Tree is also built to be easily extensible and contributions are most welcome! 

# Executing Task Tree

Here are a few ways you can execute Task Tree. The default task is called "marvin" and is mostly harmless. It will just print "Don't panic" to the logs.

All mentioned binaries can be found in the [project releases page](https://github.com/CaravanaCloud/task-tree/releases).

## Docker Container
```bash
docker run caravanacloud/task-tree
```

## Executable Jar
```bash
java -jar java -jar tasktree-runner.jar
```


# Getting Started using the RPM release
1. Download the latest RPM release:
   https://github.com/CaravanaCloud/task-tree/releases
2. Install it:
```
rpm -Uvh --force tt-cli/target/tasktree-cli.rpm
```
3. Add it to your PATH:
```
ln -sf /opt/tasktree/bin/tasktree /usr/local/bin/tt
```
4. Run it:
```
tt
```

# Configuring Tasks

## Quarkus Configuration
You can configure the tasks to run using a Quarkus configuration YAML file in the ($CWD/config/application.yaml).
Examples are provided below and in the "config" directory in this repository.

## Dry Run
Write tasks, such as deleting AWS resources, are protected by a dry run lock so that they'll only run if explicitly enabled.

# Task Library

## marvin
Don't panic! This is just a sample task.
Try running this one to check everything is working:
```bash
tt marvin
```

## cleanup-aws
Delete AWS resources based on a naming prefix.
Try adding this configuration to ```$CWD/config/application.yaml```:
```yaml
tt:
  task: cleanup-aws
  dryRun: true
  ocp:
    baseDomain: devcluster.openshift.com
  aws:
    region: ap-northeast-1
    cleanup:
      prefix: rhnb-
```

# Tasks Wishlist:
- Support more AWS Services on cleanup task
- Delete AWS resources by tag
- Delete AWS resources by usage
- Report/Notify usage by attribution ("chargeback")
- OpenShift Cluster Provisioning and Deployment
- Fully-automated OpenShift management (Source2Service)

# Features Wishlist:
1. Integrate with ansible
2. Kogito-defined tasks
3. CloudWatch and/or ElasticSearch/Logstash/Kibana for visualization

