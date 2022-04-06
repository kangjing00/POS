package com.example.pos.CashierFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import com.example.pos.R;
import com.example.pos.databinding.FragmentTodaySaleBinding;

public class FragmentTodaySale extends Fragment {

    private FragmentTodaySaleBinding binding;
    //viewmodel

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_today_sale, container, false);
        View view = binding.getRoot();

        return view;
    }
}
