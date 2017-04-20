package event_based_server;

import java.io.IOException;
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

	private Selector selector;
	private int socketOps = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
	
	public HttpServer(int port) throws IOException{

		//Open ServerSocketChannel to get request from users
		this.port = port;


		server = ServerSocketChannel.open();
		server.configureBlocking(false);

		InetSocketAddress address = new InetSocketAddress(port);
		server.socket().bind(address);

		requestProcessor = new RequestProcessor();

		//register ssc into selector
		selector = Selector.open();
		server.register(selector,SelectionKey.OP_ACCEPT);
	}
	
	
	public void run(){
		try{
			while(true){
				System.out.println("Server Started");
				int readyKeys = selector.select();
				if(readyKeys>0){
					Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
					
					while(iter.hasNext()){
						SelectionKey key = iter.next();
						
						SelectableChannel selectedChannel = key.channel();

						//Connect Request from Client to Server
						if(selectedChannel instanceof ServerSocketChannel){
							ServerSocketChannel serverChannel = (ServerSocketChannel) selectedChannel;
							//Finding Newly-connected Channel
                            //Case1 : If not found, return null(As it is non-blocking async IO)
                            //Case2 : If found, connect client
							SocketChannel client = serverChannel.accept();
							if(client==null){
								continue;
							}

							//Change socketChannel from Blocking(default) to Non-Blocking State
							client.configureBlocking(false);

							//Register Client to Selector to find out request/response from/to Client
							client.register(selector, socketOps);
						}

						//Find Request from Client/Response to Client
						else{
							SocketChannel client = (SocketChannel) selectedChannel;

							//Request from Client
							if(key.isReadable()){
                                requestProcessor.process(key);
							}

							//Make Response to Client
							else if(key.isWritable()){
							    //Heap Buffer Which is made from requestProcessor
							    ByteBuffer responseBuffer = (ByteBuffer)key.attachment();
							    client.write(responseBuffer);
							}
						}

                        iter.remove();
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
	
}
