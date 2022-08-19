package com.findbulous.pos.OrderFragments;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import com.findbulous.pos.Adapters.OrderHistoryAdapter;
import com.findbulous.pos.Adapters.OrderOnGoingAdapter;
import com.findbulous.pos.Order;
import com.findbulous.pos.OrderPage;
import com.findbulous.pos.R;
import com.findbulous.pos.databinding.FragmentOrderHistoryBinding;
import com.findbulous.pos.databinding.FragmentOrderOnGoingBinding;

import java.util.ArrayList;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

public class FragmentOrderOnGoing extends Fragment implements OrderOnGoingAdapter.OrderOnGoingClickInterface {

    private FragmentOrderOnGoingBinding binding;
    private Realm realm;
    private ArrayList<Order> orders;
    private OrderOnGoingAdapter orderOnGoingAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_order_on_going, container, false);
        View view = binding.getRoot();

        realm = Realm.getDefaultInstance();

        //Recycler View (show on going order)
        binding.orderOnGoingRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.orderOnGoingRv.setHasFixedSize(true);
        orders = new ArrayList<>();
        orderOnGoingAdapter = new OrderOnGoingAdapter(orders, this);
        getOrderOnGoingFromRealm();
        binding.orderOnGoingRv.setAdapter(orderOnGoingAdapter);


        if(orders.size() <= 0){
            binding.emptyOrderImg.setVisibility(View.VISIBLE);
        }else{
            binding.emptyOrderImg.setVisibility(View.GONE);
        }

        //Search btn onclick    //Customer name or order id
        binding.orderOnGoingSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchValue = binding.orderOnGoingEtSearch.getText().toString().trim();
                RealmResults<Order> results = realm.where(Order.class).contains("customer.customer_name", searchValue, Case.INSENSITIVE)
                        .equalTo("state", "draft").findAll();
                RealmResults<Order> allResults = realm.where(Order.class).equalTo("state", "draft").findAll();
                try{
                    int orderId = Integer.valueOf(searchValue);
                    results = realm.where(Order.class).equalTo("customer.customer_name", searchValue)
                            .or().equalTo("order_id", orderId)
                            .and().equalTo("state", "draft").findAll();
                }catch (Exception e){
                    System.out.println("Error Message: " + e);
                }

                orders.clear();
                if(searchValue.length() != 0) {
                    orders.addAll(results);
                }else{
                    orders.addAll(allResults);
                }
                if(orders.size() <= 0){
                    binding.emptyOrderImg.setVisibility(View.VISIBLE);
                }else{
                    binding.emptyOrderImg.setVisibility(View.GONE);
                }

                binding.orderOnGoingEtSearch.onEditorAction(EditorInfo.IME_ACTION_DONE);
                binding.orderOnGoingEtSearch.clearFocus();

                orderOnGoingAdapter.notifyDataSetChanged();
            }
        });


        return view;
    }

    private void getOrderOnGoingFromRealm(){
        //search draft order according to the user session
        RealmResults<Order> results = realm.where(Order.class).equalTo("state", "draft").findAll();
        orders.addAll(realm.copyFromRealm(results));
        orderOnGoingAdapter.notifyDataSetChanged();
    }

    @Override
    public void onOrderOnGoingSelect(int position) {
        ((OrderPage)getActivity()).setOrderSelected(orders.get(position));
    }
}