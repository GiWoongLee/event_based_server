package event_based_server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.io.InputStream;

public class RequestProcessor {

    private ByteBuffer kernelBuffer;
    private ByteBuffer heapBuffer;
    private HttpParser httpParser;


    public RequestProcessor(){
        kernelBuffer = ByteBuffer.allocateDirect(1024);
        heapBuffer = ByteBuffer.allocate(1024);
        httpParser = new HttpParser();
    }

    public void process(SelectionKey requestedKey,byte[] httpMsg) throws IOException {
//        Step 1 : Parse Http Request and Retrieve related info
        int status = httpParser.parseRequest(httpMsg);

//        Step2 : Handle Heavy/Light Task

//        System.out.println(httpParser.getMethod());
//        System.out.println(httpParser.getRequestURL());
//        System.out.println(httpParser.getVersion());
//        System.out.println(httpParser.getHeaders());
//        System.out.println(httpParser.getParams());



        //Step 3 : Following request msg, handle task in a two way - CPU or light-IO Bound/ Heavy-IO Bound
//        Object res = handle();
//        //Step 4 : After finishing handling the request, store info in a format of Http Response to ByteBuffer
////        httpHandler.processMsgInHttpResponseFormat(res,heapBuffer);
//        //Step 5 : Attach buffer to key to make response in a for loop
//        requestedKey.attach(heapBuffer);
    }


    private Object handle(){
        //Step 1 : Following httpParsedInfo, do stuff
        //Case 1 : CPU/light IO Bound handling
        //Case 2 : Heavy IO Bound handling(File IO/Memory&Disk IO)
        return "<<<some result>®>>";
    }

}
