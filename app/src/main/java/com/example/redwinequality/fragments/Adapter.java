package com.example.redwinequality.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class Adapter extends FragmentStateAdapter {
    public Adapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new CustomFragment();
            case 1:
                return new WineSelectFragment();
            default:
                return new CustomFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
