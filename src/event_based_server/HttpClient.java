package event_based_server;


import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;

public class HttpClient {

    public static void main(String[] args) throws Exception{





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
}
