package edu.galileo.boxing;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import javafx.application.Application;

public class Game implements Runnable {    
	private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 3141;

    private int counter = 90; //Descendent counter
	private int[] fpPosition, fpPunch, spPosition, spPunch, score;    
    private Socket receiverSocket, senderSocket;    

    public Game() {    	
    	this.fpPosition = new int[2];
    	this.fpPunch = new int[2];
        this.spPosition = new int[2];    	    	
    	this.spPunch = new int[2];
        this.score =new int[2];
        try {
            this.receiverSocket = new Socket(SERVER_IP, SERVER_PORT);
            print("Connection stablished to:" + SERVER_IP + ":" + SERVER_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {        
					try{
			           boolean ifGo = false;
			            boolean ifOk = false;
			            while (true) {
				            InputStream is = s.getInputStream();
				            int length = is.available();
				            if (length > 0) { 
					            byte[] bytes = new byte[length];
					            //System.out.println(length);
					            is.read(bytes, 0, length);
					            String inServer = new String(bytes);
					            if (inServer.equals("OK")){
					            	ifOk = true;
					            	sa = new Socket(IP,PORT);
					            	
					            	
					            }
					            if (inServer.equals("GO")){
				            		ifGo = true;
				            		continue;
				            	}
					            if (ifGo & ifOk ){
					            	
					            	
					            	parseBox(inServer);
					            	//System.out.println(desParser());
					            }
					                        
				            }           
			            }
					} catch (Exception e) {e.printStackTrace();}
				}
			});
            
            sync.start();
          
           while (true) {
        	   Scanner s = new Scanner(System.in);
        	   sendData(s.nextLine());
           }
            
        } catch(Exception e) {
            e.printStackTrace();           
        }
    }
    
    public static void sendData(String s) {
    	if (s == null) return;
    	try {
    		this.senderSocket.getOutputStream().write(s.getBytes());
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    public static void print(String msg) {
        System.out.println("BOXING CLIENT >> " + msg);
    }

    public static void parseBox (String sRead){
    	// parSeo instrucciones por ;
    	String instrucciones [] = sRead.split(";");
    	
    	//Saco los movimientos [x,y][a,b]]
    	String movimientos[] = instrucciones[0].split(",");
    	// [x,y]
    	String movimientosfp[] = movimientos[0].split("/");
    	fpPosition[0] = Integer.parseInt(movimientosfp[0]);
    	fpPosition[1] = Integer.parseInt(movimientosfp[1]);
    	// [a,b]
    	String movimientossp[] = movimientos[1].split("/");
    	spPosition[0] = Integer.parseInt(movimientossp[0]);
    	spPosition[1] = Integer.parseInt(movimientossp[1]);
    	// [contador]
    	
    	counter = Integer.parseInt(instrucciones[1]);
    	
    	//[score1][score2]
    	String scoretemp[] = instrucciones[2].split("/");
    	score[0] = Integer.parseInt(scoretemp[0]);
    	score[1] = Integer.parseInt(scoretemp[1]);
    	//[#,#][#,#]
    	String matrisgolpes[] = instrucciones[3].split(",");
    	String matrisgolpesusuario[] = matrisgolpes[0].split("/");
    	//[#,#]
    	fpPunch[0] = Integer.parseInt(matrisgolpesusuario[0]);
    	fpPunch[1] = Integer.parseInt(matrisgolpesusuario[1]);
    	String matrisgolpesusuario2[] = matrisgolpes[1].split("/");
    	spPunch[0] = Integer.parseInt(matrisgolpesusuario2[0]);
    	spPunch[1] = Integer.parseInt(matrisgolpesusuario2[1]);    	    	
    }
    
    public static String desParser (){
		String gameState = fpPosition[0] + "/" + fpPosition[1] + "," 
						   + spPosition[0] + "/" + spPosition[1] + ";";
		gameState += counter + ";";		
		gameState += score[0] + "/" + score[1] + ";";		
		gameState += fpPunch[0] + "/" + fpPunch[1] + "," 
				   + spPunch[0] + "/" + spPunch[1] + ";";
		return gameState;
    }    
}