package com.findbulous.pos.OrderFragments;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.findbulous.pos.Adapters.OrderHistoryAdapter;
import com.findbulous.pos.Order;
import com.findbulous.pos.R;
import com.findbulous.pos.databinding.FragmentOrderHistoryBinding;
import com.findbulous.pos.databinding.FragmentOrderOnGoingBinding;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class FragmentOrderOnGoing extends Fragment {

    private FragmentOrderOnGoingBinding binding;
    private Realm realm;
    private ArrayList<Order> orders;
//    private OrderHistoryAdapter orderHistoryAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_order_on_going, container, false);
        View view = binding.getRoot();

        realm = Realm.getDefaultInstance();

        //Recycler View (show on going order)
        orders = new ArrayList<>();


        if(orders.size() <= 0){
            binding.emptyOrderImg.setVisibility(View.VISIBLE);
        }else{
            binding.emptyOrderImg.setVisibility(View.GONE);
        }
        //Search btn onclick
        binding.orderOnGoingSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        return view;
    }

    private void getOrderOnGoingFromRealm(){
        //search draft order according to the user session
        RealmResults<Order> results = realm.where(Order.class).equalTo("state", "draft").findAll();
        orders.addAll(realm.copyFromRealm(results));
//        orderHistoryAdapter.notifyDataSetChanged();
    }
}