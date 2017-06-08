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
	private int port;

	private ServerSocketChannel server;
	private RequestProcessor requestProcessor;
	private RespondProcessor respondProcessor;

	private ByteBuffer buf;

	private Selector selector;
	private int socketOps = SelectionKey.OP_READ | SelectionKey.OP_WRITE;

	
	public MainController(int port) throws IOException{

		this.port = port;

		server = ServerSocketChannel.open();
		server.configureBlocking(false);

		InetAddress hostIPAddress = InetAddress.getByName("localhost"); //FIXME : Fix Server Name
		InetSocketAddress address = new InetSocketAddress(hostIPAddress, port);
		server.socket().bind(address);

        // FIXME 버퍼 할당이 connection 별로 되는게 아니라 서버 전체에서 하나만 되고있음.
		buf = ByteBuffer.allocate(2048); //FIXME : Adjust buffer size with JMeter Test

		requestProcessor = new RequestProcessor();
		respondProcessor = new RespondProcessor();

		selector = Selector.open();
		server.register(selector, SelectionKey.OP_ACCEPT); //NOTE: register ssc into selector
	}


	public void run(){
		try{
            System.out.println("Server Started"); // Test: Message
            System.out.println("****************************");
            System.out.println("Waiting For Client Connection");
            System.out.println("****************************");
            while(true) {
				int readyKeys = selector.select();
				if(readyKeys > 0){
				    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();

					while(iter.hasNext()){
						SelectionKey key = iter.next();
                        iter.remove();

						SelectableChannel selectedChannel = key.channel();


						if(selectedChannel instanceof ServerSocketChannel) {
							ServerSocketChannel serverChannel = (ServerSocketChannel) selectedChannel;

							SocketChannel clientChannel = serverChannel.accept(); //Connect Request from Client to Server

							if(clientChannel == null) {
							    System.out.println("Null server socket.");
								continue;
							}

							System.out.println("#socket accepted. Incoming connection from: \n" + clientChannel); // Test : server accepting new connection from new client

							clientChannel.configureBlocking(false); //Change socketChannel from Blocking(default) to Non-Blocking State

							clientChannel.register(selector, socketOps); //Register Client to Selector
						}

						else{
							SocketChannel clientChannel = (SocketChannel) selectedChannel;

							if(key.isReadable()) {
								int bytesCount = clientChannel.read(buf); // from client channel, read request msg and write into buffer
								if (bytesCount > 0) {
									buf.flip(); //make buffer ready to read

									byte[] requestMsgInBytes = new byte[buf.remaining()]; // NOTE(REFACTORING) : Process ByteBuffer into String to parse as a Http Msg
									buf.get(requestMsgInBytes); // Without this process, buf returns string not considering empty arrays

									System.out.println(new String(requestMsgInBytes)); //Test : Print out Http Request Msg
									buf.clear(); //clear away old info

									requestProcessor.process(key,requestMsgInBytes); // NOTE : Main Function to process http request
								}
								else{
									int status = 400;
									buf = respondProcessor.createHeaderBuffer(status); //NOTE: Buffer Size Need to be same as the buffer used in RespondProcessor
									clientChannel.write(buf);
									buf.clear();
								}
							}


//							TODO(REFACTORING): Buffer and related info
							if(key.isWritable()){
							    byte[] res  = (byte[]) key.attachment();
							    if(res == null) continue;
							    else {
							    	ByteBuffer headerBuffer = respondProcessor.createHeaderBuffer(200);
							    	headerBuffer.rewind();
							    	buf.put(headerBuffer);
							    	buf.put(res);
							    	buf.flip();

									byte[] requestMsgInBytes = new byte[headerBuffer.remaining()]; //Test : Print out Http Request Msg
									headerBuffer.get(requestMsgInBytes);
									System.out.println(new String(requestMsgInBytes));

									clientChannel.write(buf);

									buf.clear();
							        key.attach(null);
                                    //	write  이후 interestOp 가 read 로 바뀌지 않아서 write 무한 루프 이슈. 해결.
                                    key.interestOps(SelectionKey.OP_READ);
                                }
							}
						}
					}
				}
			}
		} catch(IOException e){
			e.printStackTrace();
		} finally{
			try{
				server.close();
			} catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException {
        MainController server = new MainController(8080);
        new Thread(server).start();
    }
	
}
