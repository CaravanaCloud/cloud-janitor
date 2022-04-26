# Cloud Janitor

Cloud Janitor is a tool to automate complex maintenance and troubleshooting tasks in cloud computing. It's built considering the security and flexibility requirements found in operating real-world production workloads.

In cloud computing many responsibilities are shared between the provider and the consumer of services. This tool's goal is to share automations that customers and partners frequently need to build themselves, often with minimal differences.

As an example context, consider the task of cleaning up your AWS account after testing some drafts. Here's how our features can help:

**Resource Filtering** so that only the matched resources are deleted.

**Defensive Defaults** so that resources are only deleted when you disable the "dry run" flag.

**Dependency Resolution** so that deletes are cascaded to dependent resources.

**Logs & Reporting** so that you have a record of invocations and their results for queries and visualization.

**Flexible Runtime** so it can be used as an executable container, executable jar, native executable, github action or your preferred CI/CD tool.

**Rate Limiting** so that API throttling and limits are respected.

**Easy to Operate** all configuration can be passed as environment variables, as well as other sources supported by quarkus.io.

Cloud Janitor is also built to be easily extensible and contributions are most welcome! Join our Discord chat at https://caravana.cloud/cloud-janitor

# Executing Cloud Janitor

Here are a few ways you can execute this project. 
The default task is called "marvin" and is mostly harmless. It will just print "Don't panic" to the logs.

All mentioned binaries can be found in the [project releases page](https://github.com/CaravanaCloud/cloud-janitor/releases).

## Docker Container
```bash
docker run --pull=always caravanacloud/cloud-janitor 
```

## Github Action
```
- name: Cloud Janitor
  uses: CaravanaCloud/cloud-janitor@v1.0.20220324161817
```

## Executable Jar
```bash
java -jar java -jar cloud-janitor.jar
```

## RPM Package
Install it:
```
rpm -Uvh --force cloud-janitor.rpm
```
Add it to your PATH:
```
sudo ln -sf /opt/cloud-janitor/bin/cloud-janitor /usr/local/bin/cj
```
Run it:
```
cj
```

## From sources (dev mode)
```
./mvnw quarkus:dev
```

# Configuring Tasks

Here are a few samples demonstrating how to use environment variables to configure tasks.

## marvin
Don't panic! This is just a sample task.
Try running this one to check everything is working:
```
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

# Wishlist:
- Support more/all AWS Services on cleanup task
- Improve test coverage
- Report/Delete AWS resources by tag
- Report/Delete AWS resources by usage
- Parallel Tasks
- Waiter Tasks
- Shell Tasks
- Cross-Language Tasks through GraalVM / JSR 223
- Report/Notify usage by attribution ("chargeback")
- OpenShift Cluster Provisioning and Deployment
- Fully-automated OpenShift management (Source2Service)


# Features Wishlist:
1. Ansible integration
2. Kogito-defined tasks
3. CloudWatch and/or ElasticSearch/Logstash/Kibana for visualization

# Badges
![Tests](https://github.com/CaravanaCloud/cloud-janitor/workflows/test-prs-to-main/badge.svg)
![Coverage](.github/badges/jacoco.svg)
