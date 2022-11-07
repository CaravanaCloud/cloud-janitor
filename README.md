# Cloud Janitor

Cloud Janitor is a tool to automate complex maintenance and troubleshooting tasks in cloud computing. It's built considering the security and flexibility requirements found in operating real-world production workloads.

In cloud computing, many responsibilities are shared between the provider and the consumer of services. This tool's goal is to share automations that customers and partners frequently need to build themselves, often with minimal differences.

A key automation in this context is "cleaning up" an AWS account, for example. That will be a bit different in each environment, but it's all about filtering and deleting (or replacing) resources. Here's how our features can help:

**Easy to Run and Configure** - All settings are exposed as environment variables and code can run as executable, jar, container, github action or your preferred CI/CD tool.

**Defensive Defaults** - No writes are executed unless the "dry run" setting is explicitly disabled.

**Resource Filtering** - Only the matched resources, and their dependents, are deleted.

**Simple to customize and extend** - Take custom actions, such as scale down or notify, instead of delete.

**Logs & Reporting** - Keep a record of invocations and their results for queries, visualization and audits.

**Rate Limiting** - API limits and throttling are respected with backoffs.

Cloud Janitor is also built to be easily extensible and contributions are most welcome!

# Cloud Janitor on GitPod

This repository contains the gitpod configuration to start your development instance with Java, Quarkus, AWS CLI and all tools that you need to code.

Start you gitpod worspace by visiting https://gitpod.io/#github.com/CaravanaCloud/cloud-janitor

Once your workspace is running use the command ```aws configure``` to setup your AWS account authentication. You can also use gitpod environment variables or dotfiles for that purpose. Check that authentication is correct with the command ```aws sts get-caller-identity```

You can run cloud-janitor in development mode by executing ```quarkus dev```, ```mvn quarkus:dev``` or simply running the class ```cj.Application```. The default task is called "marvin" and is mostly harmless. It will just log the most important reminder: "Don't panic" :) 

Ready to start janitoring? Check the ```LearningFromTests``` class for some examples.

# Contribute to Cloud Janitor

Yes, we need your help and would love to work with you :)

Besides that, contributing to cloud janitor is a great way to learn Java and AWS. We are glad to help you gest started and build upon this repository as you'd like.

We favor asynchronous communication, so the best ways to get in touch would be through our [Discord Channel](https://discord.gg/TGzJK4rmRR)

Also, we have a weekly office-hours session to work together, do join the [event on google calendar](https://calendar.google.com/event?action=TEMPLATE&tmeid=NDVxZGo5bTAwYWdqNmRwMGNkc2ZqOHF0cmtfMjAyMjA3MjZUMTYwMDAwWiBqdWxpb0BjYXJhdmFuYS5jbG91ZA&tmsrc=julio%40caravana.cloud&scp=ALL)


# Executing Cloud Janitor

Here are multiple ways you can execute this project. 

All mentioned binaries can be found in the [project releases page](https://github.com/CaravanaCloud/cloud-janitor/releases).


## Homebrew
Install it:
```
brew tap caravanacloud/homebrew-tap
brew install cloud-janitor
```
Run it:
```
cloud-janitor
```

## From sources (dev mode)
Using the maven wrapper:
```
./mvnw quarkus:dev
```
Using the Quarkus CLI:
```
quarkus dev
```
Using your IDE, run the `cj.Main` class.

## Executable Jar
```bash
java -jar java -jar cloud-janitor.jar
```

## Docker Container
```bash
docker run --pull=always caravanacloud/cloud-janitor 
```

## Github Action
```
- name: Cloud Janitor
  uses: CaravanaCloud/cloud-janitor@v1.0.20220324161817
```
