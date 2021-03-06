package com.findbulous.pos.CashierFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.findbulous.pos.R;
import com.findbulous.pos.databinding.FragmentSaleHistoryBinding;

public class FragmentSaleHistory extends Fragment {
    private FragmentSaleHistoryBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sale_history, container, false);
        View view = binding.getRoot();

        return view;
    }
}
