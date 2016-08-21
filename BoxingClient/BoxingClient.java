import javafx.animation.TranslateTransition;
import edu.galileo.boxing.Player;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;

import com.sun.javafx.geom.Rectangle;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;


public class BoxingClient extends Application {
	private static final int		KEYBOARD_MOVEMENT_DELTA = 20;
    private static final Duration	TRANSLATE_DURATION      = Duration.seconds(0.25);
	private static final String		SERVER_IP 				= "192.168.0.20";
    private static final int		SERVER_PORT 			= 3141;

	private static int 				counter = 90; //Descendent counter
	private static int[] 			fpPosition, fpPunch, spPosition, spPunch, score;    
    private static Socket 			receiverSocket, senderSocket;  	
	private static Player			player1;
	private static Player			player2;
	private static Label			player1Score;
	private static Label			player2Score;
	private static Label			timer;
	private static Label			result;
	private static boolean			isFirstPlayer;
	
	
    public static void main(String[] args) {		
		BoxingClient.fpPosition = new int[2];
    	BoxingClient.fpPunch = new int[2];
        BoxingClient.spPosition = new int[2];    	    	
    	BoxingClient.spPunch = new int[2];
        BoxingClient.score =new int[2];
        
        BoxingClient.player1 = new Player("img/p1sp.png");		
		BoxingClient.player2 = new Player("img/p2sp.png");	
		
		player1Score = new Label("0");
		player1Score.setFont((new Font("Arial", 30)));
		player1Score.setLayoutX(15);
		player1Score.setLayoutY(15);
		
		player2Score = new Label("0");
		player2Score.setFont((new Font("Arial", 30)));
		player2Score .setLayoutX(855);
		player2Score .setLayoutY(15);
		
		
		timer = new Label("00");
		timer.setFont((new Font("Arial", 30)));
		timer.setLayoutX(435);
		timer.setLayoutY(15);
		
		 result = new Label("");
		 result.setFont((new Font("Arial", 60)));
		 result.setTextFill(Color.INDIANRED);		 
		 result.setLayoutX(320);
		 result.setLayoutY(100);
		 result.setVisible(false);
		
		try {
            BoxingClient.receiverSocket = new Socket(SERVER_IP, SERVER_PORT);
            print("Connection stablished to:" + SERVER_IP + ":" + SERVER_PORT);
			Thread receiverThread = new Thread(new Runnable() {
				@Override
    			public void run() {
					try {
						boolean okReceived = false;
						boolean goReceived = false;
			            
			            while (true) {							
				            InputStream is = receiverSocket.getInputStream();
				            int length = is.available();
				            if (length > 0) { 
					            byte[] bytes = new byte[length];					            
					            is.read(bytes, 0, length);
					            String inServer = new String(bytes);

					            if (inServer.startsWith("OK")) {
					            	okReceived = true;
					            	BoxingClient.senderSocket = new Socket(SERVER_IP, SERVER_PORT);					            	
					            	isFirstPlayer = inServer.contains("1");					            	
									print("Second socket created!");
					            }
					            if (inServer.equals("GO")) {
				            		goReceived = true;
				            		continue;
				            	}
					            if (okReceived & goReceived) {					            						            	
					            	parseBox(inServer);					            	
					            }					                        
				            }           
			            }
					} catch (Exception e) {e.printStackTrace();}
				}				        
			});
			receiverThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        } 		 
		launch(args);
    }
    
