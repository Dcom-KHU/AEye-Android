package com.example.aeye;

import androidx.appcompat.app.AppCompatActivity;

import androidx.viewpager.widget.ViewPager;
import me.relex.circleindicator.CircleIndicator;
import android.animation.ArgbEvaluator;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //Initialize Variables
    LinearLayout linearLayout;
    //TextView swipeCheckingText;

    ViewPager viewPager;
    Adapter adapter;
    CircleIndicator circleIndicator;
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
        linearLayout = findViewById(R.id.linear_layout);
        //swipeCheckingText = findViewById(R.id.swipe_checking_text);

        circleIndicator = findViewById(R.id.indicator);

        //Initialize swipe listener
        //swipeListener = new SwipeListener(relativeLayout, swipeCheckingText);

        modes = new ArrayList<>();
        modes.add(new Mode(R.drawable.drink_background2,
                R.drawable.drink_icon,
                "음료 구매",
                getResources().getColor(R.color.title_color1, null))
        );
        modes.add(new Mode(R.drawable.medicine_background,
                R.drawable.drink_medicine,
                "의약품 구매",
                getResources().getColor(R.color.title_color2, null))
        );

        adapter = new Adapter(modes,this);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setPadding(50, 0, 50, 0);

        circleIndicator.setViewPager(viewPager);

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
                    circleIndicator.setBackgroundColor(
                            (Integer) argbEvaluator.evaluate(
                                    positionOffset,
                                    colors[position],
                                    colors[position + 1]
                            )
                    );
                }
                else{
                    viewPager.setBackgroundColor(colors[colors.length - 1]);
                    circleIndicator.setBackgroundColor(colors[colors.length - 1]);
                }
            }
            @Override
            public void onPageSelected(int position) {}

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

    }


}

