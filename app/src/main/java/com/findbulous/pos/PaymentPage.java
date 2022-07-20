package com.findbulous.pos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.findbulous.pos.Adapters.PaymentOrderLineAdapter;
import com.findbulous.pos.Network.CheckConnection;
import com.findbulous.pos.PaymentTab.PaymentMethodPagerAdapter;
import com.findbulous.pos.databinding.CashInOutPopupBinding;
import com.findbulous.pos.databinding.PaymentAddTipPopupBinding;
import com.findbulous.pos.databinding.PaymentPageBinding;
import com.findbulous.pos.databinding.ToolbarSyncPopupBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

import io.realm.Realm;

public class PaymentPage extends CheckConnection {

    private PaymentPageBinding binding;
    private PaymentMethodPagerAdapter paymentMethodPagerAdapter;
    private String[] titles = new String[]{"Cash", "Other Modes"};
    private PaymentPageViewModel viewModel;
    //RecyclerView
    private ArrayList<Order_Line> order_lines;
    private PaymentOrderLineAdapter orderLineAdapter;
    //Current order
    private Order currentOrder;
    //Current customer
    private Customer currentCustomer;

    private Realm realm;
    private SharedPreferences currentOrderSharePreference, currentCustomerSharePreference;
    private SharedPreferences.Editor currentOrderSharePreferenceEdit, currentCustomerSharePreferenceEdit;

    private POS_Config pos_config;

    String statuslogin;
    Context contextpage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextpage = PaymentPage.this;
        binding = DataBindingUtil.setContentView(this, R.layout.payment_page);
        viewModel = new ViewModelProvider(this).get(PaymentPageViewModel.class);
        binding.setPaymentPageViewModel(viewModel);
        binding.setLifecycleOwner(this);

        realm = Realm.getDefaultInstance();

        currentCustomerSharePreference = getSharedPreferences("CurrentCustomer",MODE_MULTI_PROCESS);
        currentCustomerSharePreferenceEdit = currentCustomerSharePreference.edit();
        int current_customer_id = currentCustomerSharePreference.getInt("customerID", -1);
        String customer_name = currentCustomerSharePreference.getString("customerName", null);
        String customerPhoneNo = currentCustomerSharePreference.getString("customerPhoneNo", null);
        String customerEmail = currentCustomerSharePreference.getString("customerEmail", null);
        String customerIdentityNo = currentCustomerSharePreference.getString("customerIdentityNo", null);
        String customerBirthdate = currentCustomerSharePreference.getString("customerBirthdate", null);
        if(current_customer_id != -1){
            currentCustomer = new Customer(current_customer_id, customer_name, customerEmail, customerPhoneNo, customerIdentityNo, customerBirthdate);
        }else{
            currentCustomer = null;
        }


        currentOrderSharePreference = getSharedPreferences("CurrentOrder",MODE_MULTI_PROCESS);
        currentOrderSharePreferenceEdit = currentOrderSharePreference.edit();
        currentOrder = new Order();
        int currentOrderId = currentOrderSharePreference.getInt("orderId", -1);
        Order order = realm.where(Order.class).equalTo("order_id", currentOrderId).findFirst();
        if(order != null) {
            currentOrder = realm.copyFromRealm(order);
        }

        //Body Setting
        //Tip Setting
        POS_Config temp_pos_config = realm.where(POS_Config.class).findFirst();
        if(temp_pos_config != null){
            pos_config = realm.copyFromRealm(temp_pos_config);
        }
        if(pos_config.isIface_tipproduct()){
            binding.paymentBarAddTip.setVisibility(View.VISIBLE);
        }else{
            binding.paymentBarAddTip.setVisibility(View.GONE);
        }
        //Customer Setting
        if(current_customer_id != -1) {
            binding.paymentBarCustomerName.setText(customer_name);
            binding.paymentBarCustomerId.setText("#" + current_customer_id);
            binding.paymentBarCustomerRl.setVisibility(View.VISIBLE);
            binding.paymentOrderDetailCustomerName.setText(customer_name);
        }else{
            binding.paymentBarCustomerRl.setVisibility(View.GONE);
            binding.paymentOrderDetailCustomerName.setText("[Customer Name]");
        }
        //Recycler View
        binding.paymentOrderDetailProductRv.setLayoutManager(new LinearLayoutManager(contextpage, LinearLayoutManager.VERTICAL, false));
        binding.paymentOrderDetailProductRv.setHasFixedSize(true);
        order_lines = new ArrayList<>();
        orderLineAdapter = new PaymentOrderLineAdapter(order_lines);
        order_lines.addAll(order.getOrder_lines());
        binding.paymentOrderDetailProductRv.setAdapter(orderLineAdapter);
        paymentMethodPagerAdapter = new PaymentMethodPagerAdapter(this);

