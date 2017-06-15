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

    private ByteBuffer readBuf;
    private ByteBuffer writeBuf;
    private byte[] readSlice;
    private byte[] writeSlice;

    private MainController(int port) throws IOException {
        selector = Selector.open();

        server = ServerSocketChannel.open();
        server.configureBlocking(false);

        InetAddress hostIPAddress = InetAddress.getByName(Constants.HOST);
        InetSocketAddress address = new InetSocketAddress(hostIPAddress, port);
        server.socket().bind(address, 300);

        server.register(selector, SelectionKey.OP_ACCEPT); //NOTE: register ssc into selector

        requestProcessor = new RequestProcessor();

        // FIXME 버퍼 하나로 전체 다 충분?
        readBuf = ByteBuffer.allocateDirect(Constants.MAIN_BUFFER_SIZE); //FIXME : Adjust buffer size with JMeter Test
        writeBuf = ByteBuffer.allocateDirect(Constants.MAIN_BUFFER_SIZE);

        readSlice = new byte[Constants.MAIN_BUFFER_SIZE];
        writeSlice = new byte[Constants.MAIN_BUFFER_SIZE];
    }

    public void run() {
        try {
            System.out.println("Server Started"); // Test: Message
            System.out.println("****************************");
            System.out.println("Waiting For Client Connection");
            System.out.println("****************************");
            while (!Thread.interrupted()) {
                int readyKeys = selector.select(Constants.PERIODIC_SELECT);
                // int readyKeys = selector.select();

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
                                request.setResponseHeader(ResponseProcessor.createHeaderBuffer(408));
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
            clientChannel.socket().setKeepAlive(true);

            // System.out.println("#socket accepted. Incoming connection from: " + clientChannel); // Test : server accepting new connection from new client

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

            int bytesCount = clientChannel.read(readBuf); // from client channel, read request msg and write into buffer

            if (bytesCount > 0) {
                readBuf.flip(); //make buffer ready to read

                System.out.println("#channel reading. Buffer as below: ");
                byte[] requestMsgInBytes = new byte[readBuf.remaining()]; // NOTE(REFACTORING) : Process ByteBuffer into String to parse as a Http Msg
                readBuf.get(requestMsgInBytes); // Without this process, buf returns string not considering empty arrays
                System.out.println(new String(requestMsgInBytes)); //Test : Print out Http Request Msg
                readBuf.clear(); //clear away old info

                requestProcessor.process(key, requestMsgInBytes, request); // NOTE : Main Function to process http request
            } else {
                System.out.println("read(): client connection might have been dropped!");

                request.setState(Request.ERROR);
                request.setResponseHeader(ResponseProcessor.createHeaderBuffer(500));
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
                writeBuf.put(request.getResponse());
            } else if (request.getResponse() != null) {
                ByteBuffer response = request.getResponse();
                if (Constants.MAIN_BUFFER_SIZE <= response.remaining()) {
                    response.get(writeSlice);
                    writeBuf.put(writeSlice);
                } else {
                    int remaining = response.remaining();
                    response.get(writeSlice, 0, remaining);
                    // response.flip();
                    // String s2 = Charset.forName("UTF-8").decode(response).toString();
                    // String s = new String(writeSlice);
                    writeBuf.put(writeSlice, 0, remaining);
                    // writeBuf.flip();
                    // writeBuf.flip();
                }
            } else {
                writeBuf.put(ResponseProcessor.createHeaderBuffer(500));
            }

            writeBuf.flip();

            System.out.println("#channel writing. Buffer as below: ");
            byte[] requestMsgInBytes = new byte[writeBuf.remaining()]; //Test : Print out Http Request Msg
            writeBuf.get(requestMsgInBytes);
            System.out.println(new String(requestMsgInBytes));
            writeBuf.flip();

            // while (writeBuf.hasRemaining()) {
            clientChannel.write(writeBuf);
            // }

            writeBuf.clear();

            if (request.getResponse().hasRemaining()) {
                key.interestOps(SelectionKey.OP_WRITE);
                key.selector().wakeup();
            } else {
                // TODO connection 헤더에 따라 분리.
                // keepalive 이면 interestOp read.
                // close 이면 sc.close()
                clientChannel.close();
                key.selector().wakeup();
                key.cancel();
                // TODO key.cancel() 필요하나?
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        MainController server = new MainController(Constants.PORT);
        new Thread(server).start();
    }
}
