package com.orangehrm.rabbitmq;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.processors.EpochProcessor;
import com.newrelic.metrics.publish.processors.Processor;

public class RabbitMQAgent extends Agent{

    private static final String GUID = "com.orangehrm.rabbitmq";

    private String name;
    private Processor memoryUsageProcessor = new EpochProcessor();
    private RabbitMQRESTClient rabbitMQRESTClient;

    public RabbitMQAgent(String GUID, String version,String host, String username, String password) {
        super(GUID,version);
        this.rabbitMQRESTClient = new RabbitMQRESTClient(host,username,password);
        this.name = name;
    }

    @Override
    public void pollCycle() {
        int totalMemory = 16572;
        int totalCounts = 1234;
        reportMetric("Connections/Count", "connections", totalCounts);
        reportMetric("Connections/Rate", "connections/sec", memoryUsageProcessor.process(totalCounts));
    }

    @Override
    public String getAgentName() {
        return this.name;
    }
}
