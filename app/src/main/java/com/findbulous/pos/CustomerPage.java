package com.findbulous.pos;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.findbulous.pos.API.DeleteOneDraftOrder;
import com.findbulous.pos.API.DeleteOneOrderLine;
import com.findbulous.pos.API.SetOrderCustomer;
import com.findbulous.pos.API.SetOrderDiscount;
import com.findbulous.pos.API.SetOrderNote;
import com.findbulous.pos.API.SetOrderTable;
import com.findbulous.pos.API.UpdateOneOrderLineConfig;
import com.findbulous.pos.API.UpdateOneOrderLineQtyDisc;
import com.findbulous.pos.API.UpdateOrderCustomerCount;
import com.findbulous.pos.Adapters.CartOrderLineAdapter;
import com.findbulous.pos.CustomerFragments.FragmentAddCustomer;
import com.findbulous.pos.CustomerFragments.FragmentCustomer;
import com.findbulous.pos.CustomerFragments.FragmentCustomerDetail;
import com.findbulous.pos.Network.CheckConnection;
import com.findbulous.pos.Network.NetworkUtils;
import com.findbulous.pos.databinding.CartOrderAddDiscountPopupBinding;
import com.findbulous.pos.databinding.CartOrderAddNotePopupBinding;
import com.findbulous.pos.databinding.CashInOutPopupBinding;
import com.findbulous.pos.databinding.CustomerPageBinding;
import com.findbulous.pos.databinding.ProductModifierPopupBinding;
import com.findbulous.pos.databinding.ToolbarSyncPopupBinding;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class CustomerPage extends CheckConnection implements CartOrderLineAdapter.OnItemClickListener{

    private CustomerPageBinding binding;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private boolean customerFragment;
    // Storing data into SharedPreferences
    private SharedPreferences cartSharedPreference, customerSharedPreference;
    // Creating an Editor object to edit(write to the file)
    private SharedPreferences.Editor cartSharedPreferenceEdit, customerSharedPreferenceEdit;
    //Realm
    private Realm realm;
    //Current order
    private Order currentOrder;
    //OnHold customer
    private Customer onHoldCustomer;
    //Update table while order onHold
    private Table updateTableOnHold;
    //Recyclerview
    private CartOrderLineAdapter orderLineAdapter;
    private ArrayList<Order_Line> order_lines;
    //Number of Customer Popup
    private EditText number_customer_et;
    //Product attribute
    private Attribute_Value[] allAttributes;
    //private String[] allAttributes_custom;
    private EditText[] allAttributes_custom;

    private POS_Config pos_config;
    private Currency currency;
    //Modifier popup color btn
    private ImageButton lastClickedColorBtn;

    //POS Type
    private ArrayAdapter<String> orderTypeAdapter;
    private List<String> posOrderType = new ArrayList<String>();
    private String takeaway_posType = "Takeaway", dine_in_posType = "Dine-in";

    private String statuslogin;
    private Context contextpage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextpage = CustomerPage.this;
        binding = DataBindingUtil.setContentView(this, R.layout.customer_page);
        realm = Realm.getDefaultInstance();

        //Toolbar Settings
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //Nav Settings
        binding.navbarLayoutInclude.navBarCustomers.setChecked(true);

        //Cart Settings
        cartSharedPreference = getSharedPreferences("CurrentOrder",MODE_MULTI_PROCESS);
        cartSharedPreferenceEdit = cartSharedPreference.edit();

        customerSharedPreference = getSharedPreferences("CurrentCustomer",MODE_MULTI_PROCESS);
        customerSharedPreferenceEdit = customerSharedPreference.edit();
        //Cart Current Customer
        refreshCartCurrentCustomer();

        //Body Settings
        allAttributes_custom = null;
        allAttributes = null;


        lastClickedColorBtn = null;
        pos_config = realm.where(POS_Config.class).findFirst();
        currency = realm.copyFromRealm(realm.where(Currency.class).findFirst());
        currentOrder = new Order();
        updateTableOnHold = new Table();
        onHoldCustomer = null;
        //Cart / Order Line Recycler view
        binding.cartInclude.cartOrdersRv.setLayoutManager(new LinearLayoutManager(contextpage, LinearLayoutManager.VERTICAL, false));
        binding.cartInclude.cartOrdersRv.setHasFixedSize(true);
        order_lines = new ArrayList<>();
        orderLineAdapter = new CartOrderLineAdapter(order_lines, this, contextpage);
        getOrderLineFromRealm();
        binding.cartInclude.cartOrdersRv.setAdapter(orderLineAdapter);

        int currentOrderId = cartSharedPreference.getInt("localOrderId", -1);
        Order order = realm.where(Order.class).equalTo("local_order_id", currentOrderId).findFirst();
        if(order != null){
            currentOrder = realm.copyFromRealm(order);
        }
        //Cart Discount
        if((currentOrder.getDiscount_type() != null) && (currentOrder.getDiscount_type().length() > 0)) {
            if (currentOrder.getDiscount_type().equalsIgnoreCase("percentage")
                    || currentOrder.getDiscount_type().equalsIgnoreCase("fixed_amount")) {

                binding.cartInclude.cartOrderSummaryDiscountRl.setVisibility(View.VISIBLE);

                if(currentOrder.getDiscount_type().equalsIgnoreCase("fixed_amount")) {
                    binding.cartInclude.cartOrderSummaryDiscount.setText(
                            "- " + currencyDisplayFormat(currentOrder.getDiscount()));
                }else if(currentOrder.getDiscount_type().equalsIgnoreCase("percentage")){
                    double total_price_subtotal_incl = 0.0;
                    double amount_discount = 0.0;
                    RealmResults<Order_Line> results = realm.where(Order_Line.class)
                            .equalTo("order.local_order_id", currentOrder.getLocal_order_id()).findAll();
                    ArrayList<Order_Line> order_lineForDiscount = new ArrayList<>();
                    order_lineForDiscount.addAll(results);
                    System.out.println("Current order, order_line size ===========" + order_lineForDiscount.size());
                    for(int i = 0; i < order_lineForDiscount.size(); i++){
                        Order_Line order_line = order_lineForDiscount.get(i);
                        total_price_subtotal_incl += order_line.getPrice_subtotal_incl();
                    }
                    amount_discount = (total_price_subtotal_incl * currentOrder.getDiscount()) / 100;
                    binding.cartInclude.cartOrderSummaryDiscount.setText(
                            "- " + currencyDisplayFormat(amount_discount));
                }


                binding.cartInclude.cartOrderDiscountBtn.setTextColor(contextpage.getResources().getColor(R.color.green));
            }
        }else{
            binding.cartInclude.cartOrderSummaryDiscountRl.setVisibility(View.GONE);
        }

        //Fragment Settings
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.disallowAddToBackStack();
        customerFragment = true;
        ft.replace(binding.customerPageFl.getId(), new FragmentCustomer(), "Customers").commit();

        //OnClickListener
        //Body
        {
        binding.customerPageActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ft = fm.beginTransaction();
                customerFragment = !customerFragment;
                if(!customerFragment) {
                    ft.replace(binding.customerPageFl.getId(), new FragmentAddCustomer(), "AddUpdate").commit();
                    binding.customerPageActionBtn.setText("Customer");
                    binding.customerPageActionBtn.setIcon(getDrawable(R.drawable.ic_customer));
                    binding.customerPageTitle.setText("Add New Customer");
                }else{
                    ft.replace(binding.customerPageFl.getId(), new FragmentCustomer(), "Customers").commit();
                    binding.customerPageActionBtn.setText("Add New Customer");
                    binding.customerPageActionBtn.setIcon(getDrawable(R.drawable.ic_add));
                    binding.customerPageTitle.setText("Customers");
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
                     if(NetworkUtils.isNetworkAvailable(contextpage)) {
                         binding.toolbarLayoutIncl.toolbarWifi.setImageResource(R.drawable.ic_wifi);
                         binding.toolbarLayoutIncl.toolbarWifi.setColorFilter(getResources().getColor(R.color.green));
                         Toast.makeText(contextpage, "Wifi Connected", Toast.LENGTH_SHORT).show();
                     }
                     else {
                         binding.toolbarLayoutIncl.toolbarWifi.setImageResource(R.drawable.ic_no_internet);
                         binding.toolbarLayoutIncl.toolbarWifi.setColorFilter(getResources().getColor(R.color.red));
                         Toast.makeText(contextpage, "Wifi Loss", Toast.LENGTH_SHORT).show();
                     }
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
        //Navigation bar buttons
        {
        binding.navbarLayoutInclude.navBarHome.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view) {
                   Intent intent = new Intent(contextpage, HomePage.class);
                   startActivity(intent);
                   finish();
//                   Toast.makeText(contextpage, "Home Button Clicked", Toast.LENGTH_SHORT).show();
               }
           }
        );
        binding.navbarLayoutInclude.navBarCustomers.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view) {
//                   Toast.makeText(contextpage, "Customers Button Clicked", Toast.LENGTH_SHORT).show();
               }
           }
        );
        binding.navbarLayoutInclude.navBarTables.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view) {
                   Intent intent = new Intent(contextpage, TablePage.class);
                   startActivity(intent);
                   finish();
//                   Toast.makeText(contextpage, "Tables Button Clicked", Toast.LENGTH_SHORT).show();
               }
           }
        );
        binding.navbarLayoutInclude.navBarCashier.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view) {
                   Intent intent = new Intent(contextpage, CashierPage.class);
                   startActivity(intent);
                   finish();
//                   Toast.makeText(contextpage, "Cashier Button Clicked", Toast.LENGTH_SHORT).show();
               }
           }
        );
        binding.navbarLayoutInclude.navBarOrders.setOnClickListener(new View.OnClickListener(){
              @Override
              public void onClick(View view) {
                  Intent intent = new Intent(contextpage, OrderPage.class);
                  startActivity(intent);
                  finish();
//                  Toast.makeText(contextpage, "Orders Button Clicked", Toast.LENGTH_SHORT).show();
              }
          }
        );
        binding.navbarLayoutInclude.navBarReports.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view) {
//                   Toast.makeText(contextpage, "Reports Button Clicked", Toast.LENGTH_SHORT).show();
               }
           }
        );
        binding.navbarLayoutInclude.navBarSettings.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view) {
                   Intent intent = new Intent(contextpage, SettingPage.class);
                   startActivity(intent);
                   finish();
//                   Toast.makeText(contextpage, "Settings Button Clicked", Toast.LENGTH_SHORT).show();
               }
           }
        );
        binding.navbarLayoutInclude.navBarProfile.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view) {
//                   Toast.makeText(contextpage, "Profile Button Clicked", Toast.LENGTH_SHORT).show();
               }
           }
        );
        binding.navbarLayoutInclude.navbarLogout.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view) {
                   Intent intent = new Intent(contextpage, LoginPage.class);
                   startActivity(intent);
                   finish();

                   Toast.makeText(contextpage, "Logged out", Toast.LENGTH_SHORT).show();
               }
           }
        );
        }
        //Cart buttons
        {
        binding.cartInclude.cartOrderDiscountBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if((currentOrder.getOrder_id() != -1) && (order_lines.size() != 0))
                    showCartOrderAddDiscountPopup(view);
                else
                    Toast.makeText(contextpage, "Please add discount with at least 1 product", Toast.LENGTH_SHORT).show();
            }
        });
        binding.cartInclude.cartOrderCouponCodeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(contextpage, "Coupon Code Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        binding.cartInclude.cartOrderNoteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(cartSharedPreference.getInt("localOrderId", -1) == -1){
                    Toast.makeText(contextpage, "Please create an order before adding note", Toast.LENGTH_SHORT).show();
                }else {
                    showCartOrderAddNotePopup(binding.cartInclude.cartOrderNoteBtn.getId());
                }
            }
        });
        binding.cartInclude.cartAddCustomer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contextpage, CustomerPage.class);
                startActivity(intent);
                finish();
            }
        });
