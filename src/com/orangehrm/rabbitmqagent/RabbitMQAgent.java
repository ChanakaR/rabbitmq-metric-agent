package com.orangehrm.rabbitmqagent;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.processors.EpochProcessor;
import com.newrelic.metrics.publish.processors.Processor;
import com.newrelic.metrics.publish.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class RabbitMQAgent extends Agent{

    private static final String GUID = "com.orangehrm.OHRMRabbitMQ";
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
            System.exit(-1);
        } catch (IOException ioException){
            logger.error(ioException,"RabbitMQ node connection error!");
            System.exit(-1);
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

        reportMetric("MessagesTotal/Count", "messages", (long) queuesTotal.get("messages"));
        reportMetric("MessagesReady/Count", "messages", (long) queuesTotal.get("messages_ready"));
        reportMetric("MessagesUnAcknowledge/Count", "messages", (long) queuesTotal.get("messages_unacknowledged"));

        JSONObject messageStats = (JSONObject) resultJSONObjects.get("message_stats");

        System.out.println("DELIVERED " + messageStats.get("deliver_get"));
        if(messageStats.get("deliver_get") != null){
            long deliveredMessageCount = (long) messageStats.get("deliver_get");
            Object deliverRate = messageDeliverRateProcessor.process(deliveredMessageCount);
            if(deliverRate != null){
                System.out.println("DELIVER RATE"+deliverRate);
                reportMetric("MessagesDelivered/Rate", "messages/sec", (float) deliverRate);
            }else{
                reportMetric("MessagesDelivered/Rate", "messages/sec", 0);
            }
        }else{
            reportMetric("MessagesDelivered/Rate", "messages/sec", 0);
        }

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

        System.out.println("PUBLISHED " + messageStats.get("publish"));
        if(messageStats.get("publish") != null){
            long publishedMessageCount = (long) messageStats.get("publish");
            Object publishedRate = messagePublishRateProcessor.process(publishedMessageCount);
            if(publishedRate != null){
                System.out.println("PUBLISHED IN RATE"+publishedRate);
                reportMetric("MessagesPublished/Rate", "messages/sec", (float) publishedRate);
            }else{
                reportMetric("MessagesPublished/Rate", "messages/sec", 0);
            }
        }else{
            reportMetric("MessagesPublished/Rate", "messages/sec", 0);
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

                reportMetric(nodeJSONObject.get("name")+"/MemoryUsage/Percentage", "percentage", percentage);

                reportMetric(nodeJSONObject.get("name")+"/TotalMemory/Megabytes", "Mb", ((long)nodeJSONObject.get("mem_used") / (1024*1024)));

                int running;
                boolean isRunning = (boolean)nodeJSONObject.get("running");
                if(isRunning){
                    running = 1;
                }else {
                    running = 0;
                }
                reportMetric(nodeJSONObject.get("name") + "/NodeStatus","status", running);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
