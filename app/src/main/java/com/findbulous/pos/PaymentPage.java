package com.findbulous.pos;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;
import com.findbulous.pos.Adapters.PaymentOrderLineAdapter;
import com.findbulous.pos.Adapters.SplitBillOrderAdapter;
import com.findbulous.pos.Network.CheckConnection;
import com.findbulous.pos.Network.NetworkUtils;
import com.findbulous.pos.PaymentTab.PaymentMethodPagerAdapter;
import com.findbulous.pos.databinding.CashInOutPopupBinding;
import com.findbulous.pos.databinding.PaymentAddTipPopupBinding;
import com.findbulous.pos.databinding.PaymentOrderLinePopupBinding;
import com.findbulous.pos.databinding.PaymentPageBinding;
import com.findbulous.pos.databinding.SplitOrderPopupBinding;
import com.findbulous.pos.databinding.ToolbarSyncPopupBinding;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;

import io.realm.Realm;
import io.realm.RealmResults;

public class PaymentPage extends CheckConnection implements SplitBillOrderAdapter.SplitOrderLineInterface {

    private PaymentPageBinding binding;
    private PaymentMethodPagerAdapter paymentMethodPagerAdapter;
    private String[] titles = new String[]{"Cash", "Other Modes"};
    private PaymentPageViewModel viewModel;
    //RecyclerView
    private ArrayList<Order_Line> payment_order_lines;
    private PaymentOrderLineAdapter paymentOrderLineAdapter;
    private ArrayList<Order_Line> splitting_order_lines;
    private SplitBillOrderAdapter splittingOrderLineAdapter;
    //Current order
    private Order currentOrder;
    //Current customer
    private Customer currentCustomer;

    private Realm realm;
    private SharedPreferences currentOrderSharePreference, currentCustomerSharePreference;
    private SharedPreferences.Editor currentOrderSharePreferenceEdit, currentCustomerSharePreferenceEdit;

    private POS_Config pos_config;
    private Currency currency;
    private ArrayList<Payment_Method> payment_method;

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
        int currentLocalOrderId = currentOrderSharePreference.getInt("localOrderId", -1);
        Order order = realm.where(Order.class).equalTo("local_order_id", currentLocalOrderId).findFirst();
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

//        // Below line for testing purpose
//        binding.paymentBarAddTip.setVisibility(View.VISIBLE);
        //Currency setting
        currency = realm.copyFromRealm(realm.where(Currency.class).findFirst());
        payment_method = new ArrayList<>();
        payment_method.addAll(realm.copyFromRealm(realm.where(Payment_Method.class).findAll()));

        //Customer Setting
        if(current_customer_id != -1) {
            binding.paymentBarCustomerName.setText(customer_name);
            binding.paymentBarCustomerId.setText("#" + current_customer_id);
            binding.paymentBarCustomerRl.setVisibility(View.VISIBLE);
            binding.paymentOrderDetailCustomerName.setText(customer_name);
            binding.paymentOrderDetailCustomerName.setVisibility(View.VISIBLE);
            binding.paymentBarApplyCustomerBtn.setVisibility(View.GONE);
        }else{
            binding.paymentBarCustomerRl.setVisibility(View.GONE);
            binding.paymentOrderDetailCustomerName.setVisibility(View.GONE);
            binding.paymentBarApplyCustomerBtn.setVisibility(View.VISIBLE);
        }
        //Recycler View
        binding.paymentOrderDetailProductRv.setLayoutManager(new LinearLayoutManager(contextpage, LinearLayoutManager.VERTICAL, false));
        binding.paymentOrderDetailProductRv.setHasFixedSize(true);
        payment_order_lines = new ArrayList<>();
        paymentOrderLineAdapter = new PaymentOrderLineAdapter(payment_order_lines);
        payment_order_lines.addAll(order.getOrder_lines());
        binding.paymentOrderDetailProductRv.setAdapter(paymentOrderLineAdapter);
        paymentMethodPagerAdapter = new PaymentMethodPagerAdapter(this);

