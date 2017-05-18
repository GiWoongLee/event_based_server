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

public class HttpServer implements Runnable {
	private int port;

	private ServerSocketChannel server;
	private RequestProcessor requestProcessor;

	private ByteBuffer buf;
    //Test to attachment of Selector
    private ByteBuffer tempBuf;

	private Selector selector;
	private int socketOps = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
	
	public HttpServer(int port) throws IOException{

		//Open ServerSocketChannel to get request from users
		this.port = port;


		server = ServerSocketChannel.open();
		server.configureBlocking(false);

		InetAddress hostIPAddress = InetAddress.getByName("localhost");
		InetSocketAddress address = new InetSocketAddress(hostIPAddress,port);
		server.socket().bind(address);


		buf = ByteBuffer.allocate(24);
		tempBuf = ByteBuffer.allocate(24);
//		requestProcessor = new RequestProcessor();

		//register ssc into selector
		selector = Selector.open();
		server.register(selector,SelectionKey.OP_ACCEPT);
	}
	
	
	public void run(){
		try{
            System.out.println("Server Started");
            System.out.println("***************************");
            System.out.println("클라이언트의 접속을 기다리고 있습니다");
            System.out.println("***************************");
            while(true){
				int readyKeys = selector.select();
				if(readyKeys>0){
				    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();

					while(iter.hasNext()){
						SelectionKey key = iter.next();
                        iter.remove();

//                        System.out.println(key.isConnectable());
//                        System.out.println(key.isAcceptable());
//                        System.out.println(key.isReadable());
//                        System.out.println(key.isWritable());

						SelectableChannel selectedChannel = key.channel();
						//Connect Request from Client to Server
						if(selectedChannel instanceof ServerSocketChannel){
							ServerSocketChannel serverChannel = (ServerSocketChannel) selectedChannel;
							//Finding Newly-connected Channel
                            //Case1 : If not found, return null(As it is non-blocking async IO)
                            //Case2 : If found, connect client
							SocketChannel client = serverChannel.accept();
							if(client==null){
							    System.out.println("Null server socket");
								continue;
							}
							System.out.println("#socket accepted\n" + client);

							//Change socketChannel from Blocking(default) to Non-Blocking State
							client.configureBlocking(false);

							//Register Client to Selector to find out request/response from/to Client
							client.register(selector, socketOps);
						}

						//Find Request from Client/Response to Client
						else{
							SocketChannel client = (SocketChannel) selectedChannel;

							//Request from Client
                            //Channel에서 Buffer로 Byte들을 적은 다음, Buffer.get()으로 해당 data를 읽어들인다
							if(key.isReadable()){
							    //Test Case1
                                client.read(tempBuf);
                                tempBuf.flip();
                                key.attach(tempBuf);

                                //Test Case2
//							    int readStatus = client.read(buf);
//							    buf.flip();
//							    System.out.print("# Server read: ");
//							    if(readStatus!= -1){
//                                    while(buf.hasRemaining()){
//                                        System.out.print((char)buf.get());
//                                    }
//                                    System.out.println("\n");
//                                }
//							    buf.clear();
////                                  requestProcessor.process(key);
							}

							//Make Response to Client
                            //쓰고 싶은 내역을 Buffer.put()하고 Buffer에서 Channel로 정보를 read한다
							if(key.isWritable()){

							    //Test Case 1
							    ByteBuffer tempBuf2  = (ByteBuffer) key.attachment();
							    if(tempBuf2 == null) continue;
							    else {
							        client.write(tempBuf2);
							        key.attach(null);
                                }

                                //Test Case 2
//							    String temp = "Hello Client!";
//							    byte[] bytes = temp.getBytes();
//							    buf.clear();
//							    buf.put(bytes);
//							    buf.flip();
//							    client.write(buf);
//							    buf.clear();
//							    System.out.println("# Server write :" + temp);

							    //Heap Buffer Which is made from requestProcessor
//							    ByteBuffer responseBuffer = (ByteBuffer)key.attachment();
//							    client.write(responseBuffer);
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
        HttpServer server = new HttpServer(3000);
        new Thread(server).start();
    }
	
}
