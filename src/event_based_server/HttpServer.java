package event_based_server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.Iterator;

public class HttpServer extends Thread {
	private int port;
	// Queue for events from channels
	//Not sure to store channel or events, parsed something
	
	private ServerSocketChannel ssc;
	
	private Selector acceptSelector;
	private int socketOps = SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE;
	
	public HttpServer(int port) throws IOException{
		this.port = port;
		
		//make new 
		ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		
		//
		InetSocketAddress address = new InetSocketAddress(port);
		ssc.socket().bind(address);
		
		//register ssc into selector
		acceptSelector = Selector.open();
		ssc.register(acceptSelector,SelectionKey.OP_ACCEPT);
	}
	
	
	public void run(){
		try{
			while(true){
				System.out.println("Server Started");
				int numKeys = acceptSelector.select();
				if(numKeys>0){
					//현재 Selector에 ServerSocketChanne만 등록되어, 새로운 client 접속 시도만 처리
					//향후 SocketChannel에서 Selector에 다른 ops를 등록하면, 단순히 ServerSocketChannel만 return해주거나 accept 함수만을 작업하지는 않는 
					Iterator<SelectionKey> iter = acceptSelector.selectedKeys().iterator();
					
					while(iter.hasNext()){
						//SelectionKey를 넘나들며 작업이 이뤄진 후에는 이을 지워준다.
						SelectionKey key = iter.next();
						iter.remove();
						
						SelectableChannel selectedChannel = key.channel();
						//ServerSocketChannel
						if(selectedChannel instanceof ServerSocketChannel){
							ServerSocketChannel serverChannel = (ServerSocketChannel) selectedChannel;
							SocketChannel newlyAcceptedChannel = serverChannel.accept();
							//ServerSocketChannel이 Non-Blocking이라 Null을 return한다
							if(newlyAcceptedChannel==null){
								continue;
							}
							//Blocking Mode가 Default로 되어 있기에 수정 필요
							newlyAcceptedChannel.configureBlocking(false);
							// CONNECT로 설정해놓고, HTTP 요청이 오면 이를 변경해서 READ/WRITE로 바꿔주기
							newlyAcceptedChannel.register(acceptSelector, socketOps);
						}
						//SocketChannel
						else{
							SocketChannel socketChannel = (SocketChannel) selectedChannel;
							if(key.isConnectable()){
								//Connect 해주고 기능을 READ/WRITE로 설정
								boolean connected = socketChannel.finishConnect();
								if(connected){
									
								}
								else{
									continue;
								}
							}
							else if(key.isReadable()){
								//HTTP parsing 작업 필요할듯, buf로 정보를 주고 받는 듯
								//SocketChannel.read(buf)
							}
							else if(key.isWritable()){
								//HTTP parsing 작업 필요할듯, buf로 정보를 주고 받는 듯
								//SocketChannel.write(buf)
							}
							else{
								//이런 경우도 있나...
							}
						}
					}
				}
			}
		} catch(IOException e){
			e.printStackTrace();
		} finally{
			try{
				ssc.close();
			} catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
}
