package com.orangehrm.rabbitmq;


import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import java.util.Map;

public class RabbitMQAgentFactory extends AgentFactory {

    @Override
    public Agent createConfiguredAgent(Map<String, Object> map){
        String name = (String) map.get("name");
        String version = (String) map.get("version");
        return new RabbitMQAgent(name,version);
    }
}
