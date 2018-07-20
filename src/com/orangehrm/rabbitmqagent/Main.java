package com.orangehrm.rabbitmqagent;

import com.newrelic.metrics.publish.Runner;
import org.json.simple.parser.ParseException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        try{
            Runner runner = new Runner();
            runner.add(new RabbitMQAgentFactory());
            runner.setupAndRun();
        }catch (Exception e){
            System.err.println("ERROR: " + e.getMessage());
            System.exit(-1);
        }
    }
}
