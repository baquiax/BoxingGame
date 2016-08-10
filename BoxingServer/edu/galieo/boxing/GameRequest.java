package edu.galieo.boxing;
import java.awt.Desktop.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.net.Socket;

import javax.swing.Timer;

public class GameRequest implements Runnable {
	private Socket firstPlayer, secondPlayer, firstPlayerListener, secondPlayerListener;
	private int counter = 90, gameId;
	private int[] fpPosition, spPosition, score, fpPunch, spPunch; 
	public GameRequest(Socket p1, Socket p2, Socket p1a, Socket p2a ) {
    	this.fpPosition = new int[2];
    	this.spPosition = new int[2];
    	this.score =new int[2];
    	this.fpPunch = new int[2];
    	this.spPunch = new int[2];
		
		this.firstPlayer = p1;
		this.secondPlayer = p2;
		this.firstPlayerListener =p1a;
		this.secondPlayerListener =p2a;
		this.fpPosition[0] = 300;
		this.spPosition[0] = 500;
		this.fpPosition[1] = this.spPosition[1] = 300;				
	}
	
	public void run() {
		String go = "GO";
		
		try {
			this.firstPlayer.getOutputStream().write(go.getBytes());
			this.secondPlayer.getOutputStream().write(go.getBytes());
			
			 int delay = 100; //milliseconds
			  ActionListener taskPerformer = new ActionListener() {
			      public void actionPerformed(ActionEvent evt) {
			    	  //System.out.println("Notificando a los players. Timer:" + counter);
			    	  sendData();
			      }
			  };
			  new Timer(delay, taskPerformer).start();
			  
			  ActionListener timer = new ActionListener() {
			      public void actionPerformed(ActionEvent evt) {
			    	  counter--;
			      }
			  };
			  new Timer(1000, timer).start();
			  
			  Thread t1 = new Thread(new Runnable() {
				
				@Override
				public void run() {
					try{
				           boolean ifGo = false;
				            boolean ifOk = false;
				            while (true) {
					            InputStream is = firstPlayerListener.getInputStream();
					            int length = is.available();
					            if (length > 0) { 
						            byte[] bytes = new byte[length];
						            //System.out.println(length);
						            is.read(bytes, 0, length);
						            String inServer = new String(bytes);
						            System.out.println("P1: " + inServer);
						            parseClientData(1,inServer);
					            }           
				            }
						} catch (Exception e) {e.printStackTrace();}
					
				}
			});
			  
			  Thread t2 = new Thread(new Runnable() {
					
					@Override
					public void run() {
						try{
					           boolean ifGo = false;
					            boolean ifOk = false;
					            while (true) {
						            InputStream is = secondPlayerListener.getInputStream();
						            int length = is.available();
						            if (length > 0) { 
							            byte[] bytes = new byte[length];
							            //System.out.println(length);
							            is.read(bytes, 0, length);
							            String inServer = new String(bytes);
							            System.out.println("P2: " + inServer);
							            parseClientData(2,inServer);
						            }           
					            }
							} catch (Exception e) {e.printStackTrace();}
						
					}
				});
			  
			  t1.start();
			  t2.start();
			  
		} catch (Exception e) {
			
		}
	}	

	private void parseClientData(int p, String s) {
		//
		String[] instrucciones = s.split(";");
		if (instrucciones.length >= 2) {
			String[] posicion = instrucciones[0].split("/");
			String[] punch = instrucciones[1].split("/");
			if (p == 1) {
				fpPosition[0] = Integer.parseInt(posicion[0]);
				fpPosition[1] = Integer.parseInt(posicion[1]);
				fpPunch[0] = Integer.parseInt(punch[0]);
				fpPunch[1] = Integer.parseInt(punch[1]);
				if (fpPunch[0] == spPosition[0] && fpPunch[1] == spPosition[1]) {
					score[0]++;
				}
			} else {
				spPosition[0] = Integer.parseInt(posicion[0]);
				spPosition[1] = Integer.parseInt(posicion[1]);
				spPunch[0] = Integer.parseInt(punch[0]);
				spPunch[1] = Integer.parseInt(punch[1]);
				if (spPunch[0] == fpPosition[0] && spPunch[1] == fpPosition[1]) {
					score[1]++;
				}
			}
			
		}
		
	}
	
	private void sendData() {
		String gameState = this.fpPosition[0] + "/" + this.fpPosition[1] + "," 
						   + this.spPosition[0] + "/" + this.spPosition[1] + ";";
		
		gameState += this.counter + ";";
		
		gameState += this.score[0] + "/" + this.score[1] + ";";
		
		gameState += this.fpPunch[0] + "/" + this.fpPunch[1] + "," 
				   + this.spPunch[0] + "/" + this.spPunch[1] + ";";
		
		try {
			this.firstPlayer.getOutputStream().write(gameState.getBytes());
			this.secondPlayer.getOutputStream().write(gameState.getBytes());
		} catch(Exception e) {
			e.printStackTrace();
		}		
	}
	
	public void setSecondPlayerListener(Socket s) {
		this.secondPlayerListener = s;
	}
	
	public void setFirstPlayerListener(Socket s) {
		this.firstPlayerListener = s;
	}
		
}
