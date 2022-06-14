package com.example.pos.CustomerFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.pos.CustomerPage;
import com.example.pos.R;
import com.example.pos.databinding.FragmentCustomerBinding;

public class FragmentCustomer extends Fragment {

    FragmentCustomerBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_customer, container, false);
        View view = binding.getRoot();



        binding.customerCurrentRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //REMOVE CURRENT CUSTOMER /or/ CLEAR CURRENT CUSTOMER /or/ MAKE CUSTOMER TO GENERAL CUSTOMER

                binding.customerCurrentCustomerRl.setVisibility(View.GONE);
            }
        });

        binding.customerCurrentEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((CustomerPage)getActivity()).editCurrentCustomer();
            }
        });

        return view;
    }
}
