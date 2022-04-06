package com.example.pos.PaymentTab;

import android.os.Bundle;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.pos.PaymentPageViewModel;
import com.example.pos.R;
import com.example.pos.databinding.FragmentCashBinding;

public class FragmentCash extends Fragment {

    private FragmentCashBinding binding;
    private PaymentPageViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_cash, container, false);
        View view = binding.getRoot();
        viewModel = new ViewModelProvider(requireActivity()).get(PaymentPageViewModel.class);
        binding.setPaymentPageViewModel(viewModel);
        binding.setLifecycleOwner(this);
        //Restrict the cash amount edittext to show keyboard
        binding.cashAmountEt.setShowSoftInputOnFocus(false);


        //OnClickListener
        {
        binding.cashKeypad1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.getPaymentPageViewModel().keypadAddNumber('1');
            }
        });
        binding.cashKeypad2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.getPaymentPageViewModel().keypadAddNumber('2');
            }
        });
        binding.cashKeypad3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.getPaymentPageViewModel().keypadAddNumber('3');
            }
        });
        binding.cashKeypad4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.getPaymentPageViewModel().keypadAddNumber('4');
            }
        });
        binding.cashKeypad5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.getPaymentPageViewModel().keypadAddNumber('5');
            }
        });
        binding.cashKeypad6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.getPaymentPageViewModel().keypadAddNumber('6');
            }
        });
        binding.cashKeypad7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.getPaymentPageViewModel().keypadAddNumber('7');
            }
        });
        binding.cashKeypad8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.getPaymentPageViewModel().keypadAddNumber('8');
            }
        });
        binding.cashKeypad9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.getPaymentPageViewModel().keypadAddNumber('9');
            }
        });
        binding.cashKeypad0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.getPaymentPageViewModel().keypadAddNumber('0');
            }
        });
        binding.cashKeypad00.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.getPaymentPageViewModel().keypadAddNumber('0');
                binding.getPaymentPageViewModel().keypadAddNumber('0');
            }
        });
        }
        {
        binding.cashKeypadEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.getPaymentPageViewModel().keypadEnter();
            }
        });
        binding.cashKeypadCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.getPaymentPageViewModel().setAmountEtToZero();
            }
        });
        binding.cashKeypadBackspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.getPaymentPageViewModel().keypadBackSpace();
            }
        });
        }

        // Inflate the layout for this fragment
        return view; //inflater.inflate(R.layout.fragment_cash, container, false);
    }
}