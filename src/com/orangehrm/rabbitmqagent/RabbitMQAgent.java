package com.orangehrm.rabbitmqagent;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.processors.EpochProcessor;
import com.newrelic.metrics.publish.processors.Processor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.*;

public class RabbitMQAgent extends Agent{

    private static final String GUID = "com.orangehrm.Rabbi1";
    private static final String VERSION = "1.0.0";

    private String name;
    private List<Map<String, String>> vHostData = new ArrayList<>();
    private static final String VHOST_API = "vhosts";
    private static final String NODE_API = "nodes";


    private RabbitMQRESTClient rabbitRESTClient;
    private Processor messagesProcessor = new EpochProcessor();;
    private Processor bytesReadProcessor = new EpochProcessor();;

    public RabbitMQAgent(String name,String host,String username, String password){
        super(GUID, VERSION);
        this.name = name;
        this.rabbitRESTClient = new RabbitMQRESTClient(host,username,password);
    }

    @Override
    public void pollCycle() {
        try {
            List<Map<String, String>> vHostData= this.setUpVHostData();
                    int vHostMessageCount = this.getTotalMessages("ohrmvhost_264b8559",vHostData);
            System.out.println(vHostMessageCount);
            reportMetric("Messages/Count", "messages", vHostMessageCount);
            reportMetric("Messages/Rate", "messages/sec", messagesProcessor.process(vHostMessageCount));
        } catch (ParseException e) {
            e.printStackTrace();
        }


        // Report two metrics for each value from the server,
//        // One for the plain scalar value and one that processes the value over time
//        reportMetric("Connections/Rate", "connections/sec", connectionsProcessor.process(numConnections));
//
//        reportMetric("BytesRead/Count", "bytes", bytesRead);
//        reportMetric("BytesRead/Rate", "bytes/sec", bytesReadProcessor.process(bytesRead));
    }

    @Override
    public String getAgentName() {
        return name;
    }

    private int getTotalMessages(String vHost,List<Map<String, String>> vHostData){
        for (Map<String, String> aVHostData : vHostData) {
            if(Objects.equals(aVHostData.get("name"), "ohrmvhost_264b8559")){
                 return Integer.parseInt(aVHostData.get("total_messages"));
            }
        }
        return 0;
    }

    public List<Map<String, String>> setUpVHostData() throws ParseException {
        List<Map<String, String>> vHostData = new ArrayList<>();
        String result = this.rabbitRESTClient.callAPIEndPoint(VHOST_API);
        JSONParser jsonParser = new JSONParser();
        Object obj = jsonParser.parse(result);
        JSONArray resultJSONArray = (JSONArray)obj;
        int arrayLength = resultJSONArray.size();
//        System.out.println(result);
        for (Object arrayElement : resultJSONArray) {
            Map<String,String> vhostDataMap = new HashMap<String,String>();
            JSONObject jsonObject = (JSONObject) arrayElement;

            vhostDataMap.put("name",(String)jsonObject.get("name"));
            if(jsonObject.get("messages") != null){
                vhostDataMap.put("total_messages",String.valueOf(jsonObject.get("messages")));
            }else {
                vhostDataMap.put("total_messages","0");
            }

//            System.out.println("Vhost name : " + jsonObject.get("name"));
//            System.out.println("Messages in Ready State : " + jsonObject.get("messages_ready"));
//            System.out.println("Messages total count : " + jsonObject.get("messages"));
//            System.out.println("Messages Unacknowledged : " + jsonObject.get("messages_unacknowledged"));
            if(jsonObject.get("recv_oct_details") != null){
                JSONObject recv_oct_details = (JSONObject)jsonObject.get("recv_oct_details");
                JSONObject send_oct_details = (JSONObject)jsonObject.get("send_oct_details");
                vhostDataMap.put("recv_oct_rate",(String)recv_oct_details.get("rate"));
            }
            vHostData.add(vhostDataMap);
        }
        return vHostData;
    }

    private void setUpNodeMetrics() throws IOException, ParseException {
        String result = this.rabbitRESTClient.callAPIEndPoint(VHOST_API);
        JSONParser jsonParser = new JSONParser();
        Object obj = jsonParser.parse(result);
        JSONArray resultJSONArray = (JSONArray)obj;
        for (Object arrayElement : resultJSONArray) {
            JSONObject jsonObject = (JSONObject) arrayElement;
            System.out.println("fd_used: " + jsonObject.get("fd_used"));
            System.out.println();
        }
    }
}
