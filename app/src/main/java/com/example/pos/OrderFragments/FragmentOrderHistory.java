package com.example.pos.OrderFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pos.DataAdapters.OrderHistoryAdapter;
import com.example.pos.Order;
import com.example.pos.R;
import com.example.pos.databinding.FragmentOrderHistoryBinding;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class FragmentOrderHistory extends Fragment implements OrderHistoryAdapter.OnItemClickListener{

    private FragmentOrderHistoryBinding binding;
    private Realm realm;
    private ArrayList<Order> orders;
    private OrderHistoryAdapter orderHistoryAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_order_history, container, false);
        View view = binding.getRoot();

        realm = Realm.getDefaultInstance();
        binding.orderHistoryRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.orderHistoryRv.setHasFixedSize(true);
        orders = new ArrayList<>();
        orderHistoryAdapter = new OrderHistoryAdapter(orders, this);
        getOrderHistoryFromRealm();
        binding.orderHistoryRv.setAdapter(orderHistoryAdapter);

        return view;
    }

    private void getOrderHistoryFromRealm(){
        RealmResults<Order> results = realm.where(Order.class).equalTo("state", "paid").findAll();
        orders.addAll(realm.copyFromRealm(results));
        orderHistoryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onOrderHistoryOrderClick(int position) {

    }
}
