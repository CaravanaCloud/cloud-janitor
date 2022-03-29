# Task Tree

"Task Tree" is an automation tool to help developers build and share maintenance
and troubleshooting tasks. It is built to:

* Save time and money for Red Hat customers & partners.
* Be safe to execute, in prod.
* Be easy to run and customize.
* Provide correct filtering and dependency resolution
* Report results effectively


# Getting Started using the JAR release
1. Download the latest JAR release:
https://github.com/CaravanaCloud/task-tree/releases
2. Run it:
```
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

