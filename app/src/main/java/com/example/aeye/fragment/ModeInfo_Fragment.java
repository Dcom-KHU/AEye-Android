package com.example.aeye.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.aeye.Mode;
import com.example.aeye.R;
import com.example.aeye.activity.ModeLiveAnalysisActivity;

public class ModeInfo_Fragment extends Fragment {

    private final Mode current_mode;
    private ImageButton imageButton;
    private ImageView icon;
    private TextView title;

    public ModeInfo_Fragment(Mode current_mode){
        this.current_mode = current_mode;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View selection_view = inflater.inflate(R.layout.mode_selection, container, false);
        View info_view = inflater.inflate(R.layout.mode_info, container, false);

        // Assign Variables for this fragment
        imageButton = info_view.findViewById(R.id.mode_image);
        icon = info_view.findViewById(R.id.mode_icon);
        title = info_view.findViewById(R.id.mode_title);

        // Set Resources
        imageButton.setImageResource(current_mode.getImage());
        imageButton.setClipToOutline(true);
        icon.setImageResource(current_mode.getIcon());
        title.setText(current_mode.getTitle());
        title.setTextColor(current_mode.getTitle_color());

        imageButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ModeLiveAnalysisActivity.class);
            intent.putExtra("modeIcon", current_mode.getIcon());
            intent.putExtra("modeColor", current_mode.getTitle_color());
            startActivity(intent);
        });

        //Add Resource View to CardView
        FrameLayout frameLayout = selection_view.findViewById(R.id.mode_frame);
        frameLayout.addView(info_view);

        return selection_view;
    }
}