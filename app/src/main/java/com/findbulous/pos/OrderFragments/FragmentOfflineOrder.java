package com.findbulous.pos.OrderFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.findbulous.pos.Order;
import com.findbulous.pos.R;
import com.findbulous.pos.databinding.FragmentOfflineOrderBinding;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class FragmentOfflineOrder extends Fragment {

    private FragmentOfflineOrderBinding binding;
    private Realm realm;
    private ArrayList<Order> orders;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_offline_order, container, false);
        View view = binding.getRoot();

        realm = Realm.getDefaultInstance();



        orders = new ArrayList<>();

        getOfflineOrderFromRealm();


        if(orders.size() <= 0){
            binding.emptyOrderImg.setVisibility(View.VISIBLE);
        }else{
            binding.emptyOrderImg.setVisibility(View.GONE);
        }

        return view;
    }

    private void getOfflineOrderFromRealm(){
//        RealmResults<Order> results = realm.where(Order.class).equalTo("state", "paid").findAll();
//        orders.addAll(realm.copyFromRealm(results));
//        orderHistoryAdapter.notifyDataSetChanged();
    }

}
