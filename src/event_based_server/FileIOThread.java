package event_based_server;


import com.sun.org.apache.bcel.internal.generic.Select;
import com.sun.security.ntlm.Server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.SelectionKey;
import java.util.concurrent.*;
import java.nio.channels.CompletionHandler;

public class FileIOThread {

    private ExecutorService executorService;
    private RespondProcessor respondProcessor;

    public FileIOThread(){
        executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
        );
        respondProcessor = new RespondProcessor();
    }


//    https://docs.oracle.com/javase/7/docs/api/java/nio/channels/CompletionHandler.html
    private CompletionHandler<byte[],SelectionKey> callback = new CompletionHandler<byte[], SelectionKey>() {
        @Override
        public void completed(byte[] result, SelectionKey clientKey) {
            //RespondProcessor make Http Header
            //send result and channel related info to event queue
            clientKey.attach(result);
        }

        @Override
        public void failed(Throwable exc,SelectionKey clientKey) {
            //RespondProcessor make Http Header
            //send error msg and channel related infos to event queue
            clientKey.attach("Msg: Request Failed");
        }
    };

//   Make new Task for Reading File
//    List up Diverse tasks to be handled
    private Runnable makeNewTask(SelectionKey clientKey){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                String filePath = "src/ex0801/io/이클립스.zip"; // target file
                FileInputStream inputStream = null; // file inputstream
                FileOutputStream outputStream = null;
                BufferedInputStream bufferedInputStream = null; // buffer stream
                BufferedOutputStream bufferedOutputStream = null;
                //Declare Buffer
                byte[] readBuffer = new byte[1024];

                try
                {
                    inputStream = new FileInputStream(filePath);// 파일 입력 스트림 생성
                    bufferedInputStream = new BufferedInputStream(inputStream);// 파일 출력 스트림 생성

                    outputStream = new FileOutputStream("src/ex0801/io/bb.zip");
                    bufferedOutputStream = new BufferedOutputStream(outputStream);

                    while (bufferedInputStream.read(readBuffer, 0, readBuffer.length) != -1)
                    {
                        //버퍼 크기만큼 읽을 때마다 출력 스트림에 써준다.
                        bufferedOutputStream.write(readBuffer);
                    }
                    callback.completed(readBuffer,clientKey);

                }
                catch (Exception e)
                {
                    callback.failed(e,clientKey);
                }
                finally
                {
                    try
                    {
                        // 파일 닫기. 여기에도 try/catch가 필요하다.
                        // 보조스트림을 닫으면 원스트림도 닫힌다.
                        bufferedInputStream.close();
                        bufferedOutputStream.close();
                    }
                    catch (Exception e)
                    {
                        callback.failed(e,clientKey);
//                        System.out.println("닫기 실패" + e);
                    }
                }
            }
        };
        return task;
    }

    public void handle(SelectionKey clientKey, HttpParser httpParser){
//          httpParser 정보를 기반으로 task 분기하기
        if(httpParser.getRequestURL() == "sample.txt"){
            Runnable task = makeNewTask(clientKey);
            executorService.submit(task);
        }
        else{

        }
    }

}