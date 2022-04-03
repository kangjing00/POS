package com.example.pos.PaymentTab;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.pos.R;
import com.example.pos.databinding.FragmentCashBinding;

public class FragmentCash extends Fragment {

    FragmentCashBinding binding;
    double cashAmountInET;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_cash, container, false);
        View view = binding.getRoot();
        cashAmountInET = 0;


        binding.cashKeypad1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cashAmountInET = Double.parseDouble(binding.cashAmountEt.getText().toString().substring(3, binding.cashAmountEt.getText().length()));
                cashAmountInET += 1;
                System.out.println(cashAmountInET);
                binding.cashAmountEt.setText("RM " + String.format("%.2f", cashAmountInET));
            }
        });

        binding.cashKeypadCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAmountEtToZero();
            }
        });


        // Inflate the layout for this fragment
        return view; //inflater.inflate(R.layout.fragment_cash, container, false);
    }

    private void setAmountEtToZero(){
        binding.cashAmountEt.setText("RM 0.00");
    }
}