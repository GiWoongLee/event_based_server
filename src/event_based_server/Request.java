package event_based_server;

import java.nio.ByteBuffer;

/**
 * Created by soo on 2017. 6. 10..
 */
public class Request {
    private byte[] data;
    private HttpParser httpParser;
    private final long requestStartTime;
    private int state;
    private ByteBuffer responseHeader;

    static final int INIT = 0;
    static final int ACCEPT = 1;
    static final int READ = 2;
    static final int WRITE = 4;
    static final int ERROR = -1;

    Request() {
        this(System.currentTimeMillis());
    }

    private Request(long requestStartTime) {
        this.requestStartTime = requestStartTime;
        state = INIT;
    }

    byte[] getData() {
        return data;
    }

    void setData(byte[] data) {
        this.data = data;
    }

    public HttpParser getHttpParser() {
        return httpParser;
    }

    void setHttpParser(HttpParser httpParser) {
        this.httpParser = httpParser;
    }

    long getRequestStartTime() {
        return requestStartTime;
    }

    void setState(int state) {
        this.state = state;
    }

    int getState() {
        return state;
    }

    void setResponseHeader(ByteBuffer responseHeader) {
        this.responseHeader = responseHeader;
    }

    ByteBuffer getResponseHeader() {
        return responseHeader;
    }
}
