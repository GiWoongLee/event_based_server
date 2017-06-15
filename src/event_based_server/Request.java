package event_based_server;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by soo on 2017. 6. 10..
 */
public class Request {
    private ByteBuffer data;
    private HttpParser httpParser;
    private final long requestStartTime;
    private int state;
    private ByteBuffer responseHeader;
    private ByteBuffer response;

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
        this.state = INIT;
    }

    ByteBuffer getData() {
        return data;
    }

    void setData(ByteBuffer data) {
        this.data = data;
        responseHeader.flip();
        ByteBuffer res = ByteBuffer.allocate(responseHeader.remaining() + data.remaining());
        res.put(responseHeader);
        res.put(data);
        res.flip();
        this.response = res;
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
        ByteBuffer res = ByteBuffer.allocate(responseHeader.remaining());
        res.put(responseHeader);
        res.flip();
        this.response = res;
    }

    ByteBuffer getResponseHeader() {
        // responseHeader.flip();
        return responseHeader;
    }

    ByteBuffer getResponse() {
        // response.flip();
        return response;
    }
}