//        binding.cartInclude.cartOrderSummaryHoldBtn.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                if(currentOrder.getOrder_id() == -1){
//                    Toast.makeText(contextpage, "No order to hold", Toast.LENGTH_SHORT).show();
//                }
////                else if(customerSharedPreference.getInt("customerID", -1) == -1){
////                    Toast.makeText(contextpage, "Please add a customer to this order before hold", Toast.LENGTH_SHORT).show();
////                }
//                else {
//                    showCartOrderAddNotePopup(binding.cartInclude.cartOrderSummaryHoldBtn.getId());
//                }
//            }
//        });
        binding.cartInclude.cartOrderSummaryProceedBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                int current_local_order_id = cartSharedPreference.getInt("localOrderId", -1);
                int orderTypePosition = cartSharedPreference.getInt("orderTypePosition", -1);
                if(current_local_order_id == -1){ //no order and no order_line(products)
                    Toast.makeText(contextpage, "Please proceed payment with at least 1 product", Toast.LENGTH_SHORT).show();
                }else {//got order
                    RealmResults<Order_Line> results = realm.where(Order_Line.class).equalTo("order.local_order_id", current_local_order_id)
                            .findAll();
                    if(results.isEmpty()){//no order_line(products)
                        Toast.makeText(contextpage, "Please proceed payment with at least 1 product", Toast.LENGTH_SHORT).show();
                    }else {//got order_line(products)
                        if(orderTypePosition == 1){ // dine-in
                            if(currentOrder.getTable() == null){
                                Intent intent = new Intent(contextpage, TablePage.class);
                                startActivity(intent);
                                finish();
                            }else{
                                Intent intent = new Intent(contextpage, PaymentPage.class);
                                startActivity(intent);
                                finish();
                                Toast.makeText(contextpage, "Proceed Button Clicked", Toast.LENGTH_SHORT).show();
                            }
                        }else if (orderTypePosition == 0) { //takeaway
                            Intent intent = new Intent(contextpage, PaymentPage.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(contextpage, "Proceed Button Clicked", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        binding.cartInclude.cartBtnNumberCustomer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                showNumberOfCustomer();
            }
        });
        binding.cartInclude.cartBtnScan.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(contextpage, "Scan Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        posOrderType.add(takeaway_posType);
        posOrderType.add(dine_in_posType);
        //getResources().getStringArray(R.array.order_types
        orderTypeAdapter = new ArrayAdapter<String>(contextpage, R.layout.textview_spinner_item, posOrderType){
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent){
                View v = null;
                v = super.getDropDownView(position, null, parent);

                // If this is the selected item position
                if (position == binding.cartInclude.cartBtnPosType.getSelectedItemPosition()) {
                    if(position == (binding.cartInclude.cartBtnPosType.getCount() - 1)){
                        v.setBackground(getResources().getDrawable(R.drawable.box_btm_corner_dark_orange));
                    }else { //not the last one
                        v.setBackgroundColor(getResources().getColor(R.color.darkOrange));
                    }
                }
                else {
                    // for other views
                    if(position != (binding.cartInclude.cartBtnPosType.getCount() - 1)) {
                        v.setBackgroundColor(getResources().getColor(R.color.lightGrey));
                    }else{ //last one
                        v.setBackground(getResources().getDrawable(R.drawable.box_btm_corner_light_grey));
                    }
                }
                return v;
            }
        };
        orderTypeAdapter.setDropDownViewResource(R.layout.textview_spinner_item);
        binding.cartInclude.cartBtnPosType.setAdapter(orderTypeAdapter);
//        binding.cartInclude.cartBtnPosType.getAdapter().notifyAll();
        binding.cartInclude.cartBtnPosType.setDropDownVerticalOffset(65);
        binding.cartInclude.cartBtnPosType.setSelection(cartSharedPreference.getInt("orderTypePosition", 1));

        if(currentOrder.getTable() != null){
            posOrderType.remove(dine_in_posType);
            dine_in_posType = currentOrder.getTable().getFloor().getName() + " / " + currentOrder.getTable().getName();
            posOrderType.add(dine_in_posType);
            orderTypeAdapter.notifyDataSetChanged();
        }

        binding.cartInclude.cartBtnPosType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int orderTypePosition = binding.cartInclude.cartBtnPosType.getSelectedItemPosition(); //0 = takeaway, 1 = dine-in
                //if change from dine-in to takeaway \/ takeaway to dine-in
                if((orderTypePosition == 0) && (currentOrder.getOrder_id() != -1)
                    && (currentOrder.getTable() != null)){//takeaway && order now && order has table
                    //update table
                    RealmResults<Order> results = realm.where(Order.class)
                            .equalTo("table.table_id", currentOrder.getTable().getTable_id())
                            .and().notEqualTo("state", "paid").and()
                            .notEqualTo("local_order_id", currentOrder.getLocal_order_id()).findAll();
                    if(results.size() == 0)
                        tableOccupiedToVacant(currentOrder.getTable());

                    RealmResults<Order_Line> order_lines = realm.where(Order_Line.class).equalTo("order.local_order_id", currentOrder.getLocal_order_id())
                            .findAll();
                    if(order_lines.size() < 1){// when order_lines empty, delete the order
                        //delete order
                        Order deleteOrder = realm.where(Order.class)
                                .equalTo("local_order_id", currentOrder.getLocal_order_id())
                                .findFirst();
                        cartSharedPreferenceEdit.putInt("orderingState", 0);
                        cartSharedPreferenceEdit.putInt("localOrderId", -1);
                        cartSharedPreferenceEdit.commit();
                        if(NetworkUtils.isNetworkAvailable(contextpage)) {
                            new DeleteOneDraftOrder(contextpage, currentOrder.getOrder_id()).execute();
                        }
                        currentOrder = new Order();
                        binding.cartInclude.cartBtnNumberCustomer.setText("Guest(s)");
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                deleteOrder.deleteFromRealm();
                            }
                        });
                    }else {     // when order_lines not empty, set order table to null
                        currentOrder.setTable(null);
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.insertOrUpdate(currentOrder);
                            }
                        });
                        if(!NetworkUtils.isNetworkAvailable(contextpage)){
                            Toast.makeText(contextpage, "No Internet Connection.", Toast.LENGTH_SHORT).show();
                        }else {
                            new SetOrderTable(contextpage, currentOrder.getOrder_id(), currentOrder.getLocal_order_id(),
                                    null, null, null).execute();
                        }
                    }
                    resetPosType();
                }

                cartSharedPreferenceEdit.putInt("orderTypePosition", binding.cartInclude.cartBtnPosType.getSelectedItemPosition());
                cartSharedPreferenceEdit.commit();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        binding.cartInclude.cartOrderSummaryDiscountCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentOrder.getDiscount_type() != null) {
                    currentOrder.setDiscount(0.0);
                    currentOrder.setDiscount_type(null);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.insertOrUpdate(currentOrder);
                        }
                    });
                    updateOrderTotalAmount();

                    new SetOrderDiscount(contextpage, currentOrder.getOrder_id(), currentOrder.getLocal_order_id(),
                            null, 0).execute();
                }
                binding.cartInclude.cartOrderSummaryDiscount.setText("- " + currencyDisplayFormat(0.00));
                binding.cartInclude.cartOrderSummaryDiscountRl.setVisibility(View.GONE);
                binding.cartInclude.cartOrderDiscountBtn.setTextColor(contextpage.getResources().getColor(R.color.darkOrange));
            }
        });
        }
    }

    private void getOrderLineFromRealm(){
        int localOrderId = cartSharedPreference.getInt("localOrderId", -1);
        if(localOrderId > -1) {
            RealmResults<Order_Line> results = realm.where(Order_Line.class)
                    .equalTo("order.local_order_id", localOrderId).findAll();
            order_lines.addAll(realm.copyFromRealm(results));
        }else{
            order_lines.clear();
        }
        orderLineAdapter.notifyDataSetChanged();
    }
    private Customer getCurrentCustomer(){
        Customer customer = null;
        int current_customer_id = customerSharedPreference.getInt("customerID", -1);
        String customer_name = customerSharedPreference.getString("customerName", null);
        String customerPhoneNo = customerSharedPreference.getString("customerPhoneNo", null);
        String customerEmail = customerSharedPreference.getString("customerEmail", null);
        String customerIdentityNo = customerSharedPreference.getString("customerIdentityNo", null);
        String customerBirthdate = customerSharedPreference.getString("customerBirthdate", null);
        if(current_customer_id != -1){
            customer = new Customer(current_customer_id, customer_name, customerEmail, customerPhoneNo, customerIdentityNo, customerBirthdate);
        }else{
            customer = realm.where(Customer.class).equalTo("customer_id", 0).findFirst();
        }

        return customer;
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
        WindowManager wm = (WindowManager) CustomerPage.this.getSystemService(Context.WINDOW_SERVICE);
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

    //Number of Customer Popup
    private void showNumberOfCustomer(){
        PopupWindow popup = new PopupWindow(contextpage);
        View layout = getLayoutInflater().inflate(R.layout.number_of_customer_popup, null);
        popup.setContentView(layout);
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
        WindowManager wm = (WindowManager) CustomerPage.this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);

        number_customer_et = layout.findViewById(R.id.number_customer_et);
        MaterialButton keypad_clear = layout.findViewById(R.id.keypad_clear);
        MaterialButton keypad_backspace = layout.findViewById(R.id.keypad_backspace);
        MaterialButton keypad_plus_1 = layout.findViewById(R.id.keypad_plus_1);
        MaterialButton keypad_plus_2 = layout.findViewById(R.id.keypad_plus_2);
        MaterialButton keypad_plus_3 = layout.findViewById(R.id.keypad_plus_3);
        MaterialButton keypad_plus_4 = layout.findViewById(R.id.keypad_plus_4);
        MaterialButton keypad_1 = layout.findViewById(R.id.keypad_1);
        MaterialButton keypad_2 = layout.findViewById(R.id.keypad_2);
        MaterialButton keypad_3 = layout.findViewById(R.id.keypad_3);
        MaterialButton keypad_4 = layout.findViewById(R.id.keypad_4);
        MaterialButton keypad_5 = layout.findViewById(R.id.keypad_5);
        MaterialButton keypad_6 = layout.findViewById(R.id.keypad_6);
        MaterialButton keypad_7 = layout.findViewById(R.id.keypad_7);
        MaterialButton keypad_8 = layout.findViewById(R.id.keypad_8);
        MaterialButton keypad_9 = layout.findViewById(R.id.keypad_9);
        MaterialButton keypad_0 = layout.findViewById(R.id.keypad_0);
        MaterialButton cancel_btn = layout.findViewById(R.id.cancel_btn);
        MaterialButton confirm_btn = layout.findViewById(R.id.confirm_btn);

        number_customer_et.setShowSoftInputOnFocus(false);
        if(currentOrder.getOrder_id() != -1){
            if(currentOrder.getCustomer_count() != 0) {
                number_customer_et.setText(String.valueOf(currentOrder.getCustomer_count()));
            }else{
                number_customer_et.setText("1");
            }
        }else{
            number_customer_et.setText("1");
        }


        {
        keypad_backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int etLength = number_customer_et.getText().toString().length();
                if (etLength > 1) {
                    number_customer_et.setText(number_customer_et.getText().toString().substring(0, etLength - 1));
                }else{
                    number_customer_et.setText("0");
                }
            }
        });
        keypad_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                number_customer_et.setText("0");
            }
        });
        keypad_plus_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keypadPlusNumber(1);
            }
        });
        keypad_plus_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keypadPlusNumber(2);
            }
        });
        keypad_plus_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keypadPlusNumber(3);
            }
        });
        keypad_plus_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keypadPlusNumber(4);
            }
        });
        keypad_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keypadAddNumber('1');
            }
        });
        keypad_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keypadAddNumber('2');
            }
        });
        keypad_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keypadAddNumber('3');
            }
        });
        keypad_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keypadAddNumber('4');
            }
        });
        keypad_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keypadAddNumber('5');
            }
        });
        keypad_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keypadAddNumber('6');
            }
        });
        keypad_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keypadAddNumber('7');
            }
        });
        keypad_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keypadAddNumber('8');
            }
        });
        keypad_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keypadAddNumber('9');
            }
        });
        keypad_0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keypadAddNumber('0');
            }
        });
        cancel_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popup.dismiss();
                }
            });
        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentOrder.getOrder_id() != -1) {
                    if (number_customer_et.getText().toString().equalsIgnoreCase("0")) {
                        binding.cartInclude.cartBtnNumberCustomer.setText("1 Guest(s)");
                        currentOrder.setCustomer_count(1);
                    } else {
                        binding.cartInclude.cartBtnNumberCustomer.setText(number_customer_et.getText().toString() + " Guest(s)");
                        currentOrder.setCustomer_count(Integer.valueOf(number_customer_et.getText().toString()));
                    }
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.insertOrUpdate(currentOrder);
                        }
                    });
                    refreshCustomerNumber();
                    if(!NetworkUtils.isNetworkAvailable(contextpage)){
                        Toast.makeText(contextpage, "No Internet Connection.", Toast.LENGTH_SHORT).show();
                    }else {
                        new UpdateOrderCustomerCount(contextpage, currentOrder.getOrder_id(),
                                currentOrder.getLocal_order_id(), currentOrder.getCustomer_count()).execute();
                    }
                }
                popup.dismiss();
            }
        });
        }
    }
    private void keypadAddNumber(char keypadNo){
        if(number_customer_et.getText().toString().equalsIgnoreCase("0")){
            number_customer_et.setText(String.valueOf(keypadNo));
        }else {
            String addedNumberCustomer = number_customer_et.getText().toString() + keypadNo;
            number_customer_et.setText(addedNumberCustomer);
        }
    }
    private void keypadPlusNumber(int value){
        int number = Integer.valueOf(number_customer_et.getText().toString());
        number += value;
        number_customer_et.setText(String.valueOf(number));
    }
    //Cart Note and Discount
    private void showCartOrderAddNotePopup(int btnID) {
        PopupWindow popup = new PopupWindow(contextpage);
        CartOrderAddNotePopupBinding popupBinding = CartOrderAddNotePopupBinding.inflate(getLayoutInflater());
//        View layout = getLayoutInflater().inflate(R.layout.cart_order_add_note_popup, null);
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
        WindowManager wm = (WindowManager) CustomerPage.this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);


        if(currentOrder.getOrder_id() != -1) {
            if(currentOrder.getNote() != null){
                if(!currentOrder.getNote().isEmpty()) {
                    popupBinding.addNotePopupEt.setText(currentOrder.getNote());
                }
            } else {
                popupBinding.addNotePopupEt.setText("");
            }
        }else{
            popupBinding.addNotePopupEt.setText("");
        }
        popupBinding.addNotePopupPositiveBtn.setText("Add & Update");
