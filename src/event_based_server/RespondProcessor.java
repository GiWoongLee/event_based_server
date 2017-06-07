package event_based_server;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharacterCodingException;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.io.IOException;

//https://github.com/maulufia/async_server_test/blob/master/src/kr/co/jm/RequestProcessor.java
public class RespondProcessor {
    private ByteBuffer headerBuffer;
    private Charset euckr;
    private CharsetEncoder euckrEncoder;

    //Make Http form response with result
    //Need to use buffer to send through socket channel
    public RespondProcessor(){
        euckr = Charset.forName("euc-kr");
    }

    public void createHeaderBuffer() throws CharacterCodingException{
        CharBuffer chars = CharBuffer.allocate(88);
        chars.put("HTTP/1.1 200 OK\n");
        chars.put("Connection: close\n");
        chars.put("Server: testServer\n");
        chars.put("Content-Type: text/html\n");
        chars.put("\n");
        chars.flip();
        headerBuffer = euckrEncoder.encode(chars);
    }
}