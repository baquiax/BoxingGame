import javafx.animation.TranslateTransition;
import edu.galileo.boxing.Player;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayerBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javafx.animation.Animation;
import com.sun.deploy.uitoolkit.impl.fx.ui.FXConsole;
import com.sun.javafx.geom.Rectangle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;


public class BoxingClient extends Application {
	private static final int		KEYBOARD_MOVEMENT_DELTA = 20;
	private static final String		SERVER_IP 				= "localhost";
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
	private static TextField 		playerName;
	private static Label			playerNameLabel;
	private static ListView 		scoreBoard;
	private static ImageView 		fondo;
	private static AudioClip 		punchSound;
	private static MediaPlayer		songGame; 
	private static Thread			receiverThread;
	
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
		
		playerName = new TextField();
		playerName.setLayoutX(350);
		playerName.setLayoutY(500);
		playerName.setOnKeyPressed(
				new EventHandler<KeyEvent>()
				{	                	
					public void handle(KeyEvent event) {	                    	
						switch (event.getCode()) {
						case ENTER:
							startGame();
							break;
						}
					}
				});
		
		playerNameLabel = new Label("Ingrese su nombre: ");
		playerNameLabel.setLayoutX(200);
		playerNameLabel.setLayoutY(500);
		 
		scoreBoard = new ListView();
		scoreBoard.setVisible(false);
		
		launch(args);
	}
	
	public static void startGame() {
		try {
			BoxingClient.receiverSocket = new Socket(SERVER_IP, SERVER_PORT);
			print("Connection stablished to:" + SERVER_IP + ":" + SERVER_PORT);
			BoxingClient.receiverThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						boolean okReceived = false;
						boolean goReceived = false;
						
						while (!receiverSocket.isClosed()) {							
							InputStream is = receiverSocket.getInputStream();
							if (receiverSocket == null || (receiverSocket != null && receiverSocket.isClosed())) return;
							int length = is.available();
							if (length > 0) { 
								byte[] bytes = new byte[length];					            
								is.read(bytes, 0, length);
								String inServer = new String(bytes);

								if (inServer.startsWith("OK")) {
									okReceived = true;
									BoxingClient.senderSocket = new Socket(SERVER_IP, SERVER_PORT);					            	
									sendData(playerName.getText());
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
			showSecondScene();
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
	
	public static void showFirstScene() {
		playerName.setVisible(true);
		playerNameLabel.setVisible(true);
		timer.setVisible(false);
		player1Score.setVisible(false);
		player2Score.setVisible(false);
   	
	}
	
	public static void showSecondScene() {
		playerName.deselect();
		timer.setVisible(true);
		player1Score.setVisible(true);
		player2Score.setVisible(true);
		playerName.setVisible(false);
		playerNameLabel.setVisible(false);
		fondo.setImage(new Image("img/arena.png"));
	}
	
	@Override 
	public void start(Stage stage) throws Exception {    	
		punchSound = new AudioClip(getClass().getResource("sound/punch.mp3").toString());		
		punchSound.setCycleCount(1);
		Media sound = new Media(getClass().getResource("sound/game.mp3").toString());				
		songGame = MediaPlayerBuilder.create().media(sound).build();
		songGame.setOnReady(new Runnable() {
			@Override
			public void run() {
				songGame.play();
			}
		});		

		stage.setTitle("Boxing game!");
		Group root = new Group();
		Scene sceneRoot = new Scene(root);

		sceneRoot.setOnKeyPressed(
			new EventHandler<KeyEvent>() {
				public void handle(KeyEvent event) {
					Player currentPlayer = (isFirstPlayer) ? player1 : player2;
					double x = currentPlayer.getImageView().getX();
					double y = currentPlayer.getImageView().getY();

					switch (event.getCode()) {
						case UP:    							
							y -= KEYBOARD_MOVEMENT_DELTA; 
							BoxingClient.sendData((int)x + "/" + (int)y + ";0/0");
							break;
						case RIGHT: 							
							x += KEYBOARD_MOVEMENT_DELTA; 
							BoxingClient.sendData((int)x + "/" + (int)y + ";0/0");
							break;
						case DOWN:  							
							y += KEYBOARD_MOVEMENT_DELTA; 
							BoxingClient.sendData((int)x + "/" + (int)y + ";0/0");
							break;
						case LEFT:
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
			}
		);
			
		stage.setScene(sceneRoot);
		
		Canvas c = new Canvas(900,700);		
		root.getChildren().add(c);
						
		fondo = new ImageView(new Image("img/login.png"));
		fondo.setViewport(new Rectangle2D(0, 0, 900, 700));
		root.getChildren().add(fondo);
		GraphicsContext gc = c.getGraphicsContext2D();
				
		root.getChildren().add(player1.getImageView());		
		root.getChildren().add(player2.getImageView());
		root.getChildren().add(player1Score);
		root.getChildren().add(player2Score);
		root.getChildren().add(timer);
		root.getChildren().add(result);
		root.getChildren().add(playerName);
		root.getChildren().add(playerNameLabel);
		root.getChildren().add(scoreBoard);
		player1.reset();
		player2.reset();
		player1.setCycleCount(Animation.INDEFINITE);
		player1.play();    
		player2.setCycleCount(Animation.INDEFINITE);
		player2.play();
		player2.left = true;
		stage.show();				
		showFirstScene();
	}  

	//Gaming
	public static void parseBox (String sRead){		
		print("Parsebox: " + sRead + "\r\n");
		
		if (sRead.toUpperCase().startsWith("STOP")) {					
			Platform.runLater(new Runnable() {
  			  @Override public void run() {
  				  result.setText("WINNER: " + sRead.split(";")[1]);
  				  result.setVisible(true);
  			  }
			});
			return;
		}
		
		if (sRead.toUpperCase().startsWith("SCOREBOARD")) {			
			try {
				senderSocket.close();
				receiverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			
			String[] players = sRead.substring(10).split(";");
			ArrayList<String> data = new ArrayList<String>();
			for (int i = 0; i < players.length; i++) {
				String[] d = players[i].split("/");
				data.add(d[1] + " (" + d[0]+ ")");
			}
			
			Platform.runLater(new Runnable() {
  			  @Override public void run() {
  				//TableColumn tc = new TableColumn<>("Player") ;
  				//TableColumn tc2 = new TableColumn<>("Score") ;
  				
  				ObservableList<String> ol = FXCollections.observableArrayList(data);
  				//scoreBoard.getColumns().addAll(tc,tc2);
  				scoreBoard.setItems(ol);
  				scoreBoard.setVisible(true);
  			  }
			});	
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
			punchSound.play();
		} else if(fpPunch[1] == 1) {
			player1.rightPunch();
			punchSound.play();
		}
		
		String matrisgolpesusuario2[] = matrisgolpes[1].split("/");
		spPunch[0] = Integer.parseInt(matrisgolpesusuario2[0]);
		spPunch[1] = Integer.parseInt(matrisgolpesusuario2[1]);
		if (spPunch[0] == 1) {
			player2.leftPunch();
			punchSound.play();
		} else if(spPunch[1] == 1) {
			player2.rightPunch();
			punchSound.play();
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
		if (s == null && !senderSocket.isClosed()) return;
		print("Sending: " + s + "\r\n");		
		try {
			if (BoxingClient.senderSocket != null && !BoxingClient.senderSocket.isClosed()) {
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