//        if(btnID == binding.cartInclude.cartOrderNoteBtn.getId()){
//            popupBinding.addNotePopupPositiveBtn.setText("Add & Update");
//        }else if(btnID == binding.cartInclude.cartOrderSummaryHoldBtn.getId()){
//            popupBinding.addNotePopupPositiveBtn.setText("Proceed");
//        }

        popupBinding.addNotePopupNegativeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                popup.dismiss();
                Toast.makeText(contextpage, "Cancel", Toast.LENGTH_SHORT).show();
            }
        });
        popupBinding.addNotePopupPositiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String note = popupBinding.addNotePopupEt.getText().toString();
                if(!note.isEmpty()){
//                    if(btnID != binding.cartInclude.cartOrderSummaryHoldBtn.getId()) {
                        binding.cartInclude.cartOrderNoteBtn.setTextColor(contextpage.getResources().getColor(R.color.green));
//                    }
                }else{
                    binding.cartInclude.cartOrderNoteBtn.setTextColor(contextpage.getResources().getColor(R.color.darkOrange));
                }

//                if(btnID == binding.cartInclude.cartOrderSummaryHoldBtn.getId()){//Proceed
//                    if((currentOrder.getTable() == null) && (cartSharedPreference.getInt("orderTypePosition", -1) == 1)) {
//                        // the order has no table + it is dine-in
//                        currentOrder.setNote(note);
//                        realm.executeTransaction(new Realm.Transaction() {
//                            @Override
//                            public void execute(Realm realm) {
//                                realm.insertOrUpdate(currentOrder);
//                            }
//                        });
//                        Intent intent = new Intent(contextpage, TablePage.class);
//                        if(!NetworkUtils.isNetworkAvailable(contextpage)){  //no internet
//                            startActivity(intent);
//                            finish();
//                            Toast.makeText(contextpage, "No Internet Connection, product added into order stored in local", Toast.LENGTH_SHORT).show();
//                        }else {
//                            new SetOrderNote(contextpage, currentOrder.getOrder_id(), currentOrder.getLocal_order_id(),
//                                    note, intent, CustomerPage.this).execute();
//                        }
//                        Toast.makeText(contextpage, "Choose a table for this order", Toast.LENGTH_SHORT).show();
//                    }else {
//                        onHoldCustomer = getCurrentCustomer();
//                        currentOrder.setCustomer(onHoldCustomer);
//                        currentOrder.setState("onHold");
//                        currentOrder.setState_name("Onhold");
//                        currentOrder.setNote(note);
//
//                        if (currentOrder.getTable() != null) {
//                            Table result = realm.where(Table.class).equalTo("table_id", currentOrder.getTable().getTable_id()).findFirst();
//                            updateTableOnHold = realm.copyFromRealm(result);
//                            updateTableOnHold.setState("O"); //before edit is H
//                        }
//                        //update
//                        realm.executeTransaction(new Realm.Transaction() {
//                            @Override
//                            public void execute(Realm realm) {
//                                realm.insertOrUpdate(currentOrder);
//                                realm.insertOrUpdate(onHoldCustomer);
//                                if (currentOrder.getTable() != null)
//                                    realm.insertOrUpdate(updateTableOnHold);
//                            }
//                        });
//                        customerSharedPreferenceEdit.putInt("customerID", -1);
//                        customerSharedPreferenceEdit.putString("customerName", null);
//                        customerSharedPreferenceEdit.putString("customerEmail", null);
//                        customerSharedPreferenceEdit.putString("customerPhoneNo", null);
//                        customerSharedPreferenceEdit.putString("customerIdentityNo", null);
//                        customerSharedPreferenceEdit.putString("customerBirthdate", null);
//                        customerSharedPreferenceEdit.commit();
//                        cartSharedPreferenceEdit.putInt("orderingState", 0);
//                        cartSharedPreferenceEdit.putInt("localOrderId", -1);
//                        cartSharedPreferenceEdit.commit();
//
//                        updateOrderTotalAmount();
//                        refreshCartCurrentCustomer();
//                        currentOrder = new Order();
//                        updateTableOnHold = new Table();
//                        order_lines.clear();
//                        orderLineAdapter.notifyDataSetChanged();
//                        refreshNote();
//                        refreshCustomerNumber();
//                        resetPosType();
//                        FragmentCustomer fragmentCustomer = (FragmentCustomer)getSupportFragmentManager().findFragmentByTag("Customers");
//                        if(fragmentCustomer != null){
//                            fragmentCustomer.updateCurrentCustomer();
//                        }else{
//                            System.out.println("currently is not FragmentCustomer");
//                        }
//                        Toast.makeText(contextpage, "Proceed", Toast.LENGTH_SHORT).show();
//                    }
//                }else{//Add Note or Update
                    currentOrder.setNote(note);
                    //update note
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.insertOrUpdate(currentOrder);
                        }
                    });
                    if(!NetworkUtils.isNetworkAvailable(contextpage)){  //no internet
                        Toast.makeText(contextpage, "No Internet Connection, product added into order stored in local", Toast.LENGTH_SHORT).show();
                    }else {
                        new SetOrderNote(contextpage, currentOrder.getOrder_id(), currentOrder.getLocal_order_id(),
                                note, null, null).execute();
                    }
                    Toast.makeText(contextpage, "Added & Updated", Toast.LENGTH_SHORT).show();
