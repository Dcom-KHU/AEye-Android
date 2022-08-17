package com.example.aeye;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class FragmentAdapter extends FragmentStateAdapter {
    public int mCount;
    private final List<Mode> modes;

    public FragmentAdapter(@NonNull FragmentActivity fragmentActivity, List<Mode> modes, int count) {
        super(fragmentActivity);
        this.mCount = count;
        this.modes = modes;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        int index = getRealPosition(position);
        return new ModeInfo_Fragment(modes.get(index));
    }

    @Override
    public int getItemCount() {
        return modes.size();
    }

    public int getRealPosition(int position) {return position % mCount; }
}
