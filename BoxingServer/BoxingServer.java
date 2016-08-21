package edu.galieo.boxing;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BoxingServer {
	static int PORT = 3141;
	static int MAX_THREADS = 100;
	private static ServerSocket server;
	public static String scoreBoard = "";
	public static void main(String[] args) throws IOException {
		
		
		//Rango del puerto válido [1024,49151]
		
		Socket tempSocket = null;	
		Socket p1 = null;
		Socket p2 = null;
		Socket p1a = null;
		Socket p2a = null;
		String player1 = "PL1";
		String player2 = "PL12";
		
		BufferedReader brd = new BufferedReader(new FileReader("ScoreBoard.txt"));
		try {
		    StringBuilder sb = new StringBuilder();
		    String line = brd.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = brd.readLine();
		    }
		    scoreBoard = sb.toString();
		} finally {
		    brd.close();
		}	
		
		System.out.println(scoreBoard);

		
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
			boolean a = true;
			ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);
			while(true) {
				tempSocket = server.accept();
				System.out.println(tempSocket.getInetAddress().getHostAddress());
				if (p1 == null) {					
					System.out.println("Welcome first player!");
					String messageOk = "OK 1";
					p1 = tempSocket;
					p1.getOutputStream().write(messageOk.getBytes());
					
					//stempSocket.getOutputStream().close();
					//DataOutputStream outToClient = new DataOutputStream(tempSocket.getOutputStream());
					//outToClient.writeBytes(messageOk);
				} else if (a & p1.getInetAddress().getHostAddress().equals(tempSocket.getInetAddress().getHostAddress())) {
					p1a = tempSocket;
					a = false;
//					InputStream is = p1a.getInputStream();
//		            int length = is.available();
//		            if (length > 0) { 
//			            byte[] bytes = new byte[length];
//			            //System.out.println(length);
//			            is.read(bytes, 0, length);
//			            player1 = new String(bytes);
//			            System.out.println("P1: " + player1);
//		            }   
					
					
				} else if (p2 == null) {
					p2 = tempSocket;
					System.out.println("Welcome second player!");
					String messageOk = "OK 2";
					p2.getOutputStream().write(messageOk.getBytes());
				} else if (p2.getInetAddress().getHostAddress().equals(tempSocket.getInetAddress().getHostAddress())) {
					p2a = tempSocket;
					
//					InputStream is1 = p2a.getInputStream();
//		            int length1 = is1.available();
//		            if (length1 > 0) { 
//			            byte[] bytes1 = new byte[length1];
//			            //System.out.println(length);
//			            is1.read(bytes1, 0, length1);
//			            player2 = new String(bytes1);
//			            System.out.println("P2: " + player2);
//		            }
			            
				} 
				
				if (p1 != null & p2 != null & p1a != null & p2a != null) {					
					
					GameRequest pr = new GameRequest(p1, p2,p1a, p2a, player1, player2);
					tempSocket = null;
					tempSocket = null;	
					p1 = null;
					p2 = null;
					p1a = null;
					p2a = null;
					pool.execute(pr);
					player1 = "";
					player2 = "";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	

}