//                }
                popup.dismiss();
            }
        });
    }
    private void showCartOrderAddDiscountPopup(View view) {
        PopupWindow popup = new PopupWindow(contextpage);
        CartOrderAddDiscountPopupBinding popupBinding = CartOrderAddDiscountPopupBinding.inflate(getLayoutInflater());
//        View layout = getLayoutInflater().inflate(R.layout.cart_order_add_discount_popup, null);
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
        binding.cartInclude.cartOrderSummaryDiscountRl.setVisibility(View.VISIBLE);
        binding.cartInclude.tvDiscount.setTextColor(contextpage.getResources().getColor(R.color.darkOrange));
        binding.cartInclude.cartOrderDiscountBtn.setTextColor(contextpage.getResources().getColor(R.color.green));
        //popup.showAsDropDown(binding.cartInclude.tvDiscount, -620, -180);
        popup.showAtLocation(binding.getRoot(), Gravity.CENTER, 0, 0);
        //blur background
        View container = (View) popup.getContentView().getParent();
        WindowManager wm = (WindowManager) CustomerPage.this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);


        if((currentOrder.getOrder_id() != -1) && (currentOrder.getDiscount_type() != null)) {
            if(currentOrder.getDiscount_type().equalsIgnoreCase("percentage")){ //percentage
                popupBinding.addDiscountPopupEt.setText(String.valueOf(currentOrder.getDiscount()));
                popupBinding.addDiscountPopupRadioBtnPercentage.setChecked(true);
                popupBinding.addDiscountPopupRadioBtnAmount.setChecked(false);
                popupBinding.addDiscountPopupEt.setInputType(InputType.TYPE_CLASS_NUMBER);
            }else if(currentOrder.getDiscount_type().equalsIgnoreCase("fixed_amount")){  //amount
                popupBinding.addDiscountPopupEt.setText(String.valueOf(currentOrder.getDiscount()));
                popupBinding.addDiscountPopupRadioBtnPercentage.setChecked(false);
                popupBinding.addDiscountPopupRadioBtnAmount.setChecked(true);
                popupBinding.addDiscountPopupEt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }
        }else{
            popupBinding.addDiscountPopupRadioBtnPercentage.setChecked(true);
            popupBinding.addDiscountPopupRadioBtnAmount.setChecked(false);
            popupBinding.addDiscountPopupEt.setText("0");
        }

        popupBinding.addDiscountPopupNegativeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                popup.dismiss();
            }
        });

        popupBinding.addDiscountPopupPositiveBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String userInput = popupBinding.addDiscountPopupEt.getText().toString();
                if((userInput.charAt(0) == '.') && (userInput.length() == 1)){
                    userInput = "0.0";
                }
                double discountInDouble = (!userInput.isEmpty())? Double.valueOf(userInput): 0.0;
                if(discountInDouble > 0.0) {
                    String discount = String.format("%.2f", discountInDouble);
                    String discount_type = null;
                    double amount_order_discount = 0.0;
                    if (popupBinding.addDiscountPopupRadioBtnAmount.isChecked()) {  //fixed_amount
                        discount_type = "fixed_amount";
                        amount_order_discount = Double.parseDouble(discount);
                    } else {    //percentage
                        discount_type = "percentage";
                        int order_discount_percent = (int)(Double.parseDouble(discount));
                        double total_price_subtotal_incl = 0;
                        for(int i=0; i < order_lines.size(); i++){
                            total_price_subtotal_incl += order_lines.get(i).getPrice_subtotal_incl();
                        }
                        amount_order_discount = total_price_subtotal_incl * ((double)order_discount_percent / 100);
                    }

                    binding.cartInclude.cartOrderSummaryDiscount.setText("- " + currencyDisplayFormat(amount_order_discount));

                    currentOrder.setDiscount_type(discount_type);
                    currentOrder.setDiscount(Double.valueOf(discount));
                    updateOrderTotalAmount();

                    new SetOrderDiscount(contextpage, currentOrder.getOrder_id(), currentOrder.getLocal_order_id(),
                            discount_type, Double.valueOf(discount)).execute();
                }
                popup.dismiss();
            }
        });

        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                binding.cartInclude.tvDiscount.setTextColor(contextpage.getResources().getColor(R.color.black));
            }
        });

        popupBinding.addDiscountPopupRadioBtnAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupBinding.addDiscountPopupEt.setText(String.format("%.2f", Double.valueOf(popupBinding.addDiscountPopupEt.getText().toString())));
                popupBinding.addDiscountPopupEt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                popupBinding.addDiscountPopupRadioBtnAmount.setChecked(true);
                popupBinding.addDiscountPopupRadioBtnPercentage.setChecked(false);
            }
        });
        popupBinding.addDiscountPopupRadioBtnPercentage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupBinding.addDiscountPopupEt.setText(String.format("%d", Math.round(Double.valueOf(popupBinding.addDiscountPopupEt.getText().toString()))));
                popupBinding.addDiscountPopupEt.setInputType(InputType.TYPE_CLASS_NUMBER);
                popupBinding.addDiscountPopupRadioBtnPercentage.setChecked(true);
                popupBinding.addDiscountPopupRadioBtnAmount.setChecked(false);
            }
        });
    }

    public void editCustomer(int customer_id){
        Bundle bundle = new Bundle();
        bundle.putInt("customer_id", customer_id);
        FragmentAddCustomer fragmentAddCustomer = new FragmentAddCustomer();
        fragmentAddCustomer.setArguments(bundle);

        ft = fm.beginTransaction();
        ft.replace(binding.customerPageFl.getId(), fragmentAddCustomer, "AddUpdate").commit();
        binding.customerPageActionBtn.setText("Customer");
        binding.customerPageActionBtn.setIcon(getDrawable(R.drawable.ic_customer));
        binding.customerPageTitle.setText("Update Customer Detail");

        customerFragment = false;
    }

    private void showProductModifier(Order_Line order_line, Product product){
        PopupWindow popup = new PopupWindow(contextpage);
        ProductModifierPopupBinding popupBinding = ProductModifierPopupBinding.inflate(getLayoutInflater());
//        View layout = getLayoutInflater().inflate(R.layout.product_modifier_popup, null);
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
        if(pos_config.isProduct_configurator() || pos_config.isIface_orderline_customer_notes()) {
            popup.showAtLocation(binding.getRoot(), Gravity.CENTER, 0, 0);
            //blur background
            View container = (View) popup.getContentView().getParent();
            WindowManager wm = (WindowManager) CustomerPage.this.getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
            p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            p.dimAmount = 0.3f;
            wm.updateViewLayout(container, p);
        }

        popupBinding.productNameModifierPopup.setText(product.getName());
        // Add view for extra attributes
        if(pos_config.isProduct_configurator()){
            RealmResults attribute_results = realm.where(Attribute.class).equalTo("product_tmpl_id", product.getProduct_tmpl_id())
                    .findAll();
            ArrayList<Attribute> attributes = (ArrayList<Attribute>) realm.copyFromRealm(attribute_results);
            if(attributes.size() == 0){
                popupBinding.productModifierLl.setVisibility(View.GONE);
            }

            allAttributes_custom = new EditText[attributes.size()];
            allAttributes = new Attribute_Value[attributes.size()];

            for(int i = 0; i < attributes.size(); i++){
                Attribute attribute = attributes.get(i);
                //Attribute Name
                TextView attribute_name_tv = new TextView(contextpage);
                attribute_name_tv.setText(attribute.getName());
                attribute_name_tv.setTextSize(20);
                popupBinding.productModifierLl.addView(attribute_name_tv);
                Space blankSpace = new Space(contextpage);
                blankSpace.setMinimumHeight(10);
                popupBinding.productModifierLl.addView(blankSpace);
                //Attribute Type
                RealmResults attribute_values_results = realm.where(Attribute_Value.class).equalTo("attribute_id", attribute.getAttribute_id())
                        .and().equalTo("product_tmpl_id", product.getProduct_tmpl_id()).findAll();
                ArrayList<Attribute_Value> attribute_values = (ArrayList<Attribute_Value>) realm.copyFromRealm(attribute_values_results);
                //Radio Button
                if(attribute.getDisplay_type().equalsIgnoreCase("radio")){
                    RadioGroup rg = new RadioGroup(contextpage);
                    rg.setOrientation(LinearLayout.HORIZONTAL);
                    RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(15, 0, 15, 0);
                    for(int x = 0; x < attribute_values.size(); x++){
                        Attribute_Value attribute_value = attribute_values.get(x);
                        RadioButton rb = new RadioButton(contextpage);
                        rb.setId(attribute_value.getId());
                        rb.setLayoutParams(params);
                        rb.setButtonDrawable(getResources().getDrawable(R.drawable.custom_radio_btn));
                        if(attribute_value.getPrice_extra() > 0){
                            rb.setText(attribute_value.getName() + " (+" + attribute_value.getDisplay_price_extra() + ")");
                        }else{
                            rb.setText(attribute_value.getName());
                        }

                        //IS_CUSTOM
                        EditText custom_et = new EditText(contextpage);
                        int finalI = i;
                        rb.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                allAttributes[finalI] = attribute_value;

                                if(attribute_value.isIs_custom() && rb.isChecked()){
                                    allAttributes_custom[finalI] = custom_et;
                                    custom_et.setVisibility(View.VISIBLE);
                                }else{
                                    allAttributes_custom[finalI] = null;
                                    custom_et.setVisibility(View.GONE);
                                }
                            }
                        });

                        if(x == 0){
                            allAttributes[i] = attribute_value;
                            if(attribute_value.isIs_custom()){
                                allAttributes_custom[i] = custom_et;
                            }else {
                                allAttributes_custom[i] = null;
                            }

                            rb.setChecked(true);
                        }

                        // Add Radio Button into RadioGroup
                        rg.addView(rb);
                        if(attribute_value.isIs_custom()){
                            if(rb.isChecked()){
                                custom_et.setVisibility(View.VISIBLE);
                            }else {
                                custom_et.setVisibility(View.GONE);
                            }
                            custom_et.setMaxWidth(500);
                            custom_et.setMaxLines(3);
                            custom_et.setTextSize(15);
                            custom_et.setHint(attribute_value.getName());
                            custom_et.setPadding(10, 0, 10, 10);
                            rg.addView(custom_et);
                        }
                    }
                    popupBinding.productModifierLl.addView(rg);
                }
                // Drop Down List / Select
                else if(attribute.getDisplay_type().equalsIgnoreCase("select")){
                    EditText custom_et = new EditText(contextpage);

                    Spinner spinner = new Spinner(contextpage);
                    String[] items = new String[attribute_values.size()];
                    int[] item_ids = new int[attribute_values.size()];
                    for(int x = 0; x < attribute_values.size(); x++){
                        Attribute_Value attribute_value = attribute_values.get(x);
                        item_ids[x] = attribute_value.getId();
                        if(attribute_value.getPrice_extra() > 0){
                            items[x] = attribute_value.getName() + " (" + attribute_value.getDisplay_price_extra() + ")";
                        }else{
                            items[x] = attribute_value.getName();
                        }
                        ArrayAdapter<String> itemAdapter = new ArrayAdapter<String>(contextpage, R.layout.textview_spinner_item, items){
                            @Override
                            public View getDropDownView(int position, View convertView, ViewGroup parent){
                                View v = null;
                                v = super.getDropDownView(position, null, parent);

                                // If this is the selected item position
                                if (position == spinner.getSelectedItemPosition()) {
                                    v.setBackgroundColor(getResources().getColor(R.color.darkOrange));
                                }
                                else {
                                    // for other views
                                    v.setBackgroundColor(getResources().getColor(R.color.lightGrey));
                                }
                                return v;
                            }
                        };
                        itemAdapter.setDropDownViewResource(R.layout.textview_spinner_item);
                        spinner.setAdapter(itemAdapter);
                        spinner.setDropDownVerticalOffset(80);
                        spinner.setSelection(0);
                        if(x == 0){
                            allAttributes[i] = attribute_value;
                        }
                        spinner.setPopupBackgroundDrawable(getResources().getDrawable(R.drawable.box_btm_corner_light_grey));
                        int finalI = i;
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                                int id = item_ids[position];
                                //IS_CUSTOM
                                Attribute_Value av = realm.where(Attribute_Value.class).equalTo("id", id).findFirst();

                                allAttributes[finalI] = realm.copyFromRealm(av);

                                if(av.isIs_custom()){
                                    allAttributes_custom[finalI] = custom_et;
                                    custom_et.setVisibility(View.VISIBLE);
                                }else{
                                    allAttributes_custom[finalI] = null;
                                    custom_et.setVisibility(View.GONE);
                                }
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {
                            }
                        });

                        custom_et.setHint(attribute_value.getName());
                    }

                    popupBinding.productModifierLl.addView(spinner);
                    //IS_CUSTOM
                    //First Selection if it IS_CUSTOM