        double order_subtotal = 0.0, total_price_subtotal_incl = 0.0, amount_order_discount = 0.0;
        for(int i = 0; i < payment_order_lines.size(); i++){
            order_subtotal += payment_order_lines.get(i).getPrice_subtotal();
            total_price_subtotal_incl += payment_order_lines.get(i).getPrice_subtotal_incl();
        }
        binding.paymentSubtotal.setText(String.format("%.2f", order_subtotal));
        binding.paymentTax.setText(String.format("%.2f", currentOrder.getAmount_tax()));
        if(currentOrder.getDiscount_type() != null) {
            if (currentOrder.getDiscount_type().equalsIgnoreCase("percentage")) {
                amount_order_discount = (total_price_subtotal_incl * currentOrder.getDiscount()) / 100;
            } else if (currentOrder.getDiscount_type().equalsIgnoreCase("fixed_amount")) {
                amount_order_discount = currentOrder.getDiscount();
            }
        }
        binding.paymentDiscount.setText(String.format("- %.2f", amount_order_discount));

        binding.paymentGrandTotal.setText(String.format("%.2f", currentOrder.getAmount_total()));
        binding.paymentBarPayableAmount.setText(currencyDisplayFormat(currentOrder.getAmount_total()));
        viewModel.setAmount_total(currentOrder.getAmount_total());

        binding.paymentOrderDetailOrderId.setText("#" + currentOrder.getOrder_id());
        if(currentOrder.getTable() == null) { //Takeaway
            binding.paymentOrderDetailType.setText("Takeaway");
        }else{ //Dine-in
            binding.paymentOrderDetailType.setText("Dine-in - " + currentOrder.getTable().getFloor().getName() + " / " + currentOrder.getTable().getName());
        }

