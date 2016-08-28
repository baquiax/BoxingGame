package edu.galileo.boxing;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Rectangle2D;
import javafx.animation.Transition;
import javafx.animation.Interpolator;
import javafx.util.Duration;

public class Player extends Transition {
    private ImageView imageView;    
    public boolean left;

    public Player(String imageName) {        
        Image i = new Image(imageName);
        imageView = new ImageView(i);
        imageView.setViewport(new Rectangle2D(0, 0, 0, 0));
        imageView.setVisible(false);
        setCycleDuration(Duration.millis(500));
        setInterpolator(Interpolator.LINEAR);
    }
    
    protected void interpolate(double k) {    
        if (k < 1.0) return;
        if (left) {
            rightMove();
            left = false;
        } else {
            leftMove();
            left = true;
        }
    }
    
    public double[] getCenter() {
    	double centerX = this.imageView.getX();
    	double centerY = this.imageView.getY();
    	double[] result = {centerX, centerY};
    	return result;
    }

    public void rightMove() {
    	imageView.setViewport(new Rectangle2D(101, 0, 100, 113));
    }
    
    public void leftMove() {
    	imageView.setViewport(new Rectangle2D(0, 0, 100, 113));
    }

    public void rightPunch() {
    	imageView.setViewport(new Rectangle2D(201, 0, 160, 113));
    }
    
    public void leftPunch() {
    	imageView.setViewport(new Rectangle2D(361, 0, 160, 113));
    }
    
    public void reset() {
    	//imageView.setViewport(new Rectangle2D(0, 0, 100, 113));
        leftMove();
    }    
    
    public void setPosition(double x, double y) {
    	imageView.setVisible(true);
    	imageView.setX(x);  	
    	imageView.setY(y);
    }
    
    public ImageView getImageView() {
    	return this.imageView;
    }
    
}