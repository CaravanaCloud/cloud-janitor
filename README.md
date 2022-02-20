# Task Tree

A set of automations for resource management and debugging, focused on cloud and kubernetes.

Here is what it should do:
1. Run your selected commands and debugging flow
1. Gather and evaluate the logs
1. If it matches the knowledge base, notify and/or fix the issue

# Initial features:
- Report/Delete resources created by OCP installer by name
- Report/Delete resources created by OCP installer by tag
- Report/Delete underutilized resources
- Report/Notify usage by attribution ("chargeback")

# Implementation considerations:
1. (Idea) Integrate with ansible
1. (Idea) Use Kogito to map the user flows and match rules
1. (Idea) Use CloudWatch and/or ElasticSearch/Logstash/Kibana for visuzization
