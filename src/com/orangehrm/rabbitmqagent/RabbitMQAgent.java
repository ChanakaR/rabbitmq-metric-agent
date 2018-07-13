package com.orangehrm.rabbitmqagent;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.processors.EpochProcessor;
import com.newrelic.metrics.publish.processors.Processor;

public class RabbitMQAgent extends Agent{

    private static final String GUID = "com.orangehrm.rabbitmqagent";
    private static final String VERSION = "1.0.0";

    private String name;
    private Processor connectionsProcessor = new EpochProcessor();;
    private Processor bytesReadProcessor = new EpochProcessor();;

    public RabbitMQAgent(String name,String host){
        super(GUID, VERSION);
        this.name = name;
    }

    @Override
    public void pollCycle() {
        int numConnections = getNumConnections();
        int bytesRead = getNumberBytesRead();

        // Report two metrics for each value from the server,
        // One for the plain scalar value and one that processes the value over time
        reportMetric("Connections/Count", "connections", numConnections);
        reportMetric("Connections/Rate", "connections/sec", connectionsProcessor.process(numConnections));

        reportMetric("BytesRead/Count", "bytes", bytesRead);
        reportMetric("BytesRead/Rate", "bytes/sec", bytesReadProcessor.process(bytesRead));
    }

    @Override
    public String getAgentName() {
        return name;
    }

    private int getNumConnections(){
        return 124;
    }

    private int getNumberBytesRead(){
        return 55;
    }
}
