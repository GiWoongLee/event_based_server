package event_based_server;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharacterCodingException;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.io.IOException;

class RespondProcessor {
    private CharsetEncoder utf8Encoder;

    RespondProcessor() {
        Charset utf8 = Charset.forName("UTF-8");
        utf8Encoder = utf8.newEncoder();
    }

    ByteBuffer createHeaderBuffer(int status) throws CharacterCodingException {
        CharBuffer chars = CharBuffer.allocate(Constants.MAIN_BUFFER_SIZE);

        // Status
        chars.put("HTTP/1.1 "); //TODO: Refactoring following http response Format
        chars.put(HttpParser.getHttpReply(status) + "\n");

        // General headers
        chars.put(HttpParser.getDateHeader() + "\n");
        chars.put("Connection: close\n");// TODO Connection close or keep-alive. 나누기.

        // Response headers
        chars.put("Server: AsyncServerByTeam2/1.0.0\n");
        // Accept range

        // Entity headers
        chars.put("Content-Type: text/html; charset=UTF-8\n");
        chars.put("Content-Length: 230\n"); // TODO file length specify
        chars.put("\n");

        chars.flip();

        return utf8Encoder.encode(chars);
    }
}