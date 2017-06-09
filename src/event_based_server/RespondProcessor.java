package event_based_server;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharacterCodingException;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.io.IOException;

public class RespondProcessor {
    private HttpParser httpParser;
    private ByteBuffer headerBuffer;
    private Charset euckr;
    private CharsetEncoder euckrEncoder;

    public RespondProcessor() {
        euckr = Charset.forName("euc-kr");
        euckrEncoder = euckr.newEncoder();
        httpParser = new HttpParser();
    }

    public ByteBuffer createHeaderBuffer(int status) throws CharacterCodingException {
        CharBuffer chars = CharBuffer.allocate(1092);
        chars.put("HTTP/1.1 "); //TODO: Refactoring following http response Format
        chars.put(HttpParser.getHttpReply(status) + "\n");
        chars.put(HttpParser.getDateHeader() + "\n");
        chars.put("Server: testServer\n");
        chars.put("Content-Length: 230\n");
        chars.put("Content-Type: text/html; charset=iso-8859-1\n");
        chars.put("Connection: closed\n");
        chars.put("\n");
        chars.flip();
        headerBuffer = euckrEncoder.encode(chars);
        return headerBuffer;
    }
}