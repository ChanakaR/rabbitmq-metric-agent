package com.orangehrm.rabbitmqagent;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.newrelic.metrics.publish.processors.EpochProcessor;
import com.newrelic.metrics.publish.processors.Processor;
import com.newrelic.metrics.publish.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class RabbitMQAgent extends Agent{

    private static final String GUID = "com.orangehrm.Rabbi";
    private static final String VERSION = "1.0.0";

    private String name;


    private RabbitMQRESTClient rabbitRESTClient;
    private Processor messageDeliverRateProcessor = new EpochProcessor();
    private Processor messagePublishRateProcessor = new EpochProcessor();
    private JSONParser jsonParser = new JSONParser();
    private static final Logger logger = Logger.getLogger(RabbitMQAgent.class);

    public RabbitMQAgent(String name,String host,String username, String password){
        super(GUID, VERSION);
        this.name = name;
        this.rabbitRESTClient = new RabbitMQRESTClient(host,username,password);
    }

    @Override
    public void pollCycle() {
        try {
            this.setUpOverallMetrics();
            this.setUpNodeSpecificMetrics();
        } catch (ParseException parseException) {
            logger.error(parseException,"JSON parse error!");
        } catch (IOException ioException){
            logger.error(ioException,"RabbitMQ node connection error!");
        }

    }

    @Override
    public String getAgentName() {
        return name;
    }

    /**
     *
     * Available metrics
     *  - MessagesTotal/Count : messages
     *  - MessagesReady/Count : messages
     *  - MessagesUnAcknowledge/Count : messages
     *  - MessageTotalDelivered/Rate : messages/sec
     *  - MessagesReturnedUnroutable/Count : messages
     *  - MessagePublishedRate/Rate : messages/sec
     *  - MessagesRedelivered/Count : messages
     *
     */

    private void setUpOverallMetrics() throws ParseException, IOException {

        String result = this.rabbitRESTClient.callAPIEndPoint("overview");
        JSONObject resultJSONObjects = (JSONObject) jsonParser.parse(result);
        JSONObject queuesTotal = (JSONObject) resultJSONObjects.get("queue_totals");

        System.out.println("TOTAL MESSAGES : " + queuesTotal.get("messages"));
        reportMetric("MessagesTotal/Count", "messages", (long) queuesTotal.get("messages"));

        System.out.println("MESSAGES READY : " + queuesTotal.get("messages_ready"));
        reportMetric("MessagesReady/Count", "messages", (long) queuesTotal.get("messages_ready"));

        System.out.println("MESSAGES UNACK : " + queuesTotal.get("messages_unacknowledged"));
        reportMetric("MessagesUnAcknowledge/Count", "messages", (long) queuesTotal.get("messages_unacknowledged"));


        JSONObject messageStats = (JSONObject) resultJSONObjects.get("message_stats");

        System.out.println("MESSAGES TOTAL DELIVERED : " + messageStats.get("deliver_get"));
        long deliveredMessageCount = (long) messageStats.get("deliver_get");
        if(messageStats.get("deliver_get") != null){
            Object deliverRate = messageDeliverRateProcessor.process(deliveredMessageCount);
            if(deliverRate != null){
                reportMetric("MessagesDelivered/Rate", "messages/sec", (float) deliverRate);
            }else{
                reportMetric("MessagesDelivered/Rate", "messages/sec", 0);
            }
        }


        System.out.println("MESSAGES TOTAL RETURNED UNROUTABLE : " + messageStats.get("return_unroutable"));
        if( messageStats.get("return_unroutable") != null){
            reportMetric("MessagesReturnedUnroutable/Count", "messages", (long) messageStats.get("return_unroutable"));
        }else {
            reportMetric("MessagesReturnedUnroutable/Count", "messages", 0);
        }

        if(messageStats.get("redeliver") != null){
            reportMetric("MessagesRedelivered/Count", "messages", (long) messageStats.get("redeliver"));
        }else{
            reportMetric("MessagesRedelivered/Count", "messages", 0);
        }

        System.out.println("MESSAGES PUBLISHED : " + messageStats.get("publish"));
        long publishedMessageCount = (long) messageStats.get("publish");
        if(messageStats.get("publish") != null){
            Object publishedRate = messagePublishRateProcessor.process(publishedMessageCount);
            if(publishedRate != null){
                reportMetric("MessagesPublished/Rate", "messages/sec", (float) publishedRate);
            }else{
                reportMetric("MessagesPublished/Rate", "messages/sec", 0);
            }

        }

    }

    /**
     * - [NodeName]/MemoryUsage/Percentage : percentage
     * - [NodeName]/TotalMemory/Megabytes : Mb
     * - [NodeName]/NodeStatus : status
     */
    private void setUpNodeSpecificMetrics() throws IOException{
        try {
            String result = this.rabbitRESTClient.callAPIEndPoint("nodes");
            Object resultJSONObjects =  jsonParser.parse(result);
            JSONArray jsonArray = (JSONArray)resultJSONObjects;
            for(Object arrayElement : jsonArray){
                JSONObject nodeJSONObject = (JSONObject)arrayElement;
                long mem_limit = (long) nodeJSONObject.get("mem_limit");
                long mem_used = (long) nodeJSONObject.get("mem_used");
                float percentage = (mem_used * 100.0f) / mem_limit;

                System.out.println("Node/MemoryUsage/"+nodeJSONObject.get("name")+" : " + percentage);
                reportMetric(nodeJSONObject.get("name")+"/MemoryUsage/Percentage", "percentage", percentage);

                System.out.println("Node/TotalMemory/"+nodeJSONObject.get("name")+" : " + ((long)nodeJSONObject.get("mem_used") / 1024));
                reportMetric(nodeJSONObject.get("name")+"/TotalMemory/Megabytes", "Mb", ((long)nodeJSONObject.get("mem_used") / 1024));

                int running;
                boolean isRunning = (boolean)nodeJSONObject.get("running");
                if(isRunning){
                    running = 1;
                }else {
                    running = 0;
                }
                System.out.println("Node/Running/"+nodeJSONObject.get("name")+nodeJSONObject.get("name")+" : " + running);
                reportMetric(nodeJSONObject.get("name") + "/NodeStatus","status", running);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
