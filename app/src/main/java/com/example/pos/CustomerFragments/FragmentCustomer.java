package com.example.pos.CustomerFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import com.example.pos.R;
import com.example.pos.databinding.FragmentCustomerBinding;

public class FragmentCustomer extends Fragment {

    FragmentCustomerBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_customer, container, false);
        View view = binding.getRoot();

        return view;
    }
}
