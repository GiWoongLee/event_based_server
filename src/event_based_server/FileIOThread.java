package event_based_server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.charset.CharacterCodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.nio.ByteBuffer;


class FileIOThread {
    private ExecutorService executorService;
    private InMemoryCache<String> cache;

    FileIOThread() {
        executorService = Executors.newFixedThreadPool(
                Constants.THREAD_POOL_SIZE
        );
        cache = new InMemoryCache<>(Constants.CACHE_TIME_TO_LIVE, Constants.CACHE_TIMER_INTERVAL, Constants.CACHE_MAX_ITEMS, Constants.CACHE_BYTE_SIZE);
    }

    private Runnable loadFile(String filePath, SelectionKey clientKey, Request request) throws IOException {
        return () -> {
            String fileName = filePath;
            if (fileName.isEmpty()) {
                fileName = "index.html";
            }
            Path path = Paths.get("./server-root/" + fileName);
            try {
                // RandomAccessFile raf = new RandomAccessFile("./server-root/" + filePath, "r");
                // FileChannel channel = raf.getChannel();
                ByteArrayWrapper data = cache.get(fileName);
                String extension = "";

                int i = fileName.lastIndexOf('.');
                if (i > 0) {
                    extension = fileName.substring(i+1);
                }
                if (extension.equals("js")) {
                    extension = "javascript";
                } else if (extension.equals("css")) {
                } else if (extension.equals("html")) {
                } else {
                    extension = "plain";
                }

                if (data != null) {
                    System.out.println("CachedData exists!");
                } else {
                    data = new ByteArrayWrapper(Files.readAllBytes(path));
                    cache.put(filePath, data);
                }

                request.setState(Request.WRITE);
                request.setResponseHeader(ResponseProcessor.createHeaderBuffer(200, data.getByteArray().length, extension));
                request.setData(ByteBuffer.wrap(data.getByteArray()));

                clientKey.attach(request); // NOTE: send result to event queue

                clientKey.interestOps(SelectionKey.OP_WRITE);
                clientKey.selector().wakeup();
            } catch (IOException e) {
                request.setState(Request.ERROR);
                try {
                    request.setResponseHeader(ResponseProcessor.createHeaderBuffer(404));
                } catch (CharacterCodingException e1) {
                    e1.printStackTrace();
                }

                clientKey.attach(request); //NOTE: send error message to event queue

                clientKey.interestOps(SelectionKey.OP_WRITE);
                clientKey.selector().wakeup();
            }
        };
    }


    void handle(SelectionKey clientKey, HttpParser httpParser, Request request) throws IOException {
        //TODO : Following http request info, execute different task with thread pool
        String filePath = httpParser.getRequestURL().substring(1); //FIXME: Parse Directory info and store new file into proper directory
        // FIXME: true => GET // false => POST
        // if(true){
        Runnable task = loadFile(filePath, clientKey, request);
        executorService.submit(task);
        // }
        // else{
        // FIXME : HttpParser return bytebuffer which contains bodycontent
        // ByteBuffer bodyContent = ByteBuffer.allocate(1024);
        // Runnable task = newFile(bodyContent,filePath,clientKey);
        // executorService.submit(task);
        // }
    }
}