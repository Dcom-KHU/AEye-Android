package com.example.aeye;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


public class ObjectInfo_Fragment extends Fragment {

    private static final String ARG_MODE = "modeIcon";
    private static final String ARG_TITLE = "title";

    private Integer mMode;
    private String mTitle;

    public ObjectInfo_Fragment() {
        // Required empty public constructor
    }

    public static ObjectInfo_Fragment newInstance(String title, Integer modeIcon) {
        ObjectInfo_Fragment fragment = new ObjectInfo_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_MODE, modeIcon);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(ARG_TITLE);
            mMode = getArguments().getInt(ARG_MODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View main_view = inflater.inflate(R.layout.object_info, container, false);

        ImageButton imageButton = main_view.findViewById(R.id.object_icon);
        TextView titleText = main_view.findViewById(R.id.object_title);
        TextView infoText = main_view.findViewById(R.id.object_description);

        imageButton.setImageResource(mMode);
        titleText.setText(mTitle);

        return main_view;
    }
}