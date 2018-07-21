package com.orangehrm.rabbitmqagent;

import com.newrelic.metrics.publish.Runner;
import com.newrelic.metrics.publish.util.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(RabbitMQAgent.class);
    public static void main(String[] args) {
        try{
            Runner runner = new Runner();
            runner.add(new RabbitMQAgentFactory());
            runner.setupAndRun();
        }catch (Exception e){
            logger.error(e,"error!");
            System.exit(-1);
        }
    }
}
