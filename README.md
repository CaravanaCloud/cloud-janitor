# Cloud Janitor

TL;DR: A convenience tool to automate operations in cloud computing, like creating clusters or cleaning up accounts.

For example, consider a 'create-cluster' task. Cloud janitor will lookup reasonable defaults, validate and backup configuration,
install dependencies, create account-level resources, create cluster-level resources, wait for health check, install plugins and verify the application. 
All that from a single invocation, using a command line like `cloud-janitor openshift-create-cluster` or the equivalent inside a container, github action or gitpod.io workspace.

Start you gitpod workspace in the link below and try `cloud-jantor --help`


[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#github.com/CaravanaCloud/cloud-janitor)

## Features

As developers and sysadmins we are used to building scripts and solutions to automate tasks. 
However, sharing those solutions can be difficult as they are executed in different machines and contexts.
This often leads to a lot of duplication of effort and code to perform the same tasks, such as creating clusters or cleaning up accounts.

**Intent Resolution**
Tasks will look up reasonable defaults, using well-known tools and patterns. 
For example, a "create-cluster" identify the cluster provider from the system configuration and generate reasonable defaults, including the cluster name. 

**Improved Security**
Sensitive outputs, such as administrator passwords, can be filtered out of output and stored in a secure location.
All sensitive operations are protected by a capability toggle and can be easily disabled for dry runs.

**Easy to Run and Configure**
All settings are exposed as environment variables, yaml and others, as supported by [Quarkus and Smallrye Config](https://quarkus.io/guides/config). Code can be executed as a command line, jar package, container image, github action. Ansible and other tools to be added in future releases.

**Resource Filtering & Resolution**
Only the matched resources, and their dependents, are affected.

**Multi-region, Multi-account and Multi-cloud**
Cloud janitor will use your configured cloud credentials an any extra configuration 
to repeat actions across different contexts, such as cleaning up multiple cloud accounts.

**Logs & Reporting**
Keep a record of invocations and their results for queries, visualization and audits.

**Rate Limiting**
Respect API limits and throttling with backoffs and timeouts.

Cloud Janitor is also built to be easily extensible and contributions are most welcome!

## Cloud Janitor on GitPod

This repository is ready to launch on gitpod, containing all configuration to start your development instance with Java, Quarkus, AWS CLI and all tools that you need to code.

Once your workspace is running use the command ```aws configure``` to setup your AWS account authentication. You can also use gitpod environment variables or dotfiles for that purpose. Check that authentication is correct with the command ```aws sts get-caller-identity```

You can run cloud-janitor in development mode by executing ```quarkus dev```, ```mvn quarkus:dev``` or simply running the class ```cj.Application```. The default task is called "marvin" and is mostly harmless. It will just log the most important reminder: "Don't panic" :) 

Ready to start janitoring? Check the ```LearningFromTests``` class for some examples.

## Contribute to Cloud Janitor

Yes, we need your help and would love to work with you :)

Besides that, contributing to cloud janitor is a great way to learn Java and AWS. 

We are glad to help you get started and build upon this repository as you'd like.

If you´d like to get in touch directly, besides here on github, you can find us on twitter.com/caravanacloud.

## Executing Cloud Janitor

Here are multiple ways you can execute this project. 

All mentioned binaries can be found in the [project releases page](https://github.com/CaravanaCloud/cloud-janitor/releases).

### Homebrew (todo)
Install it:
```
brew tap caravanacloud/homebrew-tap
brew install cloud-janitor
```
Run it:
```
cloud-janitor
```

### From sources (dev mode)
Using the maven wrapper:
```
./mvnw quarkus:dev
```
Using the Quarkus CLI:
```
quarkus dev
```
Using your IDE, run the `cj.Main` class.

### Executable Jar
```bash
java -jar java -jar cloud-janitor.jar
```

### Docker Container
```bash
docker run --pull=always caravanacloud/cloud-janitor 
```

### Github Action
```
- name: Cloud Janitor
  uses: CaravanaCloud/cloud-janitor@v1.7.3
```

## Configuring Cloud Janitor

Cloud janitor can be configured to perform wide variety of tasks, from creating cluster to translating videos.
Take a look at our configuration samples in the [config](./config) directory in this repository.
Renaming any configuration file to `application.yaml` will make it the default configuration and executed by cloud-janitor.


