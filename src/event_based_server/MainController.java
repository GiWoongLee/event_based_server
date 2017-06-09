package event_based_server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.ByteBuffer;
import java.util.Iterator;

public class MainController implements Runnable {
    private Selector selector;

    private ServerSocketChannel server;

    private RequestProcessor requestProcessor;
    private RespondProcessor respondProcessor;

    private ByteBuffer buf;

    private MainController(int port) throws IOException {
        selector = Selector.open();

        server = ServerSocketChannel.open();
        server.configureBlocking(false);

        InetAddress hostIPAddress = InetAddress.getByName(Constants.HOST); //FIXME : Fix Server Name
        InetSocketAddress address = new InetSocketAddress(hostIPAddress, port);
        server.socket().bind(address);

        server.register(selector, SelectionKey.OP_ACCEPT); //NOTE: register ssc into selector

        requestProcessor = new RequestProcessor();
        respondProcessor = new RespondProcessor();

        // FIXME 버퍼 하나로 전체 다 충분?
        buf = ByteBuffer.allocate(Constants.MAIN_BUFFER_SIZE); //FIXME : Adjust buffer size with JMeter Test
    }

    public void run() {
        try {
            System.out.println("Server Started"); // Test: Message
            System.out.println("****************************");
            System.out.println("Waiting For Client Connection");
            System.out.println("****************************");
            while (!Thread.interrupted()) {
                int readyKeys = selector.select();

                if (readyKeys > 0) {
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();

                    while (iter.hasNext()) {
                        // NOTE: PROPOSAL. accept, read, write 를 별도의 스레드로 처리하여 병렬 처리 가능. 성능향상 기대.
                        // Set selected = selector.selectedKeys();
                        // Iterator itr = selected.iterator();
                        // while (itr.hasNext())
                        // dispatch((SelectionKey)(itr.next()); //starts separate threads
                        // selected.clear(); //clear the keys from the set since they are already processed

                        SelectionKey key = iter.next();
                        iter.remove();

                        SelectableChannel selectedChannel = key.channel();

                        if (key.isAcceptable()) {
                            acceptor(selectedChannel);
                        } else if (key.isReadable()) {
                            reader(selectedChannel, key);
                        } else if (key.isWritable()) {
                            writer(selectedChannel, key);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // FIXME user 가 접속을 차단했을 때 에러에 의해 서버가 종료되는 문제 발생. exception handling 수정 필요.
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void acceptor(SelectableChannel selectedChannel) {
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) selectedChannel;

            SocketChannel clientChannel = serverChannel.accept(); //Connect Request from Client to Server

            if (clientChannel == null) {
                System.out.println("Null server socket.");
                return;
            }

            clientChannel.configureBlocking(false); //Change socketChannel from Blocking(default) to Non-Blocking State

            System.out.println("#socket accepted. Incoming connection from: " + clientChannel); // Test : server accepting new connection from new client

            clientChannel.register(selector, SelectionKey.OP_READ); //Register Client to Selector
            // TODO attach
            selector.wakeup();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void reader(SelectableChannel selectedChannel, SelectionKey key) {
        try {
            SocketChannel clientChannel = (SocketChannel) selectedChannel;

            int bytesCount = clientChannel.read(buf); // from client channel, read request msg and write into buffer

            if (bytesCount > 0) {
                buf.flip(); //make buffer ready to read

                byte[] requestMsgInBytes = new byte[buf.remaining()]; // NOTE(REFACTORING) : Process ByteBuffer into String to parse as a Http Msg
                buf.get(requestMsgInBytes); // Without this process, buf returns string not considering empty arrays

                System.out.println(new String(requestMsgInBytes)); //Test : Print out Http Request Msg
                buf.clear(); //clear away old info

                requestProcessor.process(key, requestMsgInBytes); // NOTE : Main Function to process http request
            } else {
                // TODO 여기서 하지말고 write 로 넘겨줘서 처리하기.
                System.out.println("read(): client connection might have been dropped!");

                int status = 400;
                buf = respondProcessor.createHeaderBuffer(status, 0); //NOTE: Buffer Size Need to be same as the buffer used in RespondProcessor
                clientChannel.write(buf);

                key.cancel();
                clientChannel.close();
                buf.clear();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void writer(SelectableChannel selectedChannel, SelectionKey key) {
        // TODO(REFACTORING): Buffer and related info
        try {
            SocketChannel clientChannel = (SocketChannel) selectedChannel;

            byte[] body = (byte[]) key.attachment();
            ByteBuffer headerBuffer = respondProcessor.createHeaderBuffer(200, body.length);
            // headerBuffer.flip();


            // if (body == null) {
            //     return; 리턴이 아니라 에러처리를 해야함. write 를 하는데 body 가 없는것도 아니고 null 이라면 앞에서 제대로 못붙여준거.
            // }
            buf.put(headerBuffer);
            buf.put(ByteBuffer.wrap(body));
            buf.flip();

            // headerBuffer.flip();
            byte[] requestMsgInBytes = new byte[buf.remaining()]; //Test : Print out Http Request Msg
            buf.get(requestMsgInBytes);
            System.out.println(new String(requestMsgInBytes));
            buf.flip();

            while (buf.hasRemaining()) {
                clientChannel.write(buf);
            }

            // if (numBytes > 0) {
            buf.clear();
            // write  이후 interestOp 가 read 로 바뀌지 않아서 write 무한 루프 이슈. 해결.
            // TODO connection 헤더에 따라 분리.
            clientChannel.close();
            key.selector().wakeup();
            // }


        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        MainController server = new MainController(Constants.PORT);
        new Thread(server).start();
    }
}
