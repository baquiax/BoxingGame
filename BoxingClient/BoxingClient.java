import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;


public class BoxingClient extends Application {
	private static final int		KEYBOARD_MOVEMENT_DELTA = 5;
    private static final Duration	TRANSLATE_DURATION      = Duration.seconds(0.25);
	private static final String		SERVER_IP 				= "192.168.0.15";
    private static final int		SERVER_PORT 			= 3141;

	private static int 				counter = 90; //Descendent counter
	private static int[] 			fpPosition, fpPunch, spPosition, spPunch, score;    
    private static Socket 			receiverSocket, senderSocket;  
	private static Circle 			circle;

    public static void main(String[] args) {		
		BoxingClient.fpPosition = new int[2];
    	BoxingClient.fpPunch = new int[2];
        BoxingClient.spPosition = new int[2];    	    	
    	BoxingClient.spPunch = new int[2];
        BoxingClient.score =new int[2];
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

					            if (inServer.equals("OK")) {
					            	okReceived = true;
					            	BoxingClient.senderSocket = new Socket(SERVER_IP, SERVER_PORT);
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
		BoxingClient.circle = createCircle();
		final Group group = new Group(circle);
		final TranslateTransition transition = createTranslateTransition(circle);    
		final Scene scene = new Scene(group, 800, 600, Color.BLACK);
		moveCircleOnKeyPress(scene, circle);
		moveCircleOnMousePress(scene, circle, transition);    
		stage.setScene(scene);
		stage.show();
	}  

	private Circle createCircle() {
		final Circle circle = new Circle(400, 300, 40, Color.RED);
		circle.setOpacity(0.8);
		return circle;
	}

	private TranslateTransition createTranslateTransition(final Circle circle) {
		final TranslateTransition transition = new TranslateTransition(TRANSLATE_DURATION, circle);
		transition.setOnFinished(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent t) {				
				circle.setCenterX(circle.getTranslateX() + circle.getCenterX());
				circle.setCenterY(circle.getTranslateY() + circle.getCenterY());
				circle.setTranslateX(0);
				circle.setTranslateY(0);
			}
		});
    	return transition;
	}

	private void moveCircleOnKeyPress(Scene scene, final Circle circle) {
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override public void handle(KeyEvent event) {
				double x = circle.getCenterX();
				double y = circle.getCenterY();

				switch (event.getCode()) {
					case UP:    
						//circle.setCenterY(circle.getCenterY() - KEYBOARD_MOVEMENT_DELTA); break;
						y -= KEYBOARD_MOVEMENT_DELTA; break;
					case RIGHT: 
						//circle.setCenterX(circle.getCenterX() + KEYBOARD_MOVEMENT_DELTA); break;
						x += KEYBOARD_MOVEMENT_DELTA; break;
          			case DOWN:  
					  	//circle.setCenterY(circle.getCenterY() + KEYBOARD_MOVEMENT_DELTA); break;
						y += KEYBOARD_MOVEMENT_DELTA; break;
          			case LEFT:  
					  	//circle.setCenterX(circle.getCenterX() - KEYBOARD_MOVEMENT_DELTA); break;
						x -= KEYBOARD_MOVEMENT_DELTA; break;
        		}				
				BoxingClient.sendData(x + "/" + y + ";0/0");
			}
		});
	}

	private void moveCircleOnMousePress(Scene scene, final Circle circle, final TranslateTransition transition) {
		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent event) {
				if (!event.isControlDown()) {
					circle.setCenterX(event.getSceneX());
					circle.setCenterY(event.getSceneY());
				} else {
					transition.setToX(event.getSceneX() - circle.getCenterX());
					transition.setToY(event.getSceneY() - circle.getCenterY());
					transition.playFromStart();
				}  
			}
		});
	}

	//Gaming
	public static void parseBox (String sRead){
		print(sRead + "\r\n");
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


		//Temporally here
		circle.setCenterX(fpPosition[0]);
		circle.setCenterY(fpPosition[0]);    	    	
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