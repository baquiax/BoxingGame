import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.galieo.boxing.GameRequest;

public class BoxingServer {
	static int PORT = 3141;
	static int MAX_THREADS = 100;
	private static ServerSocket server;
	
	
	public static void main(String[] args) {
		//Rango del puerto válido [1024,49151]
		
		Socket tempSocket = null;	
		Socket p1 = null;
		Socket p2 = null;
		Socket p1a = null;
		Socket p2a = null;
		try {
			 Runtime.getRuntime().addShutdownHook(new Thread() {
	                public void run() {
	                    try {                
	                    	server.close();
	                        System.out.println("Close server ...");        
	                    } catch (Exception ex) {
	                        // TODO Auto-generated catch block
	                        ex.printStackTrace();
	                    }
	                }
	            });
			 
			server = new ServerSocket(PORT);
			System.out.println("Server started");
			ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);
			while(true) {
				tempSocket = server.accept();
				System.out.println(tempSocket.getInetAddress().getHostAddress());
				if (p1 == null) {					
					System.out.println("Welcome first player!");
					String messageOk = "OK";
					p1 = tempSocket;
					p1.getOutputStream().write(messageOk.getBytes());			
					//stempSocket.getOutputStream().close();
					//DataOutputStream outToClient = new DataOutputStream(tempSocket.getOutputStream());
					//outToClient.writeBytes(messageOk);
				} else if (p1.getInetAddress().getHostAddress().equals(tempSocket.getInetAddress().getHostAddress())) {
					
					p1a = tempSocket;
					
				} else if (p2 == null) {
					p2 = tempSocket;
					System.out.println("Welcome second player!");
					String messageOk = "OK";
					p2.getOutputStream().write(messageOk.getBytes());
				} else if (p2.getInetAddress().getHostAddress().equals(tempSocket.getInetAddress().getHostAddress())) {
					p2a = tempSocket;
				} 
				
				if (p1 != null & p2 != null & p1a != null & p2a != null) {					
					
					GameRequest pr = new GameRequest(p1, p2,p1a, p2a);
					tempSocket = null;
					tempSocket = null;	
					p1 = null;
					p2 = null;
					p1a = null;
					p2a = null;
					pool.execute(pr);
					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