//                    Attribute_Value av = realm.where(Attribute_Value.class)
//                            .equalTo("id", attribute_values.get(0).getId())
//                            .findFirst();
                    if(attribute_values.get(0).isIs_custom()){
                        allAttributes_custom[i] = custom_et;
                        custom_et.setVisibility(View.VISIBLE);
                    }else{
                        allAttributes_custom[i] = null;
                        custom_et.setVisibility(View.GONE);
                    }
                    custom_et.setMaxWidth(500);
                    custom_et.setMaxLines(3);
                    custom_et.setTextSize(15);
                    custom_et.setPadding(10, 0, 10, 10);
                    popupBinding.productModifierLl.addView(custom_et);
                }
                // Color Selection
                else if(attribute.getDisplay_type().equalsIgnoreCase("color")){
                    LinearLayout ll = new LinearLayout(contextpage);
                    ll.setOrientation(LinearLayout.HORIZONTAL);
                    for(int x = 0; x < attribute_values.size(); x++){
                        Attribute_Value attribute_value = attribute_values.get(x);

                        ImageButton btn = new ImageButton(contextpage);
                        btn.setId(attribute_value.getId());
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        params.setMargins(5, 0, 15, 0);
                        btn.setLayoutParams(params);
                        btn.setBackground(unselect(btn.getId()));
                        btn.setClickable(true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            btn.setTooltipText(attribute_value.getName());
                        }
                        if(x == 0){
                            allAttributes[i] = attribute_value;

                            btn.setBackground(select(btn.getId()));
                            lastClickedColorBtn = btn;
                        }
                        int finalI = i;
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                allAttributes[finalI] = attribute_value;

                                if(btn != lastClickedColorBtn) {
                                    btn.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), android.R.anim.fade_in));
                                    lastClickedColorBtn.setBackground(unselect(lastClickedColorBtn.getId()));
                                    btn.setBackground(select(btn.getId()));
                                }
                                lastClickedColorBtn = btn;
                            }
                        });
                        ll.addView(btn);
                    }
                    popupBinding.productModifierLl.addView(ll);
                }
                // Pills Selection
                else if(attribute.getDisplay_type().equalsIgnoreCase("pills")){
                    RadioGroup rg = new RadioGroup(contextpage);
                    rg.setOrientation(LinearLayout.HORIZONTAL);
                    RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(15, 0, 15, 0);
                    for(int x = 0; x < attribute_values.size(); x++){
                        Attribute_Value attribute_value = attribute_values.get(x);
                        RadioButton rb = new RadioButton(contextpage);
                        rb.setId(attribute_value.getId());
                        rb.setLayoutParams(params);
                        if(attribute_value.getPrice_extra() > 0){
                            rb.setText(attribute_value.getName() + " (+" + attribute_value.getDisplay_price_extra() + ")");
                        }else {
                            rb.setText(attribute_value.getName());
                        }
                        rb.setPadding(15, 0, 15, 0);
                        rb.setTextColor(getResources().getColor(R.color.pills_text_color));
                        rb.setBackground(getResources().getDrawable(R.drawable.pills_selector));
                        rb.setButtonDrawable(null);

                        //IS_CUSTOM
                        EditText custom_et = new EditText(contextpage);
                        int finalI = i;
                        rb.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                allAttributes[finalI] = attribute_value;

                                if(attribute_value.isIs_custom() && rb.isChecked()){
                                    allAttributes_custom[finalI] = custom_et;
                                    custom_et.setVisibility(View.VISIBLE);
                                }else{
                                    allAttributes_custom[finalI] = null;
                                    custom_et.setVisibility(View.GONE);
                                }
                            }
                        });

                        if(x == 0){
                            allAttributes[i] = attribute_value;
                            if(attribute_value.isIs_custom()){
                                allAttributes_custom[i] = custom_et;
                            }else {
                                allAttributes_custom[i] = null;
                            }
                            rb.setChecked(true);
                        }

                        rg.addView(rb);
                        if(attribute_value.isIs_custom()){
                            if(rb.isChecked()){
                                custom_et.setVisibility(View.VISIBLE);
                            }else{
                                custom_et.setVisibility(View.GONE);
                            }
                            custom_et.setTextSize(15);
                            custom_et.setPadding(10, 10, 10, 10);
                            rg.addView(custom_et);
                        }
                    }
                    popupBinding.productModifierLl.addView(rg);
                }

                Space blankSpace1 = new Space(contextpage);
                blankSpace1.setMinimumHeight(30);
                popupBinding.productModifierLl.addView(blankSpace1);
            }
        }

        if(!pos_config.isIface_orderline_customer_notes()){
            popupBinding.productModifierNote.setVisibility(View.GONE);
            popupBinding.textView1.setVisibility(View.INVISIBLE);
            popupBinding.textView1.setTextSize(0);
        }

        if(pos_config.isProduct_configurator() && !pos_config.isIface_orderline_customer_notes()) {
            RealmResults attribute_results = realm.where(Attribute.class).equalTo("product_tmpl_id", product.getProduct_tmpl_id())
                    .findAll();
            ArrayList<Attribute> attributes = (ArrayList<Attribute>) realm.copyFromRealm(attribute_results);
            if(attributes.size() == 0){
                popup.dismiss();
            }
        }
        popupBinding.productModifierPopupPositiveBtn.setText("Update");
