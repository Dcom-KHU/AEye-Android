package com.example.aeye;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class Adapter extends PagerAdapter {

    private final List<Mode> modes;
    private LayoutInflater layoutInflater;
    private final Context context;

    public Adapter(List<Mode> modes, Context context) {
        this.modes = modes;
        this.context = context;
    }

    @Override
    public int getCount(){
        return modes.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.mode_selection, container, false);

        ImageButton imageButton;
        ImageView icon;
        TextView title;

        imageButton = view.findViewById(R.id.mode_image);
        icon = view.findViewById(R.id.mode_icon);
        title = view.findViewById(R.id.mode_title);

        imageButton.setImageResource(modes.get(position).getImage());
        imageButton.setClipToOutline(true);
        icon.setImageResource(modes.get(position).getIcon());
        title.setText(modes.get(position).getTitle());
        title.setTextColor(modes.get(position).getTitle_color());

        container.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
