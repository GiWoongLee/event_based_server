package event_based_server;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;


public class RequestProcessor {

    private ByteBuffer kernelBuffer;
    private ByteBuffer heapBuffer;
    private HttpParser httpParser;
    private FileIOThread fileIOThread;


    public RequestProcessor(){
//        kernelBuffer = ByteBuffer.allocateDirect(1024);
//        heapBuffer = ByteBuffer.allocate(1024);
        httpParser = new HttpParser();
        fileIOThread = new FileIOThread();
    }

    public void process(SelectionKey clientKey, byte[] httpMsg) throws IOException {
//        Step 1 : Parse Http Request and Retrieve related info
        int status = httpParser.parseRequest(httpMsg);
//        System.out.println(httpParser.getMethod());
//        System.out.println(httpParser.getRequestURL());
//        System.out.println(httpParser.getVersion());
//        System.out.println(httpParser.getHeaders());
//        System.out.println(httpParser.getParams());

        if(status==200){
            //Step2 : classify heavy/light workload

            //Step3 : Handle light workload, Activate other threads for the heavy workload
            fileIOThread.handle(clientKey,httpParser);
            //Step4 : Pass result to respondProcessor to make callback(response)


//        requestedKey.attach(heapBuffer);

        }
        else{
//            Exception Handling
        }

    }

}
