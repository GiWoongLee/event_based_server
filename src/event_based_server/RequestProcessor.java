package event_based_server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;

public class RequestProcessor {
    private RespondProcessor respondProcessor;
    private HttpParser httpParser;
    private FileIOThread fileIOThread;
    private ByteBuffer buf;

    public RequestProcessor() {
        respondProcessor = new RespondProcessor();
        httpParser = new HttpParser();
        fileIOThread = new FileIOThread();
        buf = ByteBuffer.allocate(1024);
    }

    public void process(SelectionKey clientKey, byte[] httpMsg) throws IOException {
        int status = httpParser.parseRequest(httpMsg); //NOTE : parse http request and get the result of parsing

        System.out.println(httpParser.getMethod());  //Test : Print out httpParsed Info
        System.out.println(httpParser.getRequestURL());
        System.out.println(httpParser.getVersion());
        System.out.println(httpParser.getHeaders());
        System.out.println(httpParser.getParams());

        if (status == 200) { //NOTE : Valid Http Request from client
            //TODO: if(HEAVY WORKLOAD) - Defined by requests that require IO tasks
            fileIOThread.handle(clientKey, httpParser); // NOTE : Activate Thread Pool to process task

            //TODO: else - Defined by requests that don't require IO tasks
            //TODO: **NEED TO IDENTIFY WHAT IS LIGHT WORKLOAD TASK**
            //TODO: current thread process the task
        } else {
            buf = respondProcessor.createHeaderBuffer(status); //NOTE: Buffer Size Need to be same as the buffer used in RespondProcessor
            SocketChannel clientChannel = (SocketChannel) clientKey.channel();
            clientChannel.write(buf);
            buf.clear();
        }

    }

}
