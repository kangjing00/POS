package com.findbulous.pos.CashierFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import com.findbulous.pos.R;
import com.findbulous.pos.databinding.FragmentCashierDrawerBinding;

public class FragmentCashierDrawer extends Fragment {

    private FragmentCashierDrawerBinding binding;
    //viewmodel

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_cashier_drawer, container, false);
        View view = binding.getRoot();

        binding.closeDrawerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(requireContext(), "The remarks: " + binding.remarksEt.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