//        if(pos_config.isIface_orderline_customer_notes()){
//            popupBinding.textView1.setVisibility(View.VISIBLE);
//            popupBinding.productModifierNote.setVisibility(View.VISIBLE);
//        }else{
//            popupBinding.textView1.setVisibility(View.GONE);
//            popupBinding.productModifierNote.setVisibility(View.GONE);
//        }

        popupBinding.productModifierPopupNegativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.dismiss();
            }
        });

        popupBinding.productModifierPopupPositiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double price_extra = 0.0;
                String product_attributesName = null;
                String customer_note = null;
                if(!pos_config.isIface_orderline_customer_notes()) {
                    customer_note = popupBinding.productModifierNote.getText().toString().trim();
                    if(customer_note.equalsIgnoreCase("")){
                        customer_note = null;
                    }
                }

                if(pos_config.isProduct_configurator()){
                    for(int i = 0; i < allAttributes.length; i++){
                        price_extra += allAttributes[i].getPrice_extra();
                    }

                    product_attributesName = "(";

                    for(int i = 0; i < allAttributes.length; i++){
                        product_attributesName += allAttributes[i].getName();
                        if(allAttributes_custom[i] != null){
                            product_attributesName += ": " + allAttributes_custom[i].getText().toString().trim();
                        }
                        if(i < (allAttributes.length - 1))
                            product_attributesName += ", ";
                    }

                    product_attributesName += ")";
                }

                boolean empty_custom_et = true, empty_customer_note = true;

                if(pos_config.isIface_orderline_customer_notes()){
                    if(popupBinding.productModifierNote.getText().toString().trim().length() > 0){
                        empty_customer_note = false;
                    }
                }else{
                    empty_customer_note = false;
                }

                if(pos_config.isProduct_configurator()){
                    boolean attribute_is_custom = false; //one of the attribute_value selected is custom
                    for(int i = 0; i < allAttributes.length; i++){
                        if(allAttributes[i].isIs_custom()){
                            attribute_is_custom = true;
                            if(allAttributes_custom[i] != null) {
                                if (allAttributes_custom[i].getText().toString().trim().length() > 0) {
                                    empty_custom_et = false;
                                }
                            }
                        }
                        System.out.println(allAttributes[i].getName() + " (is_custom): " + allAttributes[i].isIs_custom());
                        System.out.println("ATTRIBUTE_IS_CUSTOM: " + attribute_is_custom);
                    }

                    if(!attribute_is_custom){
                        empty_custom_et = false;
                    }
                }else{
                    empty_custom_et = false;
                }
                System.out.println("EMPTY_CUSTOMER_NOTE: " + empty_customer_note + "\n"
                        + "EMPTY_CUSTOM_ET: " + empty_custom_et);

                if(!empty_customer_note && !empty_custom_et) {
                    updateOrderLineConfig(order_line, product, product_attributesName,
                            price_extra, customer_note,
                            allAttributes, allAttributes_custom);
                    popup.dismiss();
                }else{
                    Toast.makeText(contextpage, "Please fill in all of the requirements", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private GradientDrawable unselect(int attribute_value_id){
        Attribute_Value attribute_value = realm.where(Attribute_Value.class).equalTo("id", attribute_value_id).findFirst();

        GradientDrawable unselected = new GradientDrawable();
        unselected.setShape(GradientDrawable.OVAL);
        unselected.setSize(85, 85);
        unselected.setColor(Color.parseColor(attribute_value.getHtml_color()));
        unselected.setStroke(5, getResources().getColor(R.color.darkGrey));

        return unselected;
    }
    private GradientDrawable select(int attribute_value_id){
        Attribute_Value attribute_value = realm.where(Attribute_Value.class).equalTo("id", attribute_value_id).findFirst();

        GradientDrawable selected = new GradientDrawable();
        selected.setShape(GradientDrawable.OVAL);
        selected.setSize(90, 90);
        selected.setColor(Color.parseColor(attribute_value.getHtml_color()));
        selected.setStroke(10, getResources().getColor(R.color.darkOrange));

        return selected;
    }
    //Update product to order
    private void updateOrderLineConfig(Order_Line order_line, Product product, String attributesName,
                                       double price_extra, String customer_note,
                                       Attribute_Value[] allAttributes, EditText[] allAttributes_custom){
        int index = order_lines.indexOf(order_line);
        double price_unit = product.getList_price() + price_extra;
        double amount_discount = 0.0;
        if(order_line.getDiscount_type() != null){
            if(order_line.getDiscount_type().equalsIgnoreCase("percentage")){
                amount_discount = price_unit * (order_line.getDiscount() / 100);
            }else if(order_line.getDiscount_type().equalsIgnoreCase("fixed_amount")){
                amount_discount = order_line.getDiscount();
            }
        }
        double price_before_discount = calculate_price_unit_excl_tax(product, price_unit) * order_line.getQty();
        //price_unit exclude tax include discount
        double price_unit_excl_tax = calculate_price_unit_excl_tax(product, (price_unit - amount_discount));
        double price_subtotal = price_unit_excl_tax * order_line.getQty();

        ArrayList<Product_Tax> product_taxes = new ArrayList<>();
        RealmResults product_taxes_results = realm.where(Product_Tax.class)
                .equalTo("product_tmpl_id", product.getProduct_tmpl_id()).findAll();
        product_taxes.addAll(realm.copyFromRealm(product_taxes_results));
        double price_subtotal_incl = calculate_price_subtotal_incl(product_taxes, price_subtotal);


        String product_name = product.getName();
        if(attributesName != null){
            product_name += " " + attributesName;
        }

        RealmList<Attribute_Value> attributeValues = new RealmList<Attribute_Value>();
        if(allAttributes != null) {
            attributeValues.addAll(Arrays.asList(allAttributes));
        }else{
            attributeValues = null;
        }

        Order_Line updated_order_line = new Order_Line(order_line.getLocal_order_line_id(), order_line.getOrder_line_id(),
                order_line.getName(), order_line.getQty(), price_unit, price_subtotal, price_subtotal_incl, price_before_discount,
                currencyDisplayFormat(price_unit), currencyDisplayFormat(price_subtotal),
                currencyDisplayFormat(price_subtotal_incl), currencyDisplayFormat(price_before_discount),
                product_name, customer_note, order_line.getDiscount_type(), order_line.getDiscount(),
                order_line.getDisplay_discount(), order_line.getTotal_cost(), order_line.getDisplay_total_cost(),
                price_extra, currencyDisplayFormat(price_extra), order_line.getOrder(), product, attributeValues);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(updated_order_line);
            }
        });
        order_lines.set(index, updated_order_line);
        orderLineAdapter.notifyDataSetChanged();
        updateOrderTotalAmount();

        if(!NetworkUtils.isNetworkAvailable(contextpage)){  //no internet
            Toast.makeText(contextpage, "No Internet Connection, product updated and stored in local", Toast.LENGTH_SHORT).show();
        }else{
            //call update order line api
            new UpdateOneOrderLineConfig(contextpage, order_line.getOrder().getLocal_order_id(),
                    order_line.getOrder().getOrder_id(), order_line.getLocal_order_line_id(), order_line.getOrder_line_id(),
                    customer_note, allAttributes, allAttributes_custom).execute();
        }
    }

    private double calculate_price_unit_excl_tax(Product product, double price_unit){
        double fixed = product.getAmount_tax_incl_fixed(),
                percent = product.getAmount_tax_incl_percent(),
                division = product.getAmount_tax_incl_division();

        double price_unit_excl_tax = ((price_unit  - fixed) / (1 + (percent / 100))) * (1 - (division / 100));

        return price_unit_excl_tax;
    }
    private double calculate_price_subtotal_incl(ArrayList<Product_Tax> product_taxes, double price_subtotal){
        double price_subtotal_incl;
        double total_taxes = 0.0, price = price_subtotal;
        double tax = 0.0;

        for(int i = 0; i < product_taxes.size(); i++){
            Product_Tax product_tax = product_taxes.get(i);

            if (product_tax.getAmount_type().equalsIgnoreCase("fixed")) {
                tax = product_tax.getAmount();
            } else if (product_tax.getAmount_type().equalsIgnoreCase("percent")) {
                tax = (price * (product_tax.getAmount() / 100));
            } else if (product_tax.getAmount_type().equalsIgnoreCase("division")) {
                tax = ((price / (1 - (product_tax.getAmount() / 100))) - price);
            }

            if (product_tax.isInclude_base_amount()) {    //TRUE
                price += tax;
            }

            total_taxes += tax;
        }
        price_subtotal_incl = price_subtotal + total_taxes;

        return price_subtotal_incl;
    }

    //Cart / order line
    @Override
    public void onOrderLineClick(int position) {
        //order_lines
        if(pos_config.isProduct_configurator() || pos_config.isIface_orderline_customer_notes()) {
            showProductModifier(order_lines.get(position), order_lines.get(position).getProduct());
        }

        Toast.makeText(contextpage, "" + order_lines.get(position).getOrder_line_id(), Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onOrderLineCancelClick(int position) {
        Order_Line deleteOrderLine = realm.where(Order_Line.class)
                .equalTo("local_order_line_id", order_lines.get(position).getLocal_order_line_id())
                .equalTo("order.local_order_id", currentOrder.getLocal_order_id())
                .findFirst();
        Order deleteOrder = realm.where(Order.class)
                .equalTo("local_order_id", currentOrder.getLocal_order_id())
                .findFirst();

        //popup
        AlertDialog.Builder builder = new AlertDialog.Builder(CustomerPage.this);
        builder.setMessage("Do you want to cancel the current order?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        order_lines.remove(position);
                        orderLineAdapter.notifyDataSetChanged();
                        if (order_lines.isEmpty()) {
                            if(currentOrder.getTable() != null) {
                                //Update table
                                RealmResults<Order> results = realm.where(Order.class)
                                        .equalTo("table.table_id", currentOrder.getTable().getTable_id())
                                        .and().notEqualTo("state", "paid").and()
                                        .notEqualTo("local_order_id", currentOrder.getLocal_order_id()).findAll();
                                if(results.size() == 0)
                                    tableOccupiedToVacant(currentOrder.getTable());
                            }
                            //delete order
                            cartSharedPreferenceEdit.putInt("orderingState", 0);
                            cartSharedPreferenceEdit.putInt("localOrderId", -1);
                            cartSharedPreferenceEdit.commit();

                            if(NetworkUtils.isNetworkAvailable(contextpage)){
                                new DeleteOneDraftOrder(contextpage, currentOrder.getOrder_id()).execute();
                            }
                            currentOrder = new Order();
                            refreshNote();
                            refreshCustomerNumber();
                        }
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                deleteOrderLine.deleteFromRealm();
                                if (order_lines.isEmpty()) {
                                    deleteOrder.deleteFromRealm();
                                }
                            }
                        });
                        Toast.makeText(contextpage, "Cancelled the current order", Toast.LENGTH_SHORT).show();
                        updateOrderTotalAmount();
                        resetPosType();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(contextpage, "Continue to order", Toast.LENGTH_SHORT).show();
                    }
                }).setCancelable(false);
        AlertDialog alert = builder.create();

        if(order_lines.size() == 1){
            alert.show();
        }else {
            Toast.makeText(contextpage, "Cancel: " + order_lines.get(position).getOrder_line_id(), Toast.LENGTH_SHORT).show();

            if(NetworkUtils.isNetworkAvailable(contextpage)) {
                new DeleteOneOrderLine(contextpage, order_lines.get(position).getOrder().getLocal_order_id(),
                        order_lines.get(position).getOrder().getOrder_id(),
                        order_lines.get(position).getOrder_line_id()).execute();

            }
            order_lines.remove(position);
            orderLineAdapter.notifyDataSetChanged();

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    deleteOrderLine.deleteFromRealm();
                }
            });
            updateOrderTotalAmount();
        }
    }
    @Override
    public void discountUpdateOrderLine(int position, Order_Line updateOrderLine) {
        order_lines.get(position).setPrice_subtotal(updateOrderLine.getPrice_subtotal());
        order_lines.get(position).setDisplay_price_subtotal(updateOrderLine.getDisplay_price_subtotal());
        order_lines.get(position).setPrice_subtotal_incl(updateOrderLine.getPrice_subtotal_incl());
        order_lines.get(position).setDisplay_price_subtotal_incl(updateOrderLine.getDisplay_price_subtotal_incl());
        order_lines.get(position).setDiscount(updateOrderLine.getDiscount());
        order_lines.get(position).setDisplay_discount(updateOrderLine.getDisplay_discount());
        String discount_type;
        if(updateOrderLine.getDiscount_type() != null) {
            if (updateOrderLine.getDiscount_type().equalsIgnoreCase("empty")) {
                order_lines.get(position).setDiscount_type(null);
                discount_type = "empty";
            } else {
                order_lines.get(position).setDiscount_type(updateOrderLine.getDiscount_type());
                discount_type = updateOrderLine.getDiscount_type();
            }
        }else{
            order_lines.get(position).setDiscount_type(null);
            discount_type = "empty";
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(updateOrderLine);
            }
        });
