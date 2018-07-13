package com.orangehrm.rabbitmqagent;


import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.ConfigurationException;

import java.util.Map;

public class RabbitMQAgentFactory extends AgentFactory {

    @Override
    public Agent createConfiguredAgent(Map<String, Object> map) throws ConfigurationException {
        String name = (String) map.get("name");
        String host = (String) map.get("host");

        if (name == null || host == null) {
            throw new ConfigurationException("'name' and 'host' cannot be null. Do you have a 'config/plugin.json' file?");
        }

        return new RabbitMQAgent(name,host);
    }
}