        binding.customerCount.setText("" + currentOrder.getCustomer_count());
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
        binding.paymentSplitBillBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSplitBill();
            }
        });
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
                binding.paymentBarPayableAmount.setText(currencyDisplayFormat(amount_total));
                currentOrder.setAmount_total(amount_total);
                currentOrder.setDisplay_amount_total(currencyDisplayFormat(amount_total));
                binding.paymentTipCancelBtn.setVisibility(View.GONE);
            }
        });
        binding.paymentBarApplyCustomerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contextpage, CustomerPage.class);
                startActivity(intent);
                finish();
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
                binding.paymentBarApplyCustomerBtn.setVisibility(View.VISIBLE);

                currentCustomer = null;
                binding.paymentOrderDetailCustomerName.setText("[Customer Name]");
                Toast.makeText(contextpage, "Current Customer Removed", Toast.LENGTH_SHORT).show();
                binding.paymentOrderDetailCustomerName.setVisibility(View.GONE);
            }
        });
        //wait for api to make / confirm payment
        binding.paymentOrderDetailConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(is_balanceLargerOrEqualToZero()) {
                    int current_customer_id = currentCustomerSharePreference.getInt("customerID", -1);
                    currentCustomerSharePreferenceEdit.putInt("customerID", -1);
                    currentCustomerSharePreferenceEdit.putString("customerName", null);
                    currentCustomerSharePreferenceEdit.putString("customerEmail", null);
                    currentCustomerSharePreferenceEdit.putString("customerPhoneNo", null);
                    currentCustomerSharePreferenceEdit.putString("customerIdentityNo", null);
                    currentCustomerSharePreferenceEdit.putString("customerBirthdate", null);
                    currentCustomerSharePreferenceEdit.commit();
                    int current_local_order_id = currentOrderSharePreference.getInt("localOrderId", -1);
                    currentOrderSharePreferenceEdit.putInt("orderingState", 0);
                    currentOrderSharePreferenceEdit.putInt("localOrderId", -1);
                    currentOrderSharePreferenceEdit.commit();

                    Order current_order = realm.where(Order.class).equalTo("local_order_id", current_local_order_id).findFirst();
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
                    updated_current_order.setState_name("Paid");
                    updated_current_order.setAmount_paid(amount_total);
                    updated_current_order.setAmount_return(Double.valueOf(viewModel.getPayment_order_detail_balance().getValue()));
                    if(currentCustomer == null){
                        currentCustomer = realm.where(Customer.class).equalTo("customer_id", 0).findFirst();
                    }
                    updated_current_order.setCustomer(currentCustomer);
                    if(current_order.getTable() != null){
                        RealmResults<Order> results = realm.where(Order.class)
                                .equalTo("table.table_id", updated_current_order.getTable().getTable_id())
                                .and().notEqualTo("state", "paid").and()
                                .notEqualTo("local_order_id", updated_current_order.getLocal_order_id()).findAll();
                        if(results.size() == 0)
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

    private void showSplitBill(){
        PopupWindow popup = new PopupWindow(contextpage);
        SplitOrderPopupBinding popupBinding = SplitOrderPopupBinding.inflate(getLayoutInflater());
        popup.setContentView(popupBinding.getRoot());
        // Set content width and height
        popup.setHeight(1350);
        popup.setWidth(2000);
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        popup.setElevation(8);
        popup.setBackgroundDrawable(null);

        //RecyclerView
        int currentLocalOrderId = currentOrderSharePreference.getInt("localOrderId", -1);
        Order order = realm.where(Order.class).equalTo("local_order_id", currentLocalOrderId).findFirst();

        popupBinding.productsRv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        popupBinding.productsRv.setHasFixedSize(true);

        popupBinding.paymentGrandTotal.setText(currencyDisplayFormat(0.00));

        splitting_order_lines = new ArrayList<>();
        splittingOrderLineAdapter = new SplitBillOrderAdapter(splitting_order_lines, this);
        splitting_order_lines.addAll(order.getOrder_lines());
        popupBinding.productsRv.setAdapter(splittingOrderLineAdapter);





        popupBinding.proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        popupBinding.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.dismiss();
            }
        });

        popup.showAtLocation(binding.getRoot(), Gravity.CENTER, 0, 0);
        //blur background
        View container = (View) popup.getContentView().getParent();
        WindowManager wm = (WindowManager) PaymentPage.this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);
    }

    private void showOrderProducts() {
        PopupWindow popup = new PopupWindow(contextpage);
        PaymentOrderLinePopupBinding popupBinding = PaymentOrderLinePopupBinding.inflate(getLayoutInflater());
//        View layout = getLayoutInflater().inflate(R.layout.payment_order_line_popup, null);
        popup.setContentView(popupBinding.getRoot());
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
        popupBinding.paymentOrderProductsRv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        popupBinding.paymentOrderProductsRv.setHasFixedSize(true);
        popupBinding.paymentOrderProductsRv.setAdapter(paymentOrderLineAdapter);

        //other setting
        popupBinding.paymentOrderPopupOrderId.setText("#" + currentOrder.getOrder_id());
        if(currentCustomer != null) {
            popupBinding.paymentOrderPopupCustomerName.setText(currentCustomer.getCustomer_name());
            popupBinding.paymentOrderPopupCustomerName.setVisibility(View.VISIBLE);
        }else{
            popupBinding.paymentOrderPopupCustomerName.setVisibility(View.GONE);
        }
        if(currentOrder.getTable() != null){
            popupBinding.paymentOrderPopupType.setText("Dine-in - " + currentOrder.getTable().getFloor().getName() +
                                                    " / " + currentOrder.getTable().getName());
        }else{
            popupBinding.paymentOrderPopupType.setText("Takeaway");
        }
        popupBinding.customerCount.setText("" + currentOrder.getCustomer_count());

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
                binding.paymentBarPayableAmount.setText(currencyDisplayFormat(amount_total));
                currentOrder.setAmount_total(amount_total);
                currentOrder.setDisplay_amount_total(currencyDisplayFormat(amount_total));
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

    private boolean is_balanceLargerOrEqualToZero(){
        double balance_amount = Double.valueOf(binding.getPaymentPageViewModel().getPayment_order_detail_balance().getValue());
        if(balance_amount >= 0){
            return true;
        }
        return false;
    }

    @Override
    public void onSplitOrderLineClick(int position) {

    }

    private String currencyDisplayFormat(double value){
        String valueFormatted = null;
        int decimal_place = currency.getDecimal_places();
        String currencyPosition = currency.getPosition();
        String symbol = currency.getSymbol();

        if(currencyPosition.equalsIgnoreCase("after")){
            valueFormatted = String.format("%." + decimal_place + "f", value) + symbol;
        }else if(currencyPosition.equalsIgnoreCase("before")){
            valueFormatted = symbol + String.format("%." + decimal_place + "f", value);
        }

        return valueFormatted;
    }

    public static ProgressDialog createProgressDialog(Context mContext) {
        ProgressDialog dialog = new ProgressDialog(mContext);
        try {
            dialog.show();
        } catch (WindowManager.BadTokenException e) {

        }
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.progress_dialog);
        // dialog.setMessage(Message);
        return dialog;
    }
}