//        binding.cartInclude.cartOrdersRv.post(new Runnable() {
//            @Override
//            public void run() {
//                orderLineAdapter.notifyDataSetChanged();
//            }
//        });
        updateOrderTotalAmount();
        Order_Line order_line = order_lines.get(position);
        if(!NetworkUtils.isNetworkAvailable(contextpage)){  //no internet
            Toast.makeText(contextpage, "No Internet Connection, product added into order stored in local", Toast.LENGTH_SHORT).show();
        }else {
            new UpdateOneOrderLineQtyDisc(contextpage, order_line.getOrder().getLocal_order_id(), order_line.getOrder().getOrder_id(),
                    order_line.getLocal_order_line_id(), order_line.getOrder_line_id(), -1, discount_type,
                    order_line.getDiscount()).execute();
        }
    }
    @Override
    public void quantityUpdateOrderLine(int position, Order_Line updateOrderLine) {
        order_lines.get(position).setPrice_subtotal(updateOrderLine.getPrice_subtotal());
        order_lines.get(position).setDisplay_price_subtotal(updateOrderLine.getDisplay_price_subtotal());
        order_lines.get(position).setPrice_subtotal_incl(updateOrderLine.getPrice_subtotal_incl());
        order_lines.get(position).setDisplay_price_subtotal_incl(updateOrderLine.getDisplay_price_subtotal_incl());
        order_lines.get(position).setQty(updateOrderLine.getQty());
        order_lines.get(position).setPrice_before_discount(updateOrderLine.getPrice_before_discount());
        order_lines.get(position).setDisplay_price_before_discount(updateOrderLine.getDisplay_price_before_discount());
        order_lines.get(position).setTotal_cost(updateOrderLine.getTotal_cost());
        order_lines.get(position).setDisplay_total_cost(currencyDisplayFormat(updateOrderLine.getTotal_cost()));

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(updateOrderLine);
            }
        });
