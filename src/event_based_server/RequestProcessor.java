package event_based_server;

import java.io.IOException;
import java.nio.channels.SelectionKey;

class RequestProcessor {
    private HttpParser httpParser;
    private FileIOThread fileIOThread;

    RequestProcessor() {
        httpParser = new HttpParser();
        fileIOThread = new FileIOThread();
    }

    void process(SelectionKey clientKey, byte[] httpMsg, Request request) throws IOException {
        int status = httpParser.parseRequest(httpMsg); // NOTE : parse http request and get the result of parsing

        System.out.println(httpParser.getMethod()); // Test : Print out httpParsed Info
        System.out.println(httpParser.getRequestURL());
        System.out.println(httpParser.getVersion());
        System.out.println(httpParser.getHeaders());
        System.out.println(httpParser.getParams());

        request.setHttpParser(httpParser);

        // TODO 200, 302, 400, 404, 500 처리
        if (status == 200) { // NOTE : Valid Http Request from client
            //TODO: if(HEAVY WORKLOAD) - Defined by requests that require IO tasks
            fileIOThread.handle(clientKey, httpParser, request); // NOTE : Activate Thread Pool to process task

            // TODO case: not file IO.
            // clientKey.attach("Hello World\n".getBytes());
            //
            // clientKey.interestOps(SelectionKey.OP_WRITE);
            // clientKey.selector().wakeup();

            // TODO: else - Defined by requests that don't require IO tasks
            // TODO: **NEED TO IDENTIFY WHAT IS LIGHT WORKLOAD TASK**
            // TODO: current thread process the task
        } else {
            request.setState(Request.ERROR);
            request.setResponseHeader(ResponseProcessor.createHeaderBuffer(400));

            clientKey.attach(request); //NOTE: send error message to event queue

            clientKey.interestOps(SelectionKey.OP_WRITE);
            clientKey.selector().wakeup();
        }

    }

}
