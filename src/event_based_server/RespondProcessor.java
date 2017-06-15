package event_based_server;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

class RespondProcessor {
    RespondProcessor() {
    }

    static ByteBuffer createHeaderBuffer(int status, int bodyLength) throws CharacterCodingException {
        CharBuffer chars = CharBuffer.allocate(Constants.MAIN_BUFFER_SIZE);

        // Status
        chars.put("HTTP/1.1 "); // TODO: Refactoring following http response Format
        chars.put(HttpParser.getHttpReply(status) + "\n");

        // General headers
        chars.put(HttpParser.getDateHeader() + "\n");
        chars.put("connection: close\n");// TODO Connection close or keep-alive. 나누기.
        chars.put("cache-control: private, max-age=0");

        // Response headers
        chars.put("server: AsyncServerByTeam2/1.0.0\n");
        // Accept range

        // Entity headers
        chars.put("content-type: text/plain; charset=UTF-8\n");
        chars.put("Content-Length: " + bodyLength + "\n");
        chars.put("\n");

        chars.flip();

        return Charset.forName("UTF-8").encode(chars);
    }
}