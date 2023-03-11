package com.example.aeye.ui;

import androidx.fragment.app.FragmentActivity;

import androidx.viewpager2.widget.ViewPager2;
import me.relex.circleindicator.CircleIndicator3;
import android.animation.ArgbEvaluator;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.example.aeye.ui.fragment.FragmentAdapter;
import com.example.aeye.Mode;
import com.example.aeye.R;
import com.example.aeye.ui.listener.TextToSpeechManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {
    //Initialize Variables
    LinearLayout linearLayout;

    ViewPager2 viewPager;
    FragmentAdapter fragmentAdapter;
    CircleIndicator3 circleIndicator;

    TextToSpeechManager textToSpeech;
    CharSequence infoToSpeechOut = null;

    List<Mode> modes;
    Integer[] colors = null;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayout = findViewById(R.id.linear_layout);

        /* Initialize TTS */
        textToSpeech = new TextToSpeechManager();
        textToSpeech.init(this);

        modes = new ArrayList<>();
        modes.add(new Mode(R.drawable.medicine_background,
                R.drawable.drink_medicine,
                "의약품 탐지",
                getResources().getColor(R.color.title_color2, null))
        );
        modes.add(new Mode(R.drawable.drink_background2,
                R.drawable.drink_icon,
                "음료 탐지",
                getResources().getColor(R.color.title_color1, null))
        );

        fragmentAdapter = new FragmentAdapter(this, modes, modes.size());

        //ViewPager Setting
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(fragmentAdapter);
        viewPager.setPadding(30, 0, 30, 0);
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setCurrentItem(0);
        viewPager.setOffscreenPageLimit(3);

        //CircleIndicator Setting
        circleIndicator = findViewById(R.id.indicator);
        circleIndicator.setViewPager(viewPager);
        circleIndicator.createIndicators(modes.size(), 0);

        Integer[] colors_temp = {
                getResources().getColor(R.color.background_color2, null),
                getResources().getColor(R.color.background_color1, null)
        };
        colors = colors_temp;

    }

    @Override
    protected void onStart() {
        super.onStart();
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(position < (fragmentAdapter.getItemCount() - 1) && position < (colors.length - 1)){
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
            public void onPageSelected(int position) {
                CharSequence phrase = " 모드 입니다.";
                textToSpeech.initQueue(modes.get(position).getTitle() + phrase);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }
}

