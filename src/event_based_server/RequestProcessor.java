package event_based_server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.Iterator;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class RequestProcessor {

    private SelectionKey requestedKey;
    private ByteBuffer kernelBuffer;
    private ByteBuffer heapBuffer;
    private HttpParser httpParser;

    private Charset iso8859;
    private CharsetDecoder iso8859Decoder;
    private Charset euckr;
    private CharsetEncoder euckrEncoder;


    public RequestProcessor(){
        kernelBuffer = ByteBuffer.allocateDirect(1024);
        heapBuffer = ByteBuffer.allocate(1024);

        iso8859 = Charset.forName("iso-8859-1");
        iso8859Decoder = iso8859.newDecoder();
        euckr = Charset.forName("euc-kr");
        euckrEncoder = euckr.newEncoder();

    }

    public void process(SelectionKey requestedKey){
        this.requestedKey = requestedKey;
        String requestMsg = readRequestMsg();
        Object something = httpParser.parse(requestMsg);
        handle(something);
    }

    private String readRequestMsg(){
        //Step 1 : Read Buffer and store in Data Structure
        //Use While Loop to read fully request msg from buffer
        //Step 2 : Change msg into the String and return it
        //Concatenate decoded message into String
        try (SocketChannel client = (SocketChannel) requestedKey.channel()) {
            int bytesCount = client.read(kernelBuffer);
            if(bytesCount > 0){
                kernelBuffer.flip();
                return new String(kernelBuffer.array());
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return "<<<some result>>>";
    }

    private void handle(Object something/*<<<Change DataType/Name>>>*/){
        //File IO/Memory&Disk IO/Something else... Happening
        //Have to store in kernelBuffer, regarding efficiency and whole structure
        //Copy kernelBuffer to heapBuffer(result of handler)
        requestedKey.attach(heapBuffer);
    }
}
