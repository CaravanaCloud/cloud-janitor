# red-shooter

Debug copilot using Red Hat software development tools.

Here is what it should do:
1. Run your selected commands and debugging flow
1. Gather and evaluate the logs
1. If it matches the knowledge base, notify and/or fix the issue

Implementation considerations:
1. Use Kogito to map the user flows and match rules
1. (Optional) Use ElasticSearch/Logstash/Kibana for visuzization
