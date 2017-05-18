package event_based_server;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class HttpHandler {
    private Charset iso8859;
    private CharsetDecoder iso8859Decoder;
    private Charset euckr;
    private CharsetEncoder euckrEncoder;


    public HttpHandler(){
        iso8859 = Charset.forName("iso-8859-1");
        iso8859Decoder = iso8859.newDecoder();
        euckr = Charset.forName("euc-kr");
        euckrEncoder = euckr.newEncoder();
    }

    //Proper type of Object
    public int parseHttpRequestMsg(String requestMsg,HttpInfo httpParsedInfo){
        //적절한 결과값으로 바꾸기
        //status
//        httpParsedInfo.setHttpUrl("");
//        httpParsedInfo.setHttpMethod("");
//        httpParsedInfo.setHttpProtocol("");
//        return 400;
        return 0;
    }

    //Proper type of Object
    public void processMsgInHttpResponseFormat(Object processedResult,ByteBuffer HeapBuffer){
        //Have to store in kernelBuffer, regarding efficiency and whole structure
        //Copy kernelBuffer to heapBuffer(result of handler)


        //Make Http Response Msg and Store in HeapBuffer
    }

}
