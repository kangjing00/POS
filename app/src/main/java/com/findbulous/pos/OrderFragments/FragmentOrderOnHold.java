package com.findbulous.pos.OrderFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.findbulous.pos.R;
import com.findbulous.pos.databinding.FragmentOrderOnHoldBinding;

public class FragmentOrderOnHold extends Fragment {

    FragmentOrderOnHoldBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_order_on_hold, container, false);
        View view = binding.getRoot();

        return view;
    }
}
