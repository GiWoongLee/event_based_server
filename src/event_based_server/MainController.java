package event_based_server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class MainController implements Runnable {
    private Selector selector;

    private ServerSocketChannel server;

    private RequestProcessor requestProcessor;

    private ByteBuffer buf;

    private MainController(int port) throws IOException {
        selector = Selector.open();

        server = ServerSocketChannel.open();
        server.configureBlocking(false);

        InetAddress hostIPAddress = InetAddress.getByName(Constants.HOST);
        InetSocketAddress address = new InetSocketAddress(hostIPAddress, port);
        server.socket().bind(address);

        server.register(selector, SelectionKey.OP_ACCEPT); //NOTE: register ssc into selector

        requestProcessor = new RequestProcessor();

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
                int readyKeys = selector.select(Constants.PERIODIC_SELECT);

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

                        // request timeout
                        if (key.attachment() instanceof Request) {
                            Request request = (Request) key.attachment();
                            if (System.currentTimeMillis() > request.getRequestStartTime() + Constants.REQUEST_TIMEOUT_MILLIS) {
                                System.out.println("This request is Time over!!");
                                request.setState(Request.ERROR);
                                request.setResponseHeader(RespondProcessor.createHeaderBuffer(408, 0));
                                writer(selectedChannel, key);
                                continue;
                            }
                        }

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
            selector.wakeup();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void reader(SelectableChannel selectedChannel, SelectionKey key) {
        try {
            SocketChannel clientChannel = (SocketChannel) selectedChannel;

            // if (key.attachment() != null) {
            Request request = new Request();
            request.setState(Request.READ);
            // }

            int bytesCount = clientChannel.read(buf); // from client channel, read request msg and write into buffer

            if (bytesCount > 0) {
                buf.flip(); //make buffer ready to read

                System.out.println("#channel reading. Buffer as below: ");
                byte[] requestMsgInBytes = new byte[buf.remaining()]; // NOTE(REFACTORING) : Process ByteBuffer into String to parse as a Http Msg
                buf.get(requestMsgInBytes); // Without this process, buf returns string not considering empty arrays
                System.out.println(new String(requestMsgInBytes)); //Test : Print out Http Request Msg
                buf.clear(); //clear away old info

                requestProcessor.process(key, requestMsgInBytes, request); // NOTE : Main Function to process http request
            } else {
                System.out.println("read(): client connection might have been dropped!");

                request.setState(Request.ERROR);
                request.setResponseHeader(RespondProcessor.createHeaderBuffer(500, 0));
                key.attach(request); //NOTE: send error message to event queue

                key.interestOps(SelectionKey.OP_WRITE);
                key.selector().wakeup();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void writer(SelectableChannel selectedChannel, SelectionKey key) {
        // TODO(REFACTORING): Buffer and related info
        try {
            SocketChannel clientChannel = (SocketChannel) selectedChannel;

            // TODO 장기적으로 attachment 를 클래스로 관리해서 status 등 여러 정보를 받아와야 함.
            Request request = (Request) key.attachment();
            if (request.getState() == Request.ERROR) {
                buf.put(request.getResponseHeader());
            } else if (request.getData() != null && request.getResponseHeader() != null) {
                buf.put(request.getResponseHeader());
                buf.put(ByteBuffer.wrap(request.getData()));
            } else {
                buf.put(RespondProcessor.createHeaderBuffer(500, 0));
            }

            buf.flip();

            System.out.println("#channel writing. Buffer as below: ");
            byte[] requestMsgInBytes = new byte[buf.remaining()]; //Test : Print out Http Request Msg
            buf.get(requestMsgInBytes);
            System.out.println(new String(requestMsgInBytes));
            buf.flip();

            while (buf.hasRemaining()) {
                clientChannel.write(buf);
            }

            buf.clear();
            // TODO connection 헤더에 따라 분리.
            // keepalive 이면 interestOp read.
            // close 이면 sc.close()
            clientChannel.close();
            key.selector().wakeup();
            // TODO key.cancel() 필요하나?
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        MainController server = new MainController(Constants.PORT);
        new Thread(server).start();
    }
}
