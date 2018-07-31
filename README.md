# rabbitmq-metric-agent
## Introduction
RabbitMQ metric agent is a RabbitMQ metric publisher plugin for [new relic](https://newrelic.com/) digital intelligent platform. This agent use the [New Relic Java Plugin Agent SDK](https://github.com/newrelic-platform/metrics_publish_java). User can create a jar and host it anywhere as an agent. Then agent will send data to new relic platform. 
Following metric values are possible to track using this agent. 
1) Total message count of all queues
2) Total ready message count in all queues
3) Redelivered message count in all queues
4) Return unroutable message count in all queues
5) Un-Acknowledge message count in all queues
6) Message published rate
7) Message delivered rate
8) Node status (if node is running graph value will be 1 and otherwise 0)
9) Memory usage by the node as a percentage (used_memory/total_memory_limit)
10) Total memory usage by the node (present value)

User can change any metric by changing the code.After successfully host the plugin (agent) user can view statistics on new relic plugin dashboard (user may need to configure them)

## Configurations
To use this plugin user need to add following configurations
### 1. plugin.json
Create a file called plugin.json under config directory
```
{
  "agents": [
    {
      "name" : "AGENT_NAME",
      "host" : "RABBITMQ_HOST",
      "username" : "ADMIN_USERNAME",
      "password" : "ADMIN_PASSWORD"
    }
  ]
}
```
### 2. newrelic.json
Create a file called newrelic.json under config directory
```
{
  "license_key": "YOUR_LICENSE_KEY_HERE",
  "log_level": "LOG_LEVEL",
  "log_file_name": "LOG_FILE_NAME",
  "log_file_path": "LOG_PATH"
}
```

## How to run
Create a jar and run jar using command `java -jar JAR_FILE.jar`. Make sure config directory and jar file are in the same directory.

Please go through our [wiki page](https://github.com/ChanakaR/rabbitmq-metric-agent/wiki) for more instructions.
