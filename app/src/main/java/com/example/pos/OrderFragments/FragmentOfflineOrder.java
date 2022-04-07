package com.example.pos.OrderFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.pos.R;
import com.example.pos.databinding.FragmentOfflineOrderBinding;

public class FragmentOfflineOrder extends Fragment {

    FragmentOfflineOrderBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_offline_order, container, false);
        View view = binding.getRoot();

        return view;
    }
}