    @Override 
	public void start(Stage stage) throws Exception {    	
    	stage.setTitle("Boxing game!");
    	Group root = new Group();
    	Scene sceneRoot = new Scene(root);
        sceneRoot.setOnKeyPressed(
                new EventHandler<KeyEvent>()
                {
                    public void handle(KeyEvent event)
                    {                        
     
                    	Player currentPlayer = (isFirstPlayer) ? player1 : player2;
                        double x = currentPlayer.getImageView().getX();
        				double y = currentPlayer.getImageView().getY();

        				switch (event.getCode()) {
        					case UP:    
        						//circle.setCenterY(circle.getCenterY() - KEYBOARD_MOVEMENT_DELTA); break;
        						y -= KEYBOARD_MOVEMENT_DELTA; 
        						BoxingClient.sendData((int)x + "/" + (int)y + ";0/0");
        						break;
        					case RIGHT: 
        						//circle.setCenterX(circle.getCenterX() + KEYBOARD_MOVEMENT_DELTA); break;
        						x += KEYBOARD_MOVEMENT_DELTA; 
        						BoxingClient.sendData((int)x + "/" + (int)y + ";0/0");
        						break;
                  			case DOWN:  
        					  	//circle.setCenterY(circle.getCenterY() + KEYBOARD_MOVEMENT_DELTA); break;
        						y += KEYBOARD_MOVEMENT_DELTA; 
        						BoxingClient.sendData((int)x + "/" + (int)y + ";0/0");
        						break;
                  			case LEFT:  
        					  	//circle.setCenterX(circle.getCenterX() - KEYBOARD_MOVEMENT_DELTA); break;
        						x -= KEYBOARD_MOVEMENT_DELTA; 
        						BoxingClient.sendData((int)x + "/" + (int)y + ";0/0");
        						break;        						
                  			case Q:
                  				BoxingClient.sendData((int)x + "/" + (int)y + ((isFirstPlayer) ? ";1/0" : ";0/1")); 
                  				break;
                  			case A:                  				
                  				BoxingClient.sendData((int)x + "/" + (int)y + ((isFirstPlayer) ? ";0/1" : ";1/0"));
                  				break;
                  			
                		}			
                       
                    }
                });
     
            sceneRoot.setOnKeyReleased(
                new EventHandler<KeyEvent>()
                {
                    public void handle(KeyEvent e)
                    {
                    
                    }
                });
            
    	stage.setScene(sceneRoot);
    	
    	Canvas c = new Canvas(900,700);
    	
    	root.getChildren().add(c);
    					
    	ImageView fondo = new ImageView(new Image("img/arena.png"));
    	fondo.setViewport(new Rectangle2D(0, 0, 900, 700));
    	root.getChildren().add(fondo);
		GraphicsContext gc = c.getGraphicsContext2D();
		
		
		root.getChildren().add(player1.getImageView());		
		root.getChildren().add(player2.getImageView());
		root.getChildren().add(player1Score);
		root.getChildren().add(player2Score);
		root.getChildren().add(timer);
		root.getChildren().add(result);
		
		player1.reset();
		player2.reset();
		stage.show();		
	}  

	//Gaming
	public static void parseBox (String sRead){		
		print("Parsebox: " + sRead + "\r\n");
		
		if (sRead.toUpperCase().startsWith("STOP")) {
			//Mostrar ganador.
		
			Platform.runLater(new Runnable() {
  		      @Override public void run() {
  		    	  result.setText("WINNER: " + sRead.split(";")[1]);
  		    	  result.setVisible(true);
  		      }
			});  		      
			return;
		}
		
		if (sRead.toUpperCase().startsWith("SCOREBOARD")) {
			//Mostrar Scoreboard
			
			return;
		}
		
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
    	
    	if (player1Score != null && player2Score != null && timer != null) {
    		Platform.runLater(new Runnable() {
    		      @Override public void run() {
    		    	  String timerString = String.format("%02d", counter);
    		    	  timer.setText(timerString);
    		    	  player1Score.setText(scoretemp[0]);
    		    	  player2Score.setText(scoretemp[1]);    		
    		      }
    		});
    	}
    	
    	//[#,#][#,#]
    	String matrisgolpes[] = instrucciones[3].split(",");
    	String matrisgolpesusuario[] = matrisgolpes[0].split("/");
    	//[#,#]
    	fpPunch[0] = Integer.parseInt(matrisgolpesusuario[0]);
    	fpPunch[1] = Integer.parseInt(matrisgolpesusuario[1]);
    	
    	if (fpPunch[0] == 1) {
    		player1.leftPunch();
    	} else if(fpPunch[1] == 1) {
    		player1.rightPunch();
    	} else if (player1 != null) {    		
    		player1.reset();
    	}
    	
    	String matrisgolpesusuario2[] = matrisgolpes[1].split("/");
    	spPunch[0] = Integer.parseInt(matrisgolpesusuario2[0]);
    	spPunch[1] = Integer.parseInt(matrisgolpesusuario2[1]);
    	if (spPunch[0] == 1) {
    		player2.leftPunch();
    	} else if(spPunch[1] == 1) {
    		player2.rightPunch();
    	}  else if (player2 != null) {    		
    		player2.reset();
    	}
    	
    	if (player2 != null & player1 != null) {
	    	player1.setPosition(fpPosition[0], fpPosition[1]);
	    	player2.setPosition(spPosition[0], spPosition[1]);  
    	}
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

	public static String readData() {
		String result = "";
		try {
			InputStream is = receiverSocket.getInputStream();
			int length = is.available();
			if (length > 0) { 
				byte[] bytes = new byte[length];					            
				is.read(bytes, 0, length);
				result = new String(bytes);
			}					            
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void sendData(String s) {
		print("Sending: " + s + "\r\n");
    	if (s == null) return;
    	try {
			if (BoxingClient.senderSocket != null) {
				BoxingClient.senderSocket.getOutputStream().write(s.getBytes());
			} else {
				print("senderSocket is null");
			}    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    public static void print(String msg) {
        System.out.println("BOXING CLIENT >> " + msg);
    }
}
