package com.example.aeye.listener;

import android.annotation.SuppressLint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class SwipeListener implements View.OnTouchListener{
    //Initialize Variables
    GestureDetector gestureDetector;

    //Create Constructor
    SwipeListener(View view, TextView textView){
        //Initialize Threshold Value
        int threshold = 100;
        int velocity_threshold = 100;

        //Initialize simple gesture listener
        GestureDetector.SimpleOnGestureListener listener =
                new GestureDetector.SimpleOnGestureListener(){
                    @Override
                    public boolean onDown(MotionEvent e) {
                        //Pass true value
                        return true;
                    }
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                        //Get x and y difference
                        float xDiff = e2.getX() - e1.getX();
                        float yDiff = e2.getY() - e1.getY();
                        try {
                            //Check condition
                            if(Math.abs(xDiff) > Math.abs(yDiff)){
                                //When x is greater than y
                                //Check condition
                                if(Math.abs(xDiff) > threshold
                                        && Math.abs(velocityX) > velocity_threshold){
                                    //When x difference is greater than threshold
                                    //When x velocity is greater than velocity threshold
                                    if(xDiff > 0){
                                        //When swiped right
                                        textView.setText("Swiped Right");
                                    }
                                    else{
                                        //When swiped right
                                        textView.setText("Swiped Left");
                                    }
                                    return true;
                                }
                            }
                            else{
                                //When y is greater than x
                                //Check condition
                                if(Math.abs(yDiff) > threshold
                                        && Math.abs(velocityY) > velocity_threshold){
                                    //When x difference is greater than threshold
                                    //When x velocity is greater than velocity threshold
                                    if(yDiff > 0){
                                        //When swiped down
                                        textView.setText("Swiped Down");
                                    }
                                    else{
                                        //When swiped Up
                                        textView.setText("Swiped Up");
                                    }
                                    return true;
                                }
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        return false;
                    }
                };
        //Initialize gesture Detector
        gestureDetector = new GestureDetector(listener);
        //Set listener on View
        view.setOnTouchListener(this);

    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent motionEvent) {
        //Return gesture event
        return gestureDetector.onTouchEvent(motionEvent);
    }
}
