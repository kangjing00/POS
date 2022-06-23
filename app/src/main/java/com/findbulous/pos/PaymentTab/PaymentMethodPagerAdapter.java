package com.findbulous.pos.PaymentTab;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PaymentMethodPagerAdapter extends FragmentStateAdapter {

    private String[] titles = new String[]{"Cash", "Other Modes"};

    public PaymentMethodPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new FragmentCash();
            case 1:
                return new FragmentOtherModes();

        }
        return new FragmentCash();
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }
}
