package event_based_server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


class FileIOThread {
    private ExecutorService executorService;
    private InMemoryCache<String> cache;

    FileIOThread() {
        executorService = Executors.newFixedThreadPool(
                Constants.THREAD_POOL_SIZE
        );
        cache = new InMemoryCache<>(Constants.CACHE_TIME_TO_LIVE, Constants.CACHE_TIMER_INTERVAL, Constants.CACHE_MAX_ITEMS, Constants.CACHE_BYTE_SIZE);
    }

    private Runnable loadFile(String filePath, SelectionKey clientKey) {
        return () -> {
            Path path = Paths.get("./server-root/" + filePath);
            try {
                byte[] data;
                ByteArrayWrapper cachedData = cache.get(filePath);

                if (cachedData != null) {
                    System.out.println("CachedData exists!");
                    data = cachedData.getByteArray();
                } else {
                    data = Files.readAllBytes(path);
                    cache.put(filePath, new ByteArrayWrapper(data));
                }

                clientKey.attach(data); // NOTE: send result to event queue

                clientKey.interestOps(SelectionKey.OP_WRITE);
                clientKey.selector().wakeup();

                // FIXME : When the file is bigger than the buffer, Handle it in some way with while statement
                // while (bufferedInputStream.read(readBuffer, 0, readBuffer.length) != -1) {}
            } catch (IOException e) {
                clientKey.attach(null); //NOTE: send error message to event queue

                clientKey.interestOps(SelectionKey.OP_WRITE);
                clientKey.selector().wakeup();
            }
        };
    }


    void handle(SelectionKey clientKey, HttpParser httpParser) {
        //TODO : Following http request info, execute different task with thread pool
        String filePath = httpParser.getRequestURL().substring(1); //FIXME: Parse Directory info and store new file into proper directory
        // FIXME: true => GET // false => POST
        // if(true){
        Runnable task = loadFile(filePath, clientKey);
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