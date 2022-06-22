package com.example.pos.CustomerFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pos.Customer;
import com.example.pos.Adapters.CustomerAdapter;
import com.example.pos.R;
import com.example.pos.databinding.FragmentCustomerBinding;

import java.util.ArrayList;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

public class FragmentCustomer extends Fragment implements CustomerAdapter.OnItemClickListener{

    private FragmentCustomerBinding binding;
    private CustomerAdapter customerAdapter;
    private ArrayList<Customer> customers;
    private Realm realm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_customer, container, false);
        View view = binding.getRoot();

        realm = Realm.getDefaultInstance();

        //Customer Recyclerview
        binding.searchCustomerRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.searchCustomerRv.setHasFixedSize(true);
        customers = new ArrayList<>();
        customerAdapter = new CustomerAdapter(customers, this);
        binding.searchCustomerRv.setAdapter(customerAdapter);

        binding.customerCurrentRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //REMOVE CURRENT CUSTOMER /or/ CLEAR CURRENT CUSTOMER /or/ MAKE CURRENT CUSTOMER TO GENERAL CUSTOMER

                binding.emptyCustomerImg.setVisibility(View.VISIBLE);
                binding.customerCurrentCustomerRl.setVisibility(View.GONE);
            }
        });
        binding.customerCurrentEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                int id = 0;
//                ((CustomerPage)getActivity()).editCurrentCustomer(id);
            }
        });
        binding.customerSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchEt = binding.customerEtSearch.getText().toString();
                customers.clear();
                if(searchEt.isEmpty()){
                    Toast.makeText(getContext(), "Please provide some hints for me", Toast.LENGTH_SHORT).show();
                    binding.emptyCustomerImg.setVisibility(View.VISIBLE);
                }else{
                    RealmResults<Customer> results = realm.where(Customer.class).contains("customer_name", searchEt, Case.INSENSITIVE)
                            .or().contains("customer_email", searchEt, Case.INSENSITIVE).or().contains("customer_phoneNo", searchEt, Case.INSENSITIVE)
                            .findAll();
                    if(results.isEmpty()){
                        binding.emptyCustomerImg.setVisibility(View.VISIBLE);
                    }else {
                        binding.emptyCustomerImg.setVisibility(View.GONE);
                        customers.addAll(realm.copyFromRealm(results));
                    }
                }
                customerAdapter.notifyDataSetChanged();
                binding.customerEtSearch.onEditorAction(EditorInfo.IME_ACTION_DONE);
                binding.customerEtSearch.clearFocus();
            }
        });

        return view;
    }


    @Override
    public void onCustomerClick(int position) {

        binding.emptyCustomerImg.setVisibility(View.GONE);
    }
}
