package com.example.aeye;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    //Initialize Variables
    RelativeLayout relativeLayout;
    TextView textView;
    SwipeListener swipeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Assign Variables
        relativeLayout = findViewById(R.id.relative_layout);
        textView = findViewById(R.id.text_view);
    }
    private class SwipeListener implements View.OnTouchListener{
        //Initialize Variables
        GestureDetector gestureDetector;

        //Create Constructor
        SwipeListener(View view){

        }
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return false;
        }
    }
}

