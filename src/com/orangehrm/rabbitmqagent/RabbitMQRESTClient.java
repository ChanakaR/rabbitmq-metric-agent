package com.orangehrm.rabbitmqagent;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Objects;

public class RabbitMQRESTClient {

    private String rabbitMQApiURL = null;

    private HttpsURLConnection connection = null;

    public RabbitMQRESTClient(String host, String username, String password){
        this.setCredentials(username, password);
        this.rabbitMQApiURL = host.concat("/api/");
    }

    public String callAPIEndPoint(String endPoint) {
        try{
            this.setConnection(endPoint);
            if(this.getResponseCode() == 200){
                return this.getResponseContent();
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        return  null;
    }

    private void setCredentials(String username, String password){
        Authenticator.setDefault (new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication (username, password.toCharArray());
            }
        });
    }

    private void setConnection(String endPoint) throws IOException {
        String api_url = rabbitMQApiURL;
        if(endPoint != null && !Objects.equals(endPoint, "")){
            api_url = rabbitMQApiURL.concat(endPoint);
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


    // ========================================== //
    // ---- REMOVE THE BELOW CODE WHEN DEPLOY ----//
    // ========================================== //

    static {
        disableSslVerification();
    }

    private static void disableSslVerification() {
        try
        {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }
}