        double order_subtotal = 0.0;
        for(int i = 0; i < order_lines.size(); i++){
            order_subtotal += order_lines.get(i).getPrice_subtotal();
        }
        binding.paymentSubtotal.setText(String.format("%.2f", order_subtotal));
        binding.paymentTax.setText(String.format("%.2f", currentOrder.getAmount_tax()));
        binding.paymentDiscount.setText(String.format("- %.2f", currentOrder.getAmount_order_discount()));
        binding.paymentGrandTotal.setText(String.format("%.2f", currentOrder.getAmount_total()));
        binding.paymentBarPayableAmount.setText(String.format("RM %.2f", currentOrder.getAmount_total()));
        viewModel.setAmount_total(currentOrder.getAmount_total());

        binding.paymentOrderDetailOrderId.setText("#" + currentOrder.getOrder_id());
        if(currentOrder.getTable() == null) { //Takeaway
            binding.paymentOrderDetailType.setText("Takeaway");
        }else{ //Dine-in
            binding.paymentOrderDetailType.setText("Dine-in - " + currentOrder.getTable().getName());
        }
        //Tabs
        {binding.paymentMethodViewPager.setAdapter(paymentMethodPagerAdapter);
        new TabLayoutMediator(binding.paymentMethodTl, binding.paymentMethodViewPager,
                (
                        (
                                (tab, position) -> tab.setText(titles[position])
                        )
                )
        ).attach();}

