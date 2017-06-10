package event_based_server;


import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClient {

    private final String USER_AGENT = "Chrome/7.0.517.44";

    public static void main(String[] args) throws Exception {
        HttpClient httpClient = new HttpClient();

        while (true) {
            System.out.println("Testing 1 - Send Http GET request");
            httpClient.sendGet();
        }
//        System.out.println("Testing 2 - Send Http POST request");
//        httpClient.sendPost();
    }

    private void sendGet() throws Exception {
        String url = "http://127.0.0.1:8080/src/event_based_server/sample.txt";

        URL serverURL = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();

        connection.setRequestMethod("GET");

        connection.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = connection.getResponseCode();
        System.out.println("\nSending 'GET' request to URL => " + url);
        System.out.println("Response Code :" + responseCode + "\n");

        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        inputLine = input.readLine();
        response.append(inputLine);
//        while((inputLine = input.readLine())!=null){
//            response.append(inputLine);
//        }
        input.close();

        System.out.println(response.toString());

    }

    private void sendPost() throws Exception {
        String url = "http://127.0.0.1:8080/src/event_based_server/sample.txt";
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";

        con.setDoOutput(true); // Send post request
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println(response.toString());

    }
}
