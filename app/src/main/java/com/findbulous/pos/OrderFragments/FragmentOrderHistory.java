package com.findbulous.pos.OrderFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.findbulous.pos.Adapters.OrderHistoryAdapter;
import com.findbulous.pos.Order;
import com.findbulous.pos.OrderPage;
import com.findbulous.pos.R;
import com.findbulous.pos.databinding.FragmentOrderHistoryBinding;

import java.util.ArrayList;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

public class FragmentOrderHistory extends Fragment implements OrderHistoryAdapter.OnItemClickListener{

    private FragmentOrderHistoryBinding binding;
    private Realm realm;
    private ArrayList<Order> orders;
    private OrderHistoryAdapter orderHistoryAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_order_history, container, false);
        View view = binding.getRoot();

        realm = Realm.getDefaultInstance();

        binding.orderHistoryRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.orderHistoryRv.setHasFixedSize(true);
        orders = new ArrayList<>();
        orderHistoryAdapter = new OrderHistoryAdapter(orders, this);
        getOrderHistoryFromRealm();
        binding.orderHistoryRv.setAdapter(orderHistoryAdapter);

        binding.orderHistorySearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchValue = binding.orderHistoryEtSearch.getText().toString().trim();
                RealmResults<Order> results = realm.where(Order.class).contains("customer.customer_name", searchValue, Case.INSENSITIVE)
                        .findAll();
                RealmResults<Order> allResults = realm.where(Order.class).equalTo("state", "paid").findAll();
                try{
                    int orderId = Integer.valueOf(searchValue);
                    results = realm.where(Order.class).equalTo("customer.customer_name", searchValue)
                            .or().equalTo("order_id", orderId).findAll();
                }catch (Exception e){
                    System.out.println("Error Message: " + e);
                }

                orders.clear();
                if(searchValue.length() != 0) {
                    orders.addAll(results);
                }else{
                    orders.addAll(allResults);
                }
                orderHistoryAdapter.notifyDataSetChanged();
            }

        });

        return view;
    }

    private void getOrderHistoryFromRealm(){
        RealmResults<Order> results = realm.where(Order.class).equalTo("state", "paid").findAll();
        orders.addAll(realm.copyFromRealm(results));
        orderHistoryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onOrderHistoryOrderClick(int position) {
        ((OrderPage)getActivity()).setOrderSelected(orders.get(position));
    }
}