        //OnClickListener
        //Body
        {
        binding.paymentBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contextpage, HomePage.class);
                startActivity(intent);
                finish();
                Toast.makeText(contextpage, "Back Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        binding.paymentBarAddTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTipPopup(view);
                Toast.makeText(contextpage, "Add Tip Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        binding.paymentOrderDetailViewAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrderProducts();

                Toast.makeText(contextpage, "View All Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        binding.paymentTipCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.textView9.setVisibility(View.GONE);
                binding.paymentTip.setVisibility(View.GONE);
                binding.getPaymentPageViewModel().setPayment_tip("0.00");
                double amount_total = binding.getPaymentPageViewModel().getAmount_total();
                binding.paymentGrandTotal.setText(String.format("%.2f", amount_total));
                binding.paymentBarPayableAmount.setText(String.format("RM %.2f", amount_total));
                currentOrder.setAmount_total(amount_total);
                binding.paymentTipCancelBtn.setVisibility(View.GONE);
            }
        });
        binding.paymentBarRemoveCustomerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentCustomerSharePreferenceEdit.putInt("customerID", -1);
                currentCustomerSharePreferenceEdit.putString("customerName", null);
                currentCustomerSharePreferenceEdit.putString("customerEmail", null);
                currentCustomerSharePreferenceEdit.putString("customerPhoneNo", null);
                currentCustomerSharePreferenceEdit.putString("customerIdentityNo", null);
                currentCustomerSharePreferenceEdit.putString("customerBirthdate", null);
                currentCustomerSharePreferenceEdit.commit();
                binding.paymentBarCustomerRl.setVisibility(View.GONE);
                currentCustomer = null;
                binding.paymentOrderDetailCustomerName.setText("[Customer Name]");
                Toast.makeText(contextpage, "Current Customer Removed", Toast.LENGTH_SHORT).show();
            }
        });
        binding.paymentOrderDetailConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(is_balanceZero()) {
                    int current_customer_id = currentCustomerSharePreference.getInt("customerID", -1);
                    currentCustomerSharePreferenceEdit.putInt("customerID", -1);
                    currentCustomerSharePreferenceEdit.putString("customerName", null);
                    currentCustomerSharePreferenceEdit.putString("customerEmail", null);
                    currentCustomerSharePreferenceEdit.putString("customerPhoneNo", null);
                    currentCustomerSharePreferenceEdit.putString("customerIdentityNo", null);
                    currentCustomerSharePreferenceEdit.putString("customerBirthdate", null);
                    currentCustomerSharePreferenceEdit.commit();
                    int current_order_id = currentOrderSharePreference.getInt("orderId", -1);
                    currentOrderSharePreferenceEdit.putInt("orderingState", 0);
                    currentOrderSharePreferenceEdit.putInt("orderId", -1);
                    currentOrderSharePreferenceEdit.commit();

                    Order current_order = realm.where(Order.class).equalTo("order_id", current_order_id).findFirst();
                    Order updated_current_order = realm.copyFromRealm(current_order);
                    double amount_total = binding.getPaymentPageViewModel().getAmount_total();
                    double tip_amount = Double.valueOf(binding.getPaymentPageViewModel().getPayment_tip().getValue());
                    if (tip_amount > 0) {
                        if (updated_current_order.isIs_tipped() == false) {
                            updated_current_order.setIs_tipped(true);
                        }
                        updated_current_order.setAmount_total(amount_total);
                        updated_current_order.setTip_amount(tip_amount);
                    }
                    updated_current_order.setState("paid");
                    updated_current_order.setAmount_paid(Double.valueOf(viewModel.getPayment_order_detail_credit().getValue()));
                    updated_current_order.setCustomer(currentCustomer);
                    if(current_order.getTable() != null){
                        tableOccupiedToVacant(updated_current_order.getTable());
                    }
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.insertOrUpdate(updated_current_order);
                        }
                    });

                    Toast.makeText(contextpage, "Payment Success", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(contextpage, HomePage.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(contextpage, "Transaction is not completed", Toast.LENGTH_SHORT).show();
                }
            }
        });
        }
        //Toolbar buttons
        {
        binding.toolbarLayoutIncl.toolbarSearchIcon.setOnClickListener(new View.OnClickListener(){
              @Override
              public void onClick(View view) {
                  Toast.makeText(contextpage, "Search Button Clicked", Toast.LENGTH_SHORT).show();
              }
          }
        );

        binding.toolbarLayoutIncl.toolbarRefresh.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view) {
                   showRefreshPopup(view);
               }
           }
        );

        binding.toolbarLayoutIncl.toolbarWifi.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Toast.makeText(contextpage, "Wifi Button Clicked", Toast.LENGTH_SHORT).show();
                }
            }
        );

        binding.toolbarLayoutIncl.cashInOutBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    showCashInOut();
                }
            }
        );
        }
    }

    private void showOrderProducts() {
        PopupWindow popup = new PopupWindow(contextpage);
        View layout = getLayoutInflater().inflate(R.layout.payment_order_line_popup, null);
        popup.setContentView(layout);
        // Set content width and height
        popup.setHeight(1350);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // Closes the popup window when touch outside of it - when looses focus
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        // Show anchored to button
        popup.setElevation(8);
        popup.setBackgroundDrawable(null);
        //RecyclerView
        RecyclerView products_rv = (RecyclerView)layout.findViewById(R.id.payment_order_products_rv);
        products_rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        products_rv.setHasFixedSize(true);
        products_rv.setAdapter(orderLineAdapter);


        popup.showAtLocation(binding.getRoot(), Gravity.CENTER, 0, 0);
        //blur background
        View container = (View) popup.getContentView().getParent();
        WindowManager wm = (WindowManager) PaymentPage.this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);
    }

    private void tableOccupiedToVacant(Table table){
        table.setState("V");
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(table);
            }
        });
    }

    private void showCashInOut() {
        PopupWindow popup = new PopupWindow(contextpage);
        CashInOutPopupBinding popupBinding = CashInOutPopupBinding.inflate(getLayoutInflater());
//        View layout = getLayoutInflater().inflate(R.layout.cash_in_out_popup, null);
        popup.setContentView(popupBinding.getRoot());
        // Set content width and height
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // Closes the popup window when touch outside of it - when looses focus
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        // Show anchored to button
        popup.setElevation(8);
        popup.setBackgroundDrawable(null);
        popup.showAtLocation(binding.getRoot(), Gravity.CENTER, 0, 0);
        //blur background
        View container = (View) popup.getContentView().getParent();
        WindowManager wm = (WindowManager) PaymentPage.this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);

        popupBinding.cashInOutCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.dismiss();
                Toast.makeText(contextpage, "Cancel", Toast.LENGTH_SHORT).show();
            }
        });
        popupBinding.cashInOutConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.dismiss();
                Toast.makeText(contextpage, "Confirm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddTipPopup(View view) {
        PopupWindow popup = new PopupWindow(contextpage);
        PaymentAddTipPopupBinding popupBinding = PaymentAddTipPopupBinding.inflate(getLayoutInflater());
//        View layout = getLayoutInflater().inflate(R.layout.payment_add_tip_popup, null);
        popup.setContentView(popupBinding.getRoot());
        // Set content width and height
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // Closes the popup window when touch outside of it - when looses focus
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        // Show anchored to button
        popup.setElevation(8);
        popup.setBackgroundDrawable(null);
        binding.textView9.setVisibility(View.VISIBLE);
        binding.paymentTip.setVisibility(View.VISIBLE);
        binding.paymentTipCancelBtn.setVisibility(View.VISIBLE);
        //binding.cartInclude.tvDiscount.setTextColor(contextpage.getResources().getColor(R.color.darkOrange));
        popup.showAsDropDown(binding.paymentBarAddTip, -520, -180);

        //Popup Buttons
        popupBinding.addTipPopupEt.setText(binding.getPaymentPageViewModel().getPayment_tip().getValue());

        popupBinding.addTipPopupNegativeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                popup.dismiss();
                Toast.makeText(contextpage, "Cancel", Toast.LENGTH_SHORT).show();
            }
        });

        popupBinding.addTipPopupPositiveBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String tip_amount = String.format("%.2f", Double.valueOf(popupBinding.addTipPopupEt.getText().toString()));
                binding.getPaymentPageViewModel().setPayment_tip(tip_amount);
                double amount_total = binding.getPaymentPageViewModel().getAmount_total();
                binding.paymentGrandTotal.setText(String.format("%.2f", amount_total));
                binding.paymentBarPayableAmount.setText(String.format("RM %.2f", amount_total));
                currentOrder.setAmount_total(amount_total);
                popup.dismiss();
                Toast.makeText(contextpage, "Tip Added", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRefreshPopup(View view) {
        PopupWindow popup = new PopupWindow(contextpage);
        ToolbarSyncPopupBinding popupBinding = ToolbarSyncPopupBinding.inflate(getLayoutInflater());
        //View layout = getLayoutInflater().inflate(R.layout.toolbar_sync_popup, null);
        popup.setContentView(popupBinding.getRoot());
        // Set content width and height
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // Closes the popup window when touch outside of it - when looses focus
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        // Show anchored to button
        popup.setElevation(8);
        popup.setBackgroundDrawable(null);
        popup.showAsDropDown(binding.toolbarLayoutIncl.toolbarRefresh, -120, 0);


        popupBinding.syncProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(contextpage, "refresh / sync products", Toast.LENGTH_SHORT).show();
                popup.dismiss();
            }
        });

        popupBinding.syncTransactionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(contextpage, "refresh / sync transactions", Toast.LENGTH_SHORT).show();
                popup.dismiss();
            }
        });
    }

    private boolean is_balanceZero(){
        double balance_amount = Double.valueOf(binding.getPaymentPageViewModel().getPayment_order_detail_balance().getValue());
        if(balance_amount == 0){
            return true;
        }
        return false;
    }
}