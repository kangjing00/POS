package com.findbulous.pos.OrderFragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.findbulous.pos.Adapters.OrderOnHoldAdapter;
import com.findbulous.pos.Customer;
import com.findbulous.pos.HomePage;
import com.findbulous.pos.Order;
import com.findbulous.pos.Order_Line;
import com.findbulous.pos.R;
import com.findbulous.pos.Table;
import com.findbulous.pos.databinding.FragmentOrderOnHoldBinding;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class FragmentOrderOnHold extends Fragment implements OrderOnHoldAdapter.OnHoldRemoveResumeInterface {

    private FragmentOrderOnHoldBinding binding;
    private Realm realm;
    private ArrayList<Order> orders;
    private OrderOnHoldAdapter orderOnHoldAdapter;
    private SharedPreferences currentOrderSharePreference, currentCustomerSharePreference;
    private SharedPreferences.Editor currentOrderSharePreferenceEdit, currentCustomerSharePreferenceEdit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_order_on_hold, container, false);
        View view = binding.getRoot();
        realm = Realm.getDefaultInstance();

        binding.orderOnHoldRv.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
        //binding.orderOnHoldRv.setLayoutManager(new GridAutoFitLayoutManager(getContext(), 0));
        ViewTreeObserver viewTreeObserver = binding.orderOnHoldRv.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                calculateSize();
            }
        });
        binding.orderOnHoldRv.setHasFixedSize(true);
        orders = new ArrayList<>();
        orderOnHoldAdapter = new OrderOnHoldAdapter(orders, getContext(), this);
        getOnHoldOrderFromRealm();
        binding.orderOnHoldRv.setAdapter(orderOnHoldAdapter);

        currentOrderSharePreference = getActivity().getSharedPreferences("CurrentOrder", Context.MODE_MULTI_PROCESS);
        currentOrderSharePreferenceEdit = currentOrderSharePreference.edit();

        currentCustomerSharePreference = getActivity().getSharedPreferences("CurrentCustomer", Context.MODE_MULTI_PROCESS);
        currentCustomerSharePreferenceEdit = currentCustomerSharePreference.edit();

        return view;
    }

    private static final int columnWidth = 280;
    private void calculateSize() {
        int spanCount = (int) Math.floor(binding.orderOnHoldRv.getWidth() / convertDPToPixels(columnWidth));
        ((StaggeredGridLayoutManager) binding.orderOnHoldRv.getLayoutManager()).setSpanCount(spanCount);
    }
    private float convertDPToPixels(int dp) {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float logicalDensity = metrics.density;
        return dp * logicalDensity;
    }


    private void getOnHoldOrderFromRealm(){
        RealmResults<Order> results = realm.where(Order.class).equalTo("state", "onHold").findAll();
        orders.addAll(realm.copyFromRealm(results));
        orderOnHoldAdapter.notifyDataSetChanged();
    }

    @Override
    public void onOrderOnHoldRemove(int position) {
        int order_id = orders.get(position).getOrder_id();
        Order orderRemove = realm.where(Order.class).equalTo("order_id", order_id).findFirst();
        RealmResults<Order_Line> orderLineRemove = realm.where(Order_Line.class).equalTo("order.order_id", order_id).findAll();

        if(order_id == currentOrderSharePreference.getInt("orderId", -1)){
            currentOrderSharePreferenceEdit.putInt("orderingState", 0);
            currentOrderSharePreferenceEdit.putInt("orderId", -1);
            currentOrderSharePreferenceEdit.commit();
        }

        Table tableUpdate = null;
        if(orderRemove.getTable() != null){
            tableUpdate = realm.copyFromRealm(orderRemove.getTable());
            tableUpdate.setState("V");
        }

        Table finalTableUpdate = tableUpdate;
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if(finalTableUpdate != null){
                    realm.insertOrUpdate(finalTableUpdate);
                }
                orderLineRemove.deleteAllFromRealm();
                orderRemove.deleteFromRealm();
            }
        });

        orders.remove(position);
        orderOnHoldAdapter.notifyDataSetChanged();

        Toast.makeText(getContext(), "An order has been removed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOrderOnHoldResume(int position) {
        int order_id = orders.get(position).getOrder_id();
        Order order = realm.where(Order.class).equalTo("order_id", order_id).findFirst();
        Customer customer = realm.where(Customer.class).equalTo("customer_id", order.getCustomer().getCustomer_id()).findFirst();

        int current_order_id = currentOrderSharePreference.getInt("orderId", -1);
        if(current_order_id == -1) {
            currentCustomerSharePreferenceEdit.putInt("customerID", customer.getCustomer_id());
            currentCustomerSharePreferenceEdit.putString("customerName", customer.getCustomer_name());
            currentCustomerSharePreferenceEdit.putString("customerEmail", customer.getCustomer_email());
            currentCustomerSharePreferenceEdit.putString("customerPhoneNo", customer.getCustomer_phoneNo());
            currentCustomerSharePreferenceEdit.putString("customerIdentityNo", customer.getCustomer_identityNo());
            currentCustomerSharePreferenceEdit.putString("customerBirthdate", customer.getCustomer_birthdate());
            currentCustomerSharePreferenceEdit.commit();

            currentOrderSharePreferenceEdit.putInt("orderingState", 1);
            currentOrderSharePreferenceEdit.putInt("orderId", order_id);
            if(order.getTable() != null) {  //Dine-in
                currentOrderSharePreferenceEdit.putInt("orderTypePosition", 1);
            }else{  //Takeaway
                currentOrderSharePreferenceEdit.putInt("orderTypePosition", 0);
            }
            currentOrderSharePreferenceEdit.commit();

            Intent intent = new Intent(getContext(), HomePage.class);
            startActivity(intent);
            getActivity().finish();
        }else if(current_order_id == order.getOrder_id()){
            Toast.makeText(getContext(), "This order is already in process", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getContext(), "Can not resume, an order is in process", Toast.LENGTH_SHORT).show();
        }
    }
}
