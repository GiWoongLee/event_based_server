package event_based_server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;

class RequestProcessor {
    private RespondProcessor respondProcessor; // FIXME for write
    private HttpParser httpParser;
    private FileIOThread fileIOThread;
    private ByteBuffer buf; // FIXME for write

    RequestProcessor() {
        respondProcessor = new RespondProcessor();
        httpParser = new HttpParser();
        fileIOThread = new FileIOThread();
        buf = ByteBuffer.allocate(Constants.MAIN_BUFFER_SIZE); // TODO 에러 시에도 write 로 넘겨서 처리하도록 해야. 버퍼 낭비에 안티패턴.
    }

    void process(SelectionKey clientKey, byte[] httpMsg) throws IOException {
        int status = httpParser.parseRequest(httpMsg); // NOTE : parse http request and get the result of parsing

        System.out.println(httpParser.getMethod()); // Test : Print out httpParsed Info
        System.out.println(httpParser.getRequestURL());
        System.out.println(httpParser.getVersion());
        System.out.println(httpParser.getHeaders());
        System.out.println(httpParser.getParams());

        // TODO 200, 302, 400, 404, 500 처리
        if (status == 200) { // NOTE : Valid Http Request from client
            //TODO: if(HEAVY WORKLOAD) - Defined by requests that require IO tasks
            fileIOThread.handle(clientKey, httpParser); // NOTE : Activate Thread Pool to process task

            // TODO: else - Defined by requests that don't require IO tasks
            // TODO: **NEED TO IDENTIFY WHAT IS LIGHT WORKLOAD TASK**
            // TODO: current thread process the task
        } else {
            clientKey.attach(null); //NOTE: send error message to event queue

            clientKey.interestOps(SelectionKey.OP_WRITE);
            clientKey.selector().wakeup();
        }

    }

}
