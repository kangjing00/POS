package com.example.pos.CustomerFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.pos.R;
import com.example.pos.databinding.FragmentAddCustomerBinding;

public class FragmentAddCustomer extends Fragment {

    FragmentAddCustomerBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_customer, container, false);
        View view = binding.getRoot();

        if(getArguments() != null){
            //case: Update Customer detail action
            binding.addOrUpdateCustomerBtn.setText("Update Customer");
            binding.addOrUpdateCustomerBtn.setIcon(getResources().getDrawable(R.drawable.ic_update_user));
        }
        
        binding.addOrUpdateCustomerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getArguments().isEmpty()){
                    //Add Customer

                }else{
                    //Update Customer detail

                }
            }
        });

        return view;
    }
}
