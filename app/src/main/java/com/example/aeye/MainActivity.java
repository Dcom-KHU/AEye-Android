package com.example.aeye;

import androidx.appcompat.app.AppCompatActivity;

import androidx.viewpager.widget.ViewPager;
import android.animation.ArgbEvaluator;

import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //Initialize Variables
    RelativeLayout relativeLayout;
    TextView swipeCheckingText;

    ViewPager viewPager;
    Adapter adapter;
    List<Mode> modes;
    Integer[] colors = null;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    //Swiping Detector Class Instance
    //SwipeListener swipeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Assign Variables for SwipeListener
        relativeLayout = findViewById(R.id.relative_layout);
        swipeCheckingText = findViewById(R.id.swipe_checking_text);

        //Initialize swipe listener
        //swipeListener = new SwipeListener(relativeLayout, swipeCheckingText);

        modes = new ArrayList<>();
        modes.add(new Mode(R.drawable.drink_background2,
                "음료 구매하기",
                getResources().getColor(R.color.title_color1, null))
        );
        modes.add(new Mode(R.drawable.medicine_background,
                "의약품 구매하기",
                getResources().getColor(R.color.title_color2, null))
        );

        adapter = new Adapter(modes,this);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setPadding(100, 0, 100, 0);

        Integer[] colors_temp = {
                getResources().getColor(R.color.background_color1, null),
                getResources().getColor(R.color.background_color2, null)
        };

        colors = colors_temp;

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(position < (adapter.getCount() - 1) && position < (colors.length - 1)){
                    viewPager.setBackgroundColor(
                            (Integer) argbEvaluator.evaluate(
                                    positionOffset,
                                    colors[position],
                                    colors[position + 1]
                            )
                    );
                }
                else{
                    viewPager.setBackgroundColor(colors[colors.length - 1]);
                }
            }
            @Override
            public void onPageSelected(int position) {}

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

    }


}

