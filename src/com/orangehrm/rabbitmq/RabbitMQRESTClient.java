package com.orangehrm.rabbitmq;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Objects;

public class RabbitMQRESTClient {
    private static final String RABBITMQ_API_URL = "https://infinity-rabbitmq.orangehrm.com/api/";
    private static final String RABBITMQ_USERNAME = "orangehrm";
    private static final String RABBITMQ_PASSWORD = "0FokLE%ypwo47S7u";

    private HttpsURLConnection connection = null;

    public RabbitMQRESTClient() throws IOException {
        this.setCredentials();
    }

    public String callAPIEndPoint(String endPoint) throws IOException {
        this.setConnection(endPoint);
        if(this.getResponseCode() == 200){
            return this.getResponseContent();
        }
        return  null;
    }

    private void setCredentials(){
        Authenticator.setDefault (new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication (RABBITMQ_USERNAME, RABBITMQ_PASSWORD.toCharArray());
            }
        });
    }

    private void setConnection(String endPoint) throws IOException {
        String api_url = RABBITMQ_API_URL;
        if(endPoint != null && !Objects.equals(endPoint, "")){
            api_url = RABBITMQ_API_URL.concat(endPoint);
        }
        URL url = new URL(api_url);
        this.connection = (HttpsURLConnection) url.openConnection();
    }

    private int getResponseCode() throws IOException {
        if(this.connection != null){
            return connection.getResponseCode();
        }
        return 0;
    }

    private String getResponseContent(){
        if(this.connection != null){
            try {
                BufferedReader br =
                        new BufferedReader(
                                new InputStreamReader(this.connection.getInputStream()));
                String inputComplete = "";
                String input;
                while ((input = br.readLine()) != null){
                    inputComplete = inputComplete.concat(input);
                }
                br.close();
                return inputComplete;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
