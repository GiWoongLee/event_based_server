package event_based_server;


import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;

public class HttpClient {

    public static void main(String[] args) throws Exception{
        SocketAddress serverAddress = new InetSocketAddress("localhost",3000);
        SocketChannel client = SocketChannel.open(serverAddress);
        ByteBuffer buf = ByteBuffer.allocate(24);

        System.out.println(client);

        while(true){
            String temp = "Hello Server!";
            byte[] bytes = temp.getBytes();
            buf.put(bytes);
            buf.flip();
            client.write(buf);
            buf.clear();
            System.out.println("# Client write: " + temp);

            buf.flip();
            System.out.println("# Client read: ");
            while (buf.hasRemaining()) {
                System.out.print((char) buf.get());
            }
            System.out.println("\n");
            buf.clear();
        }
    }
}
