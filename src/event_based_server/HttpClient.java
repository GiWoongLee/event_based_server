package event_based_server;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

//import java.net.InetSocketAddress;
//import java.net.SocketAddress;
//import java.nio.channels.SocketChannel;
//import java.nio.ByteBuffer;

public class HttpClient {

    //Could Select Various USER_AGENT such as safari, chrome, IE, etc
    private final String USER_AGENT = "Chrome/7.0.517.44";

    public static void main(String[] args) throws Exception{

        HttpClient httpClient = new HttpClient();

        System.out.println("Testing 1 - Send Http GET request");
        httpClient.sendGet();

//        System.out.println("Testing 2 - Send Http POST request");
//        httpClient.sendPost();

//        **** nio-style connecting server and sending info ****
//        SocketAddress serverAddress = new InetSocketAddress("localhost",3000);
//        SocketChannel client = SocketChannel.open(serverAddress);
//        ByteBuffer buf = ByteBuffer.allocate(24);
//
//        System.out.println(client);
//
//        while(true){
//            String temp = "Hello Server!";
//            byte[] bytes = temp.getBytes();
//            buf.put(bytes);
//            buf.flip();
//            client.write(buf);
//            buf.clear();
//
//            // Http Request Format/Message 적절히 구성해서 보내기
//            System.out.println("# Client write: " + temp);
//
//            int readStatus = client.read(buf);
//            buf.flip();
//            // Http Response 받은 내용 출력하기
//            System.out.print("# Client read: ");
//            if(readStatus!= -1){
//                while(buf.hasRemaining()){
//                    System.out.print((char)buf.get());
//                }
//                System.out.println("\n");
//            }
//            buf.clear();
//        }
    }

    private void sendGet() throws Exception{
        String url = "http://127.0.0.1:3000";

        URL serverURL = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();

        //optional default is GET
        connection.setRequestMethod("GET");

        //add request header
        connection.setRequestProperty("User-Agent",USER_AGENT);

        int responseCode = connection.getResponseCode();
        System.out.println("\nSending 'GET' request to URL => " + url);
        System.out.println("Response Code :" + responseCode);

        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while((inputLine = input.readLine())!=null){
            response.append(inputLine);
        }
        input.close();

        System.out.println(response.toString());
    }

    private void sendPost() throws Exception{
        String url = "https://selfsolve.apple.com/wcResults.do";
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";

        // Send post request
        con.setDoOutput(true);
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

        //print result
        System.out.println(response.toString());

    }
}