//        binding.cartInclude.cartOrdersRv.post(new Runnable() {
//            @Override
//            public void run() {
//                orderLineAdapter.notifyDataSetChanged();
//            }
//        });
        updateOrderTotalAmount();

        Order_Line order_line = order_lines.get(position);
        if(!NetworkUtils.isNetworkAvailable(contextpage)){  //no internet
            Toast.makeText(contextpage, "No Internet Connection, product updated in local", Toast.LENGTH_SHORT).show();
        }else {
            new UpdateOneOrderLineQtyDisc(contextpage, order_line.getOrder().getLocal_order_id(),
                    order_line.getOrder().getOrder_id(), order_line.getLocal_order_line_id(),
                    order_line.getOrder_line_id(), order_line.getQty(), null, -1).execute();
        }
    }

    private void updateOrderTotalAmount() {
        int orderState = cartSharedPreference.getInt("orderingState", 0);
        double order_subtotal = 0.0, total_price_subtotal_incl = 0.0, amount_total = 0.0, amount_order_discount = 0.0;
        double total_tax_amount = 0.0;
        if(orderState == 1) {
            for(int i = 0; i < order_lines.size(); i++){
                order_subtotal += order_lines.get(i).getPrice_subtotal();
                total_price_subtotal_incl += order_lines.get(i).getPrice_subtotal_incl();
            }
            amount_total = total_price_subtotal_incl;
            if(currentOrder.getDiscount_type() != null){
                if(currentOrder.getDiscount_type().equalsIgnoreCase("percentage")){
                    amount_order_discount = total_price_subtotal_incl * (currentOrder.getDiscount()/100);
                }else if(currentOrder.getDiscount_type().equalsIgnoreCase("fixed_amount")){
                    amount_order_discount = currentOrder.getDiscount();
                }
                amount_total -= amount_order_discount;
            }

            total_tax_amount = totalAllOrderLineTax();

            currentOrder.setAmount_total(amount_total);
            currentOrder.setDisplay_amount_total(currencyDisplayFormat(amount_total));
            currentOrder.setAmount_tax(total_tax_amount);
            currentOrder.setDisplay_amount_tax(currencyDisplayFormat(total_tax_amount));
            currentOrder.setAmount_subtotal(order_subtotal);
            currentOrder.setDisplay_amount_subtotal(currencyDisplayFormat(order_subtotal));

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.insertOrUpdate(currentOrder);
                }
            });
        }

        if((currentOrder.getDiscount_type() != null) && (orderState == 1)){
            binding.cartInclude.cartOrderSummaryDiscount.setText(currencyDisplayFormat(amount_order_discount));
        }else{
            binding.cartInclude.cartOrderSummaryDiscount.setText("- " + currencyDisplayFormat(0.00));
            binding.cartInclude.cartOrderSummaryDiscountRl.setVisibility(View.GONE);
            binding.cartInclude.cartOrderDiscountBtn.setTextColor(contextpage.getResources().getColor(R.color.darkOrange));
        }
        binding.cartInclude.cartOrderSummarySubtotal.setText(currencyDisplayFormat(order_subtotal));
        // Tax without rounding up
        total_tax_amount *= 100;  // moves two digits from right to left of dec point
        total_tax_amount = Math.floor(total_tax_amount);  // removes all reminaing dec digits
        total_tax_amount /= 100;  // moves two digits from left to right of dec point
        binding.cartInclude.cartOrderSummaryTax.setText(currencyDisplayFormat(total_tax_amount));
        binding.cartInclude.cartOrderSummaryPayableAmount.setText(currencyDisplayFormat(amount_total));
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
    private void refreshNote(){
        if(currentOrder.getOrder_id() != -1){
            if(currentOrder.getNote() != null){
                if(!currentOrder.getNote().isEmpty()) {
                    binding.cartInclude.cartOrderNoteBtn.setTextColor(contextpage.getResources().getColor(R.color.green));
                }
            }else{
                binding.cartInclude.cartOrderNoteBtn.setTextColor(contextpage.getResources().getColor(R.color.darkOrange));
            }
        }else{
            binding.cartInclude.cartOrderNoteBtn.setTextColor(contextpage.getResources().getColor(R.color.darkOrange));
        }
    }
    public void refreshCartCurrentCustomer(){
        int currentCustomerId = customerSharedPreference.getInt("customerID", -1);
        if((currentCustomerId != -1) && (currentCustomerId != 0)) {
            binding.cartInclude.cartCurrentCustomerName.setText(customerSharedPreference.getString("customerName", null));
            binding.cartInclude.cartCurrentCustomerId.setText("#" + customerSharedPreference.getInt("customerID", -1));
            binding.cartInclude.cartAddCustomer.setVisibility(View.GONE);
            binding.cartInclude.cartCurrentCustomerRl.setVisibility(View.VISIBLE);
        }else{
            binding.cartInclude.cartAddCustomer.setVisibility(View.VISIBLE);
            binding.cartInclude.cartCurrentCustomerRl.setVisibility(View.GONE);
        }
    }
    private void refreshCustomerNumber(){
        if(currentOrder.getOrder_id() == -1){
            binding.cartInclude.cartBtnNumberCustomer.setText("Guest(s)");
        }else{
            binding.cartInclude.cartBtnNumberCustomer.setText(currentOrder.getCustomer_count() + " Guest(s)");
        }
    }
    private void resetPosType(){
        if(currentOrder.getTable() == null){
            posOrderType.remove(dine_in_posType);
            dine_in_posType = "Dine-in";
            posOrderType.add(dine_in_posType);
            orderTypeAdapter.notifyDataSetChanged();
        }
    }

    private double totalAllOrderLineTax(){
        double total_tax_amount = 0.0;

        for(int i = 0; i < order_lines.size(); i++){
            double amount_tax = order_lines.get(i).getPrice_subtotal_incl() - order_lines.get(i).getPrice_subtotal();
            total_tax_amount += amount_tax;
        }

        return total_tax_amount;
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

    public void viewCustomerDetail(int customer_id){
        Bundle bundle = new Bundle();
        bundle.putInt("customer_id", customer_id);
        FragmentCustomerDetail fragmentCustomerDetail = new FragmentCustomerDetail();
        fragmentCustomerDetail.setArguments(bundle);

        ft = fm.beginTransaction();
        ft.replace(binding.customerPageFl.getId(), fragmentCustomerDetail, "CustomerDetails").commit();
        binding.customerPageActionBtn.setText("Customer");
        binding.customerPageActionBtn.setIcon(getDrawable(R.drawable.ic_customer));
        binding.customerPageTitle.setText("Customer Details");
        customerFragment = false;
    }
    public void setCurrentCustomer(Customer customer){

        int customer_id = -1;
        String name = null, email = null, phoneNo = null, identityNo = null, birthdate = null;

        if(customer != null){
            customer_id = customer.getCustomer_id();
            name = customer.getCustomer_name();
            email = customer.getCustomer_email();
            phoneNo = customer.getCustomer_phoneNo();
            identityNo = customer.getCustomer_identityNo();
            birthdate = customer.getCustomer_birthdate();
        }else{
            if(currentOrder.getLocal_order_id() != -1) {
                RealmResults results = realm.where(Order.class)
                        .equalTo("customer.customer_id", currentOrder.getCustomer().getCustomer_id())
                        .and().notEqualTo("local_order_id", currentOrder.getLocal_order_id()).findAll();
                if (results.size() < 1) {
                    Customer remove_Customer = realm.where(Customer.class)
                            .equalTo("customer_id", currentOrder.getCustomer().getCustomer_id())
                            .findFirst();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            remove_Customer.deleteFromRealm();
                        }
                    });
                }
                currentOrder.setPartner_id(-1);
            }
        }
        customerSharedPreferenceEdit.putInt("customerID", customer_id);
        customerSharedPreferenceEdit.putString("customerName", name);
        customerSharedPreferenceEdit.putString("customerEmail", email);
        customerSharedPreferenceEdit.putString("customerPhoneNo", phoneNo);
        customerSharedPreferenceEdit.putString("customerIdentityNo", identityNo);
        customerSharedPreferenceEdit.putString("customerBirthdate", birthdate);
        customerSharedPreferenceEdit.commit();

        if(currentOrder.getLocal_order_id() != -1) {
            currentOrder.setCustomer(customer);
            System.out.println("Iam INNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    if (customer != null) {
                        realm.insertOrUpdate(customer);
                    }
                    realm.insertOrUpdate(currentOrder);
                }
            });


            if(customer == null){
                customer_id = 0;
            }
            if(!NetworkUtils.isNetworkAvailable(contextpage)){
                Toast.makeText(contextpage, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }else {
                new SetOrderCustomer(contextpage, currentOrder.getOrder_id(),
                        currentOrder.getLocal_order_id(), customer_id).execute();
            }
        }

        refreshCartCurrentCustomer();
    }

    @Override
    public void onResume() {
        super.onResume();

        int currentOrderId = cartSharedPreference.getInt("localOrderId", -1);
        Order order = realm.where(Order.class).equalTo("local_order_id", currentOrderId).findFirst();
        if(order != null){
            currentOrder = realm.copyFromRealm(order);
        }
        order_lines.clear();
        getOrderLineFromRealm();

        updateOrderTotalAmount();
        refreshCartCurrentCustomer();
        refreshNote();
        refreshCustomerNumber();

        pos_config = realm.where(POS_Config.class).findFirst();
        currency = realm.copyFromRealm(realm.where(Currency.class).findFirst());
        //is_table_management?
        if(!pos_config.isIs_table_management()){
            binding.navbarLayoutInclude.navBarTables.setVisibility(View.GONE);
        }
    }
}