package event_based_server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;

public class RequestProcessor {

    private SelectionKey requestedKey;
    private ByteBuffer kernelBuffer;
    private ByteBuffer heapBuffer;
    private HttpHandler httpHandler;
    private HttpInfo httpParsedInfo;

    public RequestProcessor(){
        kernelBuffer = ByteBuffer.allocateDirect(1024);
        heapBuffer = ByteBuffer.allocate(1024);
        httpParsedInfo = new HttpInfo();
    }

    public void process(SelectionKey requestedKey){
        this.requestedKey = requestedKey;
        //Step 1 : Read request from client and transform bytes into String
        String requestMsg = readRequestMsg();
        //Step 2 : Parse Http Format String into useful info : URL, Method, Protocol
        int status = httpHandler.parseHttpRequestMsg(requestMsg,httpParsedInfo);
        //Step 3 : Following request msg, handle task in a two way - CPU or light-IO Bound/ Heavy-IO Bound
        Object res = handle();
        //Step 4 : After finishing handling the request, store info in a format of Http Response to ByteBuffer
        httpHandler.processMsgInHttpResponseFormat(res,heapBuffer);
        //Step 5 : Attach buffer to key to make response in a for loop
        requestedKey.attach(heapBuffer);
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
            //Test
            while(kernelBuffer.hasRemaining()){
                System.out.println((char)kernelBuffer.get());
            }

        }catch(IOException e){
            e.printStackTrace();
        }
        return "<<<some result>>>";
    }

    private Object handle(){
        //Step 1 : Following httpParsedInfo, do stuff
        //Case 1 : CPU/light IO Bound handling
        //Case 2 : Heavy IO Bound handling(File IO/Memory&Disk IO)
        return "<<<some result>>>";
    }


}
