import java.awt.Desktop.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.*;
import java.net.Socket;
import java.time.temporal.JulianFields;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import javax.swing.Timer;

public class GameRequest implements Runnable {
	static int GAME_TIME = 25;
	private Socket firstPlayer, secondPlayer, firstPlayerListener, secondPlayerListener;
	private int counter = GameRequest.GAME_TIME, gameId;
	private int[] fpPosition, spPosition, score, fpPunch, spPunch; 
	private String nombrej1 , nombrej2;	
	long startTime = 0;
	public GameRequest(Socket p1, Socket p2, Socket p1a, Socket p2a , String player1, String player2) {
		
		BoxingServer bS = new BoxingServer();
    	this.fpPosition = new int[2];
    	this.spPosition = new int[2];
    	this.score =new int[2];
    	this.fpPunch = new int[2];
    	this.spPunch = new int[2];
    	this.nombrej1 = player1;
    	this.nombrej2 = player2;
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
					if (startTime == 0) {
						startTime = System.currentTimeMillis();
					}
					long diff = System.currentTimeMillis() - startTime;
					counter = GAME_TIME - ((int)(diff / 1000));
					sendData();
				}
			};
			Timer sendDataTimer = new Timer(delay, taskPerformer);
			sendDataTimer.start();
			  
			ActionListener timer = new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (counter >= 0) {
						counter--;	
					}
				}
			};
			Timer counterTimer = new Timer(1000, timer);
			//counterTimer.start();
			  
			Thread t1 = new Thread(new Runnable() {				

			@SuppressWarnings("deprecation")
			@Override
				public void run() {
					try{
				           boolean ifGo = false;
				            boolean ifOk = false;
				            while (counter >= 0 && !firstPlayerListener.isClosed()) {								
								BufferedReader in = new BufferedReader(new InputStreamReader(firstPlayerListener.getInputStream()));								
                				String clientRequest = null;
								while ((clientRequest = in.readLine()) != null) {
									parseClientData(1,clientRequest);
								}								 
					            /*InputStream is = firstPlayerListener.getInputStream();
					            int length = is.available();
					            if (length > 0) { 
						            byte[] bytes = new byte[length];
						            //System.out.println(length);
						            is.read(bytes, 0, length);
						            String inServer = new String(bytes);
						            //System.out.println("P1: " + inServer);
						            parseClientData(1,inServer);
					            }*/          
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
					            while (counter >= 0 && !secondPlayerListener.isClosed()) {
									BufferedReader in = new BufferedReader(new InputStreamReader(secondPlayerListener.getInputStream()));
									String clientRequest = null;
									while ((clientRequest = in.readLine()) != null) {
										parseClientData(2,clientRequest);
									}
						            /*InputStream is = secondPlayerListener.getInputStream();
						            int length = is.available();
						            if (length > 0) { 
							            byte[] bytes = new byte[length];
							            //System.out.println(length);
							            is.read(bytes, 0, length);
							            String inServer = new String(bytes);
							            //System.out.println("P2: " + inServer);
							            parseClientData(2,inServer);
						            } */          
					            }
							} catch (Exception e) {e.printStackTrace();}
						
					}
				});
			  
			  t1.start();
			  
			  t2.start();
			  			  
			  while (true){
				  print(counter + "");				  
				  if (counter >= 0) continue;
				  sendDataTimer.stop();

				  String scoreBaux = BoxingServer.scoreBoard;
				  String[] scoreL = scoreBaux.split(";");
				  String paux = " ";
				  int puntaje;
				  if (score[0] > score[1]){
					  //gano p1
					  paux = this.nombrej1;
					  puntaje = this.score[0];
				  } else if (score[0] < score[1]) {
					  paux = this.nombrej2;
					  puntaje = this.score[1];
				  } else {
					  paux = "---";
					  puntaje = 0;
				  }
				  
				  this.firstPlayer.getOutputStream().write(("STOP;"+paux+"\r\n").getBytes());
				  this.secondPlayer.getOutputStream().write(("STOP;"+paux+"\r\n").getBytes());
				  
				 print("ENVIO STOP" +scoreBaux);
				  
				  if(scoreL[0].contains(paux)){
						String p21 = scoreL[0];
						String sp2 = p21.substring(0, p21.indexOf("/")).trim();
						int a = Integer.parseInt(sp2);
						a++;
						scoreL[0] = a+"/"+paux;
					} else if (scoreL[1].contains(paux)){
						String p21 = scoreL[1];
						String sp2 = p21.substring(0, p21.indexOf("/")).trim();
						int a = Integer.parseInt(sp2);
						a++;
						scoreL[1] = a+"/"+paux;
					} else if (scoreL[2].contains(paux)){
						String p21 = scoreL[2];
						String sp2 = p21.substring(0, p21.indexOf("/")).trim();
						int a = Integer.parseInt(sp2);
						a++;
						scoreL[2] = a+"/"+paux;
						
					} else if (scoreL[3].contains(paux)){
						String p21 = scoreL[3];
						String sp2 = p21.substring(0, p21.indexOf("/")).trim();
						int a = Integer.parseInt(sp2);
						a++;
						scoreL[3] = a+"/"+paux;
					} else if (scoreL[4].contains(paux)){
						String p21 = scoreL[4];
						String sp2 = p21.substring(0, p21.indexOf("/")).trim();
						int a = Integer.parseInt(sp2);
						a++;
						scoreL[4] = a+"/"+paux;
						
					} else if (scoreL[5].contains(paux)){
						String p21 = scoreL[5];
						String sp2 = p21.substring(0, p21.indexOf("/")).trim();
						int a = Integer.parseInt(sp2);
						a++;
						scoreL[5] = a+"/"+paux;
						
					} else if (scoreL[6].contains(paux)){
						String p21 = scoreL[6];
						String sp2 = p21.substring(0, p21.indexOf("/")).trim();
						int a = Integer.parseInt(sp2);
						a++;
						scoreL[6] = a+"/"+paux;
						
					} else if (scoreL[7].contains(paux)){
						String p21 = scoreL[7];
						String sp2 = p21.substring(0, p21.indexOf("/")).trim();
						int a = Integer.parseInt(sp2);
						a++;
						scoreL[7] = a+"/"+paux;
						
					} else if (scoreL[8].contains(paux)){
						String p21 = scoreL[8];
						String sp2 = p21.substring(0, p21.indexOf("/")).trim();
						int a = Integer.parseInt(sp2);
						a++;
						scoreL[8] = a+"/"+paux;
						
					} else if (scoreL[9].contains(paux)){
						String p21 = scoreL[9];
						String sp2 = p21.substring(0, p21.indexOf("/")).trim();
						int a = Integer.parseInt(sp2);
						a++;
						scoreL[9] = a+"/"+paux;
						
					} else {
						
						scoreL[9] = "1"+"/"+paux;
					}
					Arrays.sort(scoreL, Collections.reverseOrder());
					
					String scoreF = scoreL[0]+";"+scoreL[1]+";"+scoreL[2]+";"+scoreL[3]+";"+scoreL[4]
								+";"+scoreL[5]+";"+scoreL[6]+";"+scoreL[7]+";"+scoreL[8]+";"+scoreL[9];
				  
					BufferedWriter writer = null;
					
					FileWriter fstream = new FileWriter("ScoreBoard.txt");
					writer = new BufferedWriter(fstream);
					writer.write(scoreF);
					writer.close();
					BoxingServer.scoreBoard = scoreF;
					print(("SCOREBOARD " + scoreF));
					this.firstPlayer.getOutputStream().write(("SCOREBOARD " + scoreF).getBytes());
					this.secondPlayer.getOutputStream().write(("SCOREBOARD " + scoreF).getBytes());
					break;
				    
				  
			  }
			  //counterTimer.stop();
			  
			  
			  this.firstPlayer.close();
			  this.secondPlayer.close();
			  this.firstPlayerListener.close();
			  this.secondPlayerListener.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	

	private void parseClientData(int p, String s) {
		//
		String[] instrucciones = s.split(";");
		if (instrucciones.length >= 2) {
			String[] posicion = instrucciones[0].split("/");
			
			String[] punch = instrucciones[1].split("/");
			
			int posAux [] = new int[5] ;
			
			
			posAux[0] = Integer.parseInt(posicion[0]);
			posAux[1] = Integer.parseInt(posicion[1]);
			
			
			
			if (p == 1) {				
				// bordes y pos jugador 1
				if(posAux[0] <= 25){
					fpPosition[0] = fpPosition[0]+2;
				} else if (posAux[0] >= 775){
					fpPosition[0] = fpPosition[0]-2;
				} else if (posAux[0]+50 >= spPosition[0]){
					
					fpPosition[0] = fpPosition[0]-2;
				}else {
					fpPosition[0] = posAux[0];
				}
				
				if(posAux[1] <= 25){
					fpPosition[1] = fpPosition[1] + 2;
				} else if (posAux[1] >= 562){
					fpPosition[1] = fpPosition[1] - 2;
				} else {
					fpPosition[1] = posAux[1];
				}
				
				
				fpPunch[0] = Integer.parseInt(punch[0]);
				fpPunch[1] = Integer.parseInt(punch[1]);
				
				if (fpPunch[0] == 1 | fpPunch[1] == 1){
					if (posAux[0]+100 >= spPosition[0]){
						if (posAux[1]+56 <= spPosition[1] + 71 & posAux[1] + 56 >= spPosition[1] + 41) {
							score[0]++;
						}
							
					}
				}
				
				
			} else {
				
				// bordes y pos jugador 2 
				if(posAux[0] <= 25){
					spPosition[0] = spPosition[0] + 2;
				} else if (posAux[0] >= 775){
					spPosition[0] = spPosition[0] - 2;
				} else if (posAux[0] <= fpPosition[0]+20) {
					spPosition[0] = spPosition[0] + 2;
				}else {
					spPosition[0] = posAux[0];
				}
				
				if(posAux[1] <= 25){
					spPosition[1] = spPosition[1] + 2;
				} else if (posAux[1] >= 562){
					spPosition[1] = spPosition[1] - 2;
				} else {
					spPosition[1] = posAux[1];
				}
				
				spPunch[0] = Integer.parseInt(punch[0]);
				spPunch[1] = Integer.parseInt(punch[1]);
				if (spPunch[0] == 1 | spPunch[1] == 1){
					if (posAux[0]  <= fpPosition[0] +50 ){
						if (posAux[1]+56 <= fpPosition[1] + 70 & posAux[1] + 56 >= fpPosition[1] + 41) {
							score[1]++;
						}
							
					}
				}
			}
			
		} else {
			if (p == 1 ) {
			this.nombrej1 = s;
			
			} else {
				this.nombrej2 = s;
			}
		}
		
	}
	
	private void sendData() {
		String gameState = this.fpPosition[0] + "/" + this.fpPosition[1] + "," 
						   + this.spPosition[0] + "/" + this.spPosition[1] + ";";
		
		gameState += Math.max(this.counter,0) + ";";
		
		gameState += this.score[0] + "/" + this.score[1] + ";";
		
		gameState += this.fpPunch[0] + "/" + this.fpPunch[1] + "," 
				   + this.spPunch[0] + "/" + this.spPunch[1] + ";";
		
		try {
			PrintWriter out1 = new PrintWriter(firstPlayer.getOutputStream(), true);                
			out1.print(gameState + "\r\n");
			out1.flush();

			PrintWriter out2 = new PrintWriter(secondPlayer.getOutputStream(), true);
			out2.print(gameState + "\r\n");
			out2.flush();
			//this.firstPlayer.getOutputStream().write(gameState.getBytes());
			//this.secondPlayer.getOutputStream().write(gameState.getBytes());
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

	public static void print(String msg) {
		//System.out.println("SERVER >> " + msg);
	}	
}