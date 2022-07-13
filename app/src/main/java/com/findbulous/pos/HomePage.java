package com.findbulous.pos;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.findbulous.pos.Adapters.CartOrderLineAdapter;
import com.findbulous.pos.Adapters.ProductAdapter;
import com.findbulous.pos.Adapters.ProductCategoryAdapter;
import com.findbulous.pos.Network.CheckConnection;
import com.findbulous.pos.Network.NetworkUtils;
import com.findbulous.pos.databinding.HomePageBinding;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class HomePage extends CheckConnection implements ProductCategoryAdapter.ProductCategoryClickInterface,
        ProductAdapter.OnItemClickListener, CartOrderLineAdapter.OnItemClickListener{

    private HomePageBinding binding;
    //Product Modifier Choice Popup
    private TextView product_name_modifier;
    private LinearLayout product_modifier_ll;
    private MaterialButton product_modifier_popup_negative_btn, product_modifier_popup_positive_btn;
    //Cart //Popup
    private Button add_discount_popup_negative_btn, add_discount_popup_positive_btn;
    private EditText add_discount_popup_et;
    private MaterialButton add_note_popup_negative_btn, add_note_popup_positive_btn;
    private EditText add_note_popup_et;
    //Cash in out popup
    private RadioButton cash_in_rb, cash_out_rb;
    private EditText cash_in_out_amount, cash_in_out_reason;
    private MaterialButton cash_in_out_cancel, cash_in_out_confirm;
    //Sync popup
    private TextView product_sync_btn, transactions_sync_btn;
    // Storing data into SharedPreferences
    private SharedPreferences cartSharedPreference, customerSharedPreference;
    // Creating an Editor object to edit(write to the file)
    private SharedPreferences.Editor cartSharedPreferenceEdit, customerSharedPreferenceEdit;
    //Adapter and arraylist for recyclerview
    private ProductAdapter productAdapter;
    private CartOrderLineAdapter orderLineAdapter;
    private ProductCategoryAdapter productCategoryAdapter;
    private ArrayList<Product> list;
    private ArrayList<Order_Line> order_lines;
    private ArrayList<POS_Category> product_categories;
    private ArrayList<POS_Category> categories_clicked_wo_child;
    //Realm
    private Realm realm;
    //Current order
    private Order currentOrder;
    //OnHold customer
    private Customer onHoldCustomer;
    //Update table while order onhold
    private Table updateTableOnHold;
    //Number of Customer Popup
    private EditText number_customer_et;

    private String statuslogin;
    private Context contextpage;
    private LoginPage login_page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextpage = HomePage.this;
        binding = DataBindingUtil.setContentView(this, R.layout.home_page);
        realm = Realm.getDefaultInstance();

        //Appbar Settings
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //Nav Settings
        binding.navbarLayoutInclude.navBarHome.setChecked(true);

        //Cart Settings
        cartSharedPreference = getSharedPreferences("CurrentOrder",MODE_MULTI_PROCESS);
        cartSharedPreferenceEdit = cartSharedPreference.edit();

        customerSharedPreference = getSharedPreferences("CurrentCustomer", MODE_MULTI_PROCESS);
        customerSharedPreferenceEdit = customerSharedPreference.edit();
        //Cart Current Customer
        refreshCartCurrentCustomer();

        //Body
        currentOrder = new Order();
        updateTableOnHold = new Table();
        onHoldCustomer = null;
        categories_clicked_wo_child = new ArrayList<>();
        //Menu Category Recycler view
        binding.productCategoryRv.setLayoutManager(new LinearLayoutManager(contextpage, LinearLayoutManager.HORIZONTAL, false));
        binding.productCategoryRv.setHasFixedSize(true);
        product_categories = new ArrayList<>();
        productCategoryAdapter = new ProductCategoryAdapter(product_categories, this, categories_clicked_wo_child);
        getProductCategoryFromRealm();
        binding.productCategoryRv.setAdapter(productCategoryAdapter);
        //Menu Product Recycler view
        binding.productListRv.setLayoutManager(new GridLayoutManager(contextpage, 4, LinearLayoutManager.VERTICAL, false));
        binding.productListRv.setHasFixedSize(true);
        list = new ArrayList<>();
        productAdapter = new ProductAdapter(list, this);
        getProductFromRealm();
        binding.productListRv.setAdapter(productAdapter);

        //Cart / Order Line Recycler view
        binding.cartInclude.cartOrdersRv.setLayoutManager(new LinearLayoutManager(contextpage, LinearLayoutManager.VERTICAL, false));
        binding.cartInclude.cartOrdersRv.setHasFixedSize(true);
        order_lines = new ArrayList<>();
        orderLineAdapter = new CartOrderLineAdapter(order_lines, this);
        getOrderLineFromRealm();
        binding.cartInclude.cartOrdersRv.setAdapter(orderLineAdapter);

        int currentOrderId = cartSharedPreference.getInt("orderId", -1);
        Order order = realm.where(Order.class).equalTo("order_id", currentOrderId).findFirst();
        if(order != null){
            currentOrder = realm.copyFromRealm(order);
        }
        //Cart Discount
        if(currentOrder.isHas_order_discount()){
            binding.cartInclude.cartOrderSummaryDiscountRl.setVisibility(View.VISIBLE);
            binding.cartInclude.cartOrderSummaryDiscount.setText("- RM " + String.format("%.2f", currentOrder.getAmount_order_discount()));
            binding.cartInclude.cartOrderDiscountBtn.setTextColor(contextpage.getResources().getColor(R.color.green));
        }

        //OnClickListener
        //Menu
        binding.allCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getProductCategoryFromRealm();
                getProductFromRealm();
                categories_clicked_wo_child.clear();
            }
        });
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
                    Toast.makeText(contextpage, "Refresh Button Clicked", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(contextpage, "Cash in / out Button Clicked", Toast.LENGTH_SHORT).show();
                }
            }
        );
        }
        //Navigation bar buttons
        {
        binding.navbarLayoutInclude.navBarHome.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view) {
                   Toast.makeText(contextpage, "Home Button Clicked", Toast.LENGTH_SHORT).show();
               }
           }
        );
        binding.navbarLayoutInclude.navBarCustomers.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view) {
                   Intent intent = new Intent(contextpage, CustomerPage.class);
                   startActivity(intent);
                   finish();
                   Toast.makeText(contextpage, "Customers Button Clicked", Toast.LENGTH_SHORT).show();
               }
           }
        );
        binding.navbarLayoutInclude.navBarTables.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view) {
                   Intent intent = new Intent(contextpage, TablePage.class);
                   startActivity(intent);
                   finish();
                   Toast.makeText(contextpage, "Tables Button Clicked", Toast.LENGTH_SHORT).show();
               }
           }
        );
        binding.navbarLayoutInclude.navBarCashier.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view) {
                   Intent intent = new Intent(contextpage, CashierPage.class);
                   startActivity(intent);
                   finish();
                   Toast.makeText(contextpage, "Cashier Button Clicked", Toast.LENGTH_SHORT).show();
               }
           }
        );
        binding.navbarLayoutInclude.navBarOrders.setOnClickListener(new View.OnClickListener(){
              @Override
              public void onClick(View view) {
                  Intent intent = new Intent(contextpage, OrderPage.class);
                  startActivity(intent);
                  finish();
                  Toast.makeText(contextpage, "Orders Button Clicked", Toast.LENGTH_SHORT).show();
              }
          }
        );
        binding.navbarLayoutInclude.navBarReports.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view) {
                   Toast.makeText(contextpage, "Reports Button Clicked", Toast.LENGTH_SHORT).show();
               }
           }
        );
        binding.navbarLayoutInclude.navBarSettings.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view) {
                   Intent intent = new Intent(contextpage, SettingPage.class);
                   startActivity(intent);
                   finish();
                   Toast.makeText(contextpage, "Settings Button Clicked", Toast.LENGTH_SHORT).show();
               }
           }
        );
        binding.navbarLayoutInclude.navBarProfile.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view) {
                   Toast.makeText(contextpage, "Profile Button Clicked", Toast.LENGTH_SHORT).show();
               }
           }
        );
        binding.navbarLayoutInclude.navbarLogout.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view) {
                   Intent intent = new Intent(contextpage, LoginPage.class);
                   startActivity(intent);
                   finish();

                   Toast.makeText(contextpage, "Logout Button Clicked", Toast.LENGTH_SHORT).show();
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
                if(customerSharedPreference.getInt("customerID", -1) == -1){
                    Toast.makeText(contextpage, "Please add a customer to this order before adding note", Toast.LENGTH_SHORT).show();
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
        binding.cartInclude.cartOrderSummaryHoldBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(currentOrder.getOrder_id() == -1){
                    Toast.makeText(contextpage, "No order to hold", Toast.LENGTH_SHORT).show();
                }else if(customerSharedPreference.getInt("customerID", -1) == -1){
                    Toast.makeText(contextpage, "Please add a customer to this order before hold", Toast.LENGTH_SHORT).show();
                }else {
                    showCartOrderAddNotePopup(binding.cartInclude.cartOrderSummaryHoldBtn.getId());
                }
            }
        });
        binding.cartInclude.cartOrderSummaryProceedBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                int current_order_id = cartSharedPreference.getInt("orderId", -1);
                int orderTypePosition = cartSharedPreference.getInt("orderTypePosition", -1);
                if(current_order_id == -1){ //no order and no order_line(products)
                    Toast.makeText(contextpage, "Please proceed payment with at least 1 product", Toast.LENGTH_SHORT).show();
                }else {//got order
                    RealmResults<Order_Line> results = realm.where(Order_Line.class).equalTo("order.order_id", current_order_id)
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
//        binding.cartInclude.cartBtnPosType.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                //showOrderTypeChoicePopup(view);
//                Toast.makeText(contextpage, "Order Type Button Clicked", Toast.LENGTH_SHORT).show();
//            }
//        });
        ArrayAdapter<String> orderTypes = new ArrayAdapter<String>(contextpage, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.order_types)){
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent){
                View v = null;
                v = super.getDropDownView(position, null, parent);

                // If this is the selected item position
                if (position == binding.cartInclude.cartBtnPosType.getSelectedItemPosition()) {
                    if(position == (binding.cartInclude.cartBtnPosType.getCount() - 1)){ //last one
                        v.setBackground(getResources().getDrawable(R.drawable.box_btm_corner_dark_orange));
                    }else { //not the last one
                        v.setBackgroundColor(getResources().getColor(R.color.darkOrange));
                    }
                }
                else {
                    // for other views
                    if(position != (binding.cartInclude.cartBtnPosType.getCount() - 1)) { // not the last one
                        v.setBackgroundColor(getResources().getColor(R.color.lightGrey));
                    }else{  //last one
                        v.setBackground(getResources().getDrawable(R.drawable.box_btm_corner_light_grey));
                    }
                }
                return v;
            }
        };
        orderTypes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.cartInclude.cartBtnPosType.setAdapter(orderTypes);
        binding.cartInclude.cartBtnPosType.setDropDownVerticalOffset(80);
        binding.cartInclude.cartBtnPosType.setSelection(cartSharedPreference.getInt("orderTypePosition", 1));
        binding.cartInclude.cartBtnPosType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int orderTypePosition = binding.cartInclude.cartBtnPosType.getSelectedItemPosition(); //0 = takeaway, 1 = dine-in
                //if change from dine-in to takeaway \/ takeaway to dine-in
                if((orderTypePosition == 0) && (currentOrder.getOrder_id() != -1)
                    && (currentOrder.getTable() != null)){//takeaway && order now && order has table
                    //update table
                    tableOccupiedToVacant(currentOrder.getTable());

                    RealmResults<Order_Line> order_lines = realm.where(Order_Line.class).equalTo("order.order_id", currentOrder.getOrder_id())
                            .findAll();
                    if(order_lines.size() < 1){// when order_lines empty, delete the order
                        //delete order
                        Order deleteOrder = realm.where(Order.class)
                                .equalTo("order_id", currentOrder.getOrder_id())
                                .findFirst();
                        cartSharedPreferenceEdit.putInt("orderingState", 0);
                        cartSharedPreferenceEdit.putInt("orderId", -1);
                        cartSharedPreferenceEdit.commit();
                        currentOrder = new Order();
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
                    }
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
                if(currentOrder.isHas_order_discount()) {
                    currentOrder.setAmount_order_discount(0.0);
                    currentOrder.setHas_order_discount(false);
                    currentOrder.setIs_percentage(true);
                    updateOrderTotalAmount();
                }
                binding.cartInclude.cartOrderSummaryDiscount.setText("- RM 0.00");
                binding.cartInclude.cartOrderSummaryDiscountRl.setVisibility(View.GONE);
                binding.cartInclude.cartOrderDiscountBtn.setTextColor(contextpage.getResources().getColor(R.color.darkOrange));
            }
        });
        }
    }

    private void getProductCategoryFromRealm(){
        //doesnt appear in any other realmlist
        RealmResults<POS_Category> results = realm.where(POS_Category.class).findAll();
        ArrayList<POS_Category> category_list = new ArrayList<>();
        ArrayList<POS_Category> temp_sub_category = new ArrayList<>();
        ArrayList<POS_Category> not_first_level_category = new ArrayList<>();
        category_list.addAll(realm.copyFromRealm(results));
        for(int x = 0; x < category_list.size(); x++) {
            for (int i = 0; i < category_list.size(); i++) {
                temp_sub_category.clear();
                temp_sub_category.addAll(category_list.get(i).getPos_categories());
                for (int j = 0; j < temp_sub_category.size(); j++) {
                    if (category_list.get(x).getPos_categ_id() == temp_sub_category.get(j).getPos_categ_id()){
                        not_first_level_category.add(category_list.get(x));
                    }
                }
            }
        }
        category_list.removeAll(not_first_level_category);
        product_categories.clear();
        product_categories.addAll(category_list);
        productCategoryAdapter.notifyDataSetChanged();
    }
    private void getProductCategoryFromRealm(POS_Category category){
        ArrayList<POS_Category> allRelatedCategory = new ArrayList<>();

        RealmResults<POS_Category> results = realm.where(POS_Category.class).findAll();
        ArrayList<POS_Category> all_category_list = new ArrayList<>();
        all_category_list.addAll(realm.copyFromRealm(results));

        allRelatedCategory.addAll(getAllRelatedCategory(category, all_category_list));

        product_categories.clear();
        product_categories.addAll(allRelatedCategory);
        productCategoryAdapter.notifyDataSetChanged();
    }
    private ArrayList<POS_Category> getAllRelatedCategory(POS_Category category, ArrayList<POS_Category> category_list){
        //add parent & itself
        ArrayList<POS_Category> all_list = new ArrayList<>();
        all_list.addAll(getParentCategry(category, category_list));
        categories_clicked_wo_child.clear();
        categories_clicked_wo_child.addAll(all_list);
        //add sub-category
        for(int i = 0; i < category.getPos_categories().size(); i++){
            all_list.add(category.getPos_categories().get(i));
        }
        return all_list;
    }
    private ArrayList<POS_Category> getParentCategry(POS_Category category, ArrayList<POS_Category> category_list){
        ArrayList<POS_Category> parent_list = new ArrayList<>();

        int counter = 0;
        while(counter < category_list.size()){
            RealmList<POS_Category> sub_category = category_list.get(counter).getPos_categories();
            POS_Category parent_category = null;
            for(int i = 0; i < sub_category.size(); i++){
                if(category.getPos_categ_id() == sub_category.get(i).getPos_categ_id()){
                    parent_category = category_list.get(counter);
                    parent_list.addAll(getParentCategry(parent_category, category_list));
                }
            }
            counter++;
        }
        parent_list.add(category);
        return parent_list;
    }
    public ArrayList<POS_Category> getCategories_clicked_wo_child(){
        return categories_clicked_wo_child;
    }

    private void getProductFromRealm(){
        list.clear();
        RealmResults<Product> results = realm.where(Product.class).findAll();
        list.addAll(realm.copyFromRealm(results));
        productAdapter.notifyDataSetChanged();
    }
    private void getOrderLineFromRealm(){
        int orderId = cartSharedPreference.getInt("orderId", -1);
        if(orderId > -1) {
            RealmResults<Order_Line> results = realm.where(Order_Line.class).equalTo("order.order_id", orderId).findAll();
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
            customer = null;
        }

        return customer;
    }

    private void showCashInOut() {
        PopupWindow popup = new PopupWindow(contextpage);
        View layout = getLayoutInflater().inflate(R.layout.cash_in_out_popup, null);
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
        WindowManager wm = (WindowManager) HomePage.this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);

        cash_in_rb = (RadioButton)layout.findViewById(R.id.cash_in_rb);
        cash_out_rb = (RadioButton)layout.findViewById(R.id.cash_out_rb);
        cash_in_out_amount = (EditText)layout.findViewById(R.id.cash_in_out_amount_et);
        cash_in_out_reason = (EditText)layout.findViewById(R.id.cash_in_out_reason_et);
        cash_in_out_cancel = (MaterialButton)layout.findViewById(R.id.cash_in_out_cancel_btn);
        cash_in_out_confirm = (MaterialButton)layout.findViewById(R.id.cash_in_out_confirm_btn);

        cash_in_out_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.dismiss();
                Toast.makeText(contextpage, "Cancel", Toast.LENGTH_SHORT).show();
            }
        });
        cash_in_out_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.dismiss();
                Toast.makeText(contextpage, "Confirm", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showRefreshPopup(View view) {
        PopupWindow popup = new PopupWindow(contextpage);
        View layout = getLayoutInflater().inflate(R.layout.toolbar_sync_popup, null);
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
        popup.showAsDropDown(binding.toolbarLayoutIncl.toolbarRefresh, -120, 0);

        //Popup Buttons
        product_sync_btn = (TextView) layout.findViewById(R.id.sync_product_btn);
        transactions_sync_btn = (TextView)layout.findViewById(R.id.sync_transaction_btn);

        product_sync_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(contextpage, "refresh / sync products", Toast.LENGTH_SHORT).show();
                popup.dismiss();
            }
        });

        transactions_sync_btn.setOnClickListener(new View.OnClickListener() {
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
        WindowManager wm = (WindowManager) HomePage.this.getSystemService(Context.WINDOW_SERVICE);
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
                            currentOrder.setCustomer_count(Integer.valueOf(1));
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
        View layout = getLayoutInflater().inflate(R.layout.cart_order_add_note_popup, null);
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
        WindowManager wm = (WindowManager) HomePage.this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);

        add_note_popup_negative_btn = (MaterialButton)layout.findViewById(R.id.add_note_popup_negative_btn);
        add_note_popup_positive_btn = (MaterialButton)layout.findViewById(R.id.add_note_popup_positive_btn);
        add_note_popup_et = (EditText)layout.findViewById(R.id.add_note_popup_et);

        if(currentOrder.getOrder_id() != -1) {
            if(currentOrder.getNote() != null){
                if(!currentOrder.getNote().isEmpty()) {
                    add_note_popup_et.setText(currentOrder.getNote());
                }
            } else {
                add_note_popup_et.setText("");
            }
        }else{
            add_note_popup_et.setText("");
        }
        if(btnID == binding.cartInclude.cartOrderNoteBtn.getId()){
            add_note_popup_positive_btn.setText("Add & Update");
        }else if(btnID == binding.cartInclude.cartOrderSummaryHoldBtn.getId()){
            add_note_popup_positive_btn.setText("Proceed");
        }

        add_note_popup_negative_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                popup.dismiss();
                Toast.makeText(contextpage, "Cancel", Toast.LENGTH_SHORT).show();
            }
        });
        add_note_popup_positive_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String note = add_note_popup_et.getText().toString();
                if(!note.isEmpty()){
                    if(btnID != binding.cartInclude.cartOrderSummaryHoldBtn.getId()) {
                        binding.cartInclude.cartOrderNoteBtn.setTextColor(contextpage.getResources().getColor(R.color.green));
                    }
                }else{
                    binding.cartInclude.cartOrderNoteBtn.setTextColor(contextpage.getResources().getColor(R.color.darkOrange));
                }

                if(btnID == binding.cartInclude.cartOrderSummaryHoldBtn.getId()){//Proceed
                    if((currentOrder.getTable() == null) && (cartSharedPreference.getInt("orderTypePosition", -1) == 1)) {
                        // the order has no table + it is dine-in
                        currentOrder.setNote(note);
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.insertOrUpdate(currentOrder);
                            }
                        });
                        Intent intent = new Intent(contextpage, TablePage.class);
                        startActivity(intent);
                        finish();
                        Toast.makeText(contextpage, "Choose a table for this order", Toast.LENGTH_SHORT).show();
                    }else {
                        onHoldCustomer = getCurrentCustomer();
                        currentOrder.setCustomer(onHoldCustomer);
                        currentOrder.setState("onHold");
                        currentOrder.setNote(note);

                        if (currentOrder.getTable() != null) {
                            Table result = realm.where(Table.class).equalTo("table_id", currentOrder.getTable().getTable_id()).findFirst();
                            updateTableOnHold = realm.copyFromRealm(result);
                            updateTableOnHold.setState("H");
                        }
                        //update
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.insertOrUpdate(currentOrder);
                                realm.insertOrUpdate(onHoldCustomer);
                                if (currentOrder.getTable() != null)
                                    realm.insertOrUpdate(updateTableOnHold);
                            }
                        });
                        customerSharedPreferenceEdit.putInt("customerID", -1);
                        customerSharedPreferenceEdit.putString("customerName", null);
                        customerSharedPreferenceEdit.putString("customerEmail", null);
                        customerSharedPreferenceEdit.putString("customerPhoneNo", null);
                        customerSharedPreferenceEdit.putString("customerIdentityNo", null);
                        customerSharedPreferenceEdit.putString("customerBirthdate", null);
                        customerSharedPreferenceEdit.commit();
                        cartSharedPreferenceEdit.putInt("orderingState", 0);
                        cartSharedPreferenceEdit.putInt("orderId", -1);
                        cartSharedPreferenceEdit.commit();

                        updateOrderTotalAmount();
                        refreshCartCurrentCustomer();
                        currentOrder = new Order();
                        updateTableOnHold = new Table();
                        order_lines.clear();
                        orderLineAdapter.notifyDataSetChanged();
                        refreshNote();
                        refreshCustomerNumber();
                        Toast.makeText(contextpage, "Proceed", Toast.LENGTH_SHORT).show();
                    }
                }else{//Add Note or Update
                    currentOrder.setNote(note);
                    //update note
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.insertOrUpdate(currentOrder);
                        }
                    });
                    Toast.makeText(contextpage, "Added & Updated", Toast.LENGTH_SHORT).show();
                }
                popup.dismiss();
            }
        });
    }
    private void showCartOrderAddDiscountPopup(View view) {
        PopupWindow popup = new PopupWindow(contextpage);
        View layout = getLayoutInflater().inflate(R.layout.cart_order_add_discount_popup, null);
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
        binding.cartInclude.cartOrderSummaryDiscountRl.setVisibility(View.VISIBLE);
        binding.cartInclude.tvDiscount.setTextColor(contextpage.getResources().getColor(R.color.darkOrange));
        binding.cartInclude.cartOrderDiscountBtn.setTextColor(contextpage.getResources().getColor(R.color.green));
        //popup.showAsDropDown(binding.cartInclude.tvDiscount, -620, -180);
        popup.showAtLocation(binding.getRoot(), Gravity.CENTER, 0, 0);
        //blur background
        View container = (View) popup.getContentView().getParent();
        WindowManager wm = (WindowManager) HomePage.this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);
        //Popup Buttons
        add_discount_popup_negative_btn = (Button)layout.findViewById(R.id.add_discount_popup_negative_btn);
        add_discount_popup_positive_btn = (Button)layout.findViewById(R.id.add_discount_popup_positive_btn);
        add_discount_popup_et = (EditText)layout.findViewById(R.id.add_discount_popup_et);
        RadioButton add_discount_popup_radio_btn_amount = layout.findViewById(R.id.add_discount_popup_radio_btn_amount);
        RadioButton add_discount_popup_radio_btn_percentage = layout.findViewById(R.id.add_discount_popup_radio_btn_percentage);

        if((currentOrder.getOrder_id() != -1) && (currentOrder.isHas_order_discount())) {
            if(currentOrder.isIs_percentage()){
                add_discount_popup_et.setText(String.valueOf(currentOrder.getDiscount_percent()));
                add_discount_popup_radio_btn_percentage.setChecked(true);
                add_discount_popup_radio_btn_amount.setChecked(false);
                add_discount_popup_et.setInputType(InputType.TYPE_CLASS_NUMBER);
            }else{
                add_discount_popup_et.setText(String.valueOf(currentOrder.getAmount_order_discount()));
                add_discount_popup_radio_btn_percentage.setChecked(false);
                add_discount_popup_radio_btn_amount.setChecked(true);
                add_discount_popup_et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }
        }else{
            add_discount_popup_radio_btn_percentage.setChecked(true);
            add_discount_popup_radio_btn_amount.setChecked(false);
            add_discount_popup_et.setText("0");
        }

        add_discount_popup_negative_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                popup.dismiss();
            }
        });

        add_discount_popup_positive_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String userInput = add_discount_popup_et.getText().toString();
                if((userInput.charAt(0) == '.') && (userInput.length() == 1)){
                    userInput = "0.0";
                }
                double discountInDouble = (!userInput.isEmpty())? Double.valueOf(userInput): 0.0;
                if(discountInDouble > 0.0) {
                    String discount = String.format("%.2f", discountInDouble);

                    if (add_discount_popup_radio_btn_amount.isChecked()) {
                        currentOrder.setIs_percentage(false);
                    } else {
                        currentOrder.setIs_percentage(true);
                    }

                    if(currentOrder.isIs_percentage()){
                        int order_discount_percent = (int)(Double.parseDouble(discount));
                        double subtotal = 0;
                        for(int i=0; i < order_lines.size(); i++){
                            subtotal += order_lines.get(i).getPrice_subtotal();
                        }
                        double amount_order_discount = subtotal * ((double)order_discount_percent / 100);
                        discount = String.format("%.2f", amount_order_discount);
                        currentOrder.setDiscount_percent(order_discount_percent);
                    }else{
                        currentOrder.setDiscount_percent(0);
                    }
                    binding.cartInclude.cartOrderSummaryDiscount.setText("- RM " + discount);

                    currentOrder.setHas_order_discount(true);
                    currentOrder.setAmount_order_discount(Double.valueOf(discount));
                    updateOrderTotalAmount();
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

        add_discount_popup_radio_btn_amount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_discount_popup_et.setText(String.format("%.2f", Double.valueOf(add_discount_popup_et.getText().toString())));
                add_discount_popup_et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                add_discount_popup_radio_btn_amount.setChecked(true);
                add_discount_popup_radio_btn_percentage.setChecked(false);
            }
        });
        add_discount_popup_radio_btn_percentage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_discount_popup_et.setText(String.format("%d", Math.round(Double.valueOf(add_discount_popup_et.getText().toString()))));
                add_discount_popup_et.setInputType(InputType.TYPE_CLASS_NUMBER);
                add_discount_popup_radio_btn_percentage.setChecked(true);
                add_discount_popup_radio_btn_amount.setChecked(false);
            }
        });
    }

    private void showProductDetails(Product product){
        PopupWindow popup = new PopupWindow(contextpage);
        View layout = getLayoutInflater().inflate(R.layout.product_details_popup, null);
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
        WindowManager wm = (WindowManager) HomePage.this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);

        TextView product_name = layout.findViewById(R.id.product_details_name);
        TextView product_price = layout.findViewById(R.id.product_details_price);
        TextView done_btn = layout.findViewById(R.id.product_details_done_btn);

        product_name.setText(product.getName());
        product_price.setText(String.format("RM %.2f", product.getList_price()));


        done_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.dismiss();
            }
        });
    }
    private void showProductModifier(Product product, boolean fromMenu){
        PopupWindow popup = new PopupWindow(contextpage);
        View layout = getLayoutInflater().inflate(R.layout.product_modifier_popup, null);
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
        WindowManager wm = (WindowManager) HomePage.this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);

        product_name_modifier = layout.findViewById(R.id.product_name_modifier_popup);
        product_modifier_ll = layout.findViewById(R.id.product_modifier_ll);
        product_modifier_popup_negative_btn = layout.findViewById(R.id.product_modifier_popup_negative_btn);
        product_modifier_popup_positive_btn = layout.findViewById(R.id.product_modifier_popup_positive_btn);

        product_name_modifier.setText(product.getName());

        // Add view for extra modifier
        TextView size_tv = new TextView(contextpage);
        size_tv.setText("Size");
        size_tv.setTextSize(20);
        product_modifier_ll.addView(size_tv);

        if(fromMenu){
            product_modifier_popup_positive_btn.setText("Add to Cart");
        }else{
            product_modifier_popup_positive_btn.setText("Update");
        }

        product_modifier_popup_negative_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.dismiss();
            }
        });

        product_modifier_popup_positive_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fromMenu) {
                    addProductToOrder(product);
                }else{
                    addOrUpdateProductToOrder(product);
                }
                popup.dismiss();
            }
        });
    }
    //Add & Update product to order
    private void addProductToOrder(Product product){
        int orderState = cartSharedPreference.getInt("orderingState", 0);
        if(orderState == 0) {
            cartSharedPreferenceEdit.putInt("orderingState", 1);
            cartSharedPreferenceEdit.commit();
            addNewOrder();
        }

        Number id = realm.where(Order_Line.class).max("order_line_id");

        int nextID = -1;
        System.out.println(id);
        if(id == null){
            nextID = 1;
        }else{
            nextID = id.intValue() + 1;
        }
//        double amount_total = currentOrder.getAmount_total() + product.getProduct_price();
//        currentOrder.setAmount_total(amount_total);
        Order_Line newOrderLine = new Order_Line(nextID, String.valueOf(nextID), 1,
                product.getList_price(), product.getList_price(), 0, currentOrder,product);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(newOrderLine);
//                realm.insertOrUpdate(currentOrder);
            }
        });
        order_lines.add(newOrderLine);
        orderLineAdapter.notifyDataSetChanged();
        updateOrderTotalAmount();
    }
    //NOT YET DONE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!MODIFIER related
    private void addOrUpdateProductToOrder(Product product){

    }
    private void addNewOrder(){
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
        Date today = new Date();
        Order order = new Order();
        Number id = realm.where(Order.class).max("order_id");

        int nextID = -1;
        System.out.println(id);
        if(id == null){
            nextID = 1;
        }else{
            nextID = id.intValue() + 1;
        }

        order.setOrder_id(nextID);
        order.setDate_order(df.format(today));
        order.setState("draft");
        order.setCustomer_count(1);

        currentOrder = order;
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(order);
            }
        });
        cartSharedPreferenceEdit.putInt("orderId", order.getOrder_id());
        cartSharedPreferenceEdit.commit();
        refreshCustomerNumber();
    }

    //Cart / order line
    @Override
    public void onOrderLineClick(int position) {
        //order_lines
        showProductModifier(order_lines.get(position).getProduct(), false);
        Toast.makeText(contextpage, "" + order_lines.get(position).getOrder_line_id(), Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onOrderLineCancelClick(int position) {
        Order_Line deleteOrderLine = realm.where(Order_Line.class)
                .equalTo("order_line_id", order_lines.get(position).getOrder_line_id())
                .equalTo("order.order_id", currentOrder.getOrder_id())
                .findFirst();
        Order deleteOrder = realm.where(Order.class)
                .equalTo("order_id", currentOrder.getOrder_id())
                .findFirst();

        //popup
        AlertDialog.Builder builder = new AlertDialog.Builder(HomePage.this);
        builder.setMessage("Do you want to cancel the current order?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        order_lines.remove(position);
                        orderLineAdapter.notifyDataSetChanged();
                        if (order_lines.isEmpty()) {
                            if(currentOrder.getTable() != null) {
                                //Update table
                                tableOccupiedToVacant(currentOrder.getTable());
                            }
                            //delete order
                            cartSharedPreferenceEdit.putInt("orderingState", 0);
                            cartSharedPreferenceEdit.putInt("orderId", -1);
                            cartSharedPreferenceEdit.commit();
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
    public void discountUpdateOrderLine(int position, int discount) {
        double subtotal;
        double price_total = order_lines.get(position).getPrice_total();
        subtotal = price_total - ((price_total * discount) / 100);
        subtotal = Double.valueOf(String.format("%.2f", subtotal));
        order_lines.get(position).setPrice_subtotal(subtotal);
        order_lines.get(position).setDiscount(discount);
        Order_Line updateOrderLine = order_lines.get(position);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(updateOrderLine);
            }
        });
        binding.cartInclude.cartOrdersRv.post(new Runnable() {
            @Override
            public void run() {
                orderLineAdapter.notifyDataSetChanged();
            }
        });
        updateOrderTotalAmount();
    }
    @Override
    public void quantityUpdateOrderLine(int position, int quantity) {
        double price_total = quantity * order_lines.get(position).getProduct().getList_price();
        int discount = order_lines.get(position).getDiscount();
        double subtotal = price_total - ((price_total * discount) / 100);
        order_lines.get(position).setPrice_subtotal(subtotal);
        order_lines.get(position).setQty(quantity);
        order_lines.get(position).setPrice_total(price_total);
        Order_Line updateOrderLine = order_lines.get(position);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(updateOrderLine);
            }
        });
        binding.cartInclude.cartOrdersRv.post(new Runnable() {
            @Override
            public void run() {
                orderLineAdapter.notifyDataSetChanged();
            }
        });
        updateOrderTotalAmount();
    }

    private void updateOrderTotalAmount(){
        int orderState = cartSharedPreference.getInt("orderingState", 0);
        double tax = 0.0, order_subtotal = 0.0, amount_total = 0.0;
        if(orderState == 1) {
            for(int i = 0; i < order_lines.size(); i++){
                order_subtotal += order_lines.get(i).getPrice_subtotal();
            }
            //Default / testing tax = 10%
            tax = (order_subtotal * 10) / 100;
            tax = Double.valueOf(String.format("%.2f", tax));
            amount_total = order_subtotal + tax;
            if(currentOrder.isHas_order_discount()){
                if(currentOrder.isIs_percentage()){
                    double amount_order_discount = order_subtotal * ((double)currentOrder.getDiscount_percent()/100);
                    currentOrder.setAmount_order_discount(Double.valueOf(String.format("%.2f", amount_order_discount)));
                }
                amount_total -= currentOrder.getAmount_order_discount();
            }
            amount_total = Double.valueOf(String.format("%.2f", amount_total));
            currentOrder.setAmount_total(amount_total);
            currentOrder.setAmount_tax(tax);

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.insertOrUpdate(currentOrder);
                }
            });
        }

        if((currentOrder.isHas_order_discount()) && (orderState == 1)){
            binding.cartInclude.cartOrderSummaryDiscount.setText(String.format("- RM %.2f", currentOrder.getAmount_order_discount()));
        }else{
            binding.cartInclude.cartOrderSummaryDiscount.setText("- RM 0.00");
            binding.cartInclude.cartOrderSummaryDiscountRl.setVisibility(View.GONE);
            binding.cartInclude.cartOrderDiscountBtn.setTextColor(contextpage.getResources().getColor(R.color.darkOrange));
        }
        binding.cartInclude.cartOrderSummarySubtotal.setText(String.format("RM %.2f", order_subtotal));
        binding.cartInclude.cartOrderSummaryTax.setText(String.format("RM %.2f", tax));
        binding.cartInclude.cartOrderSummaryPayableAmount.setText(String.format("RM %.2f", amount_total));
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
                };
            }else{
                binding.cartInclude.cartOrderNoteBtn.setTextColor(contextpage.getResources().getColor(R.color.darkOrange));
            }
        }else{
            binding.cartInclude.cartOrderNoteBtn.setTextColor(contextpage.getResources().getColor(R.color.darkOrange));
        }
    }
    private void refreshCartCurrentCustomer(){
        int currentCustomerId = customerSharedPreference.getInt("customerID", -1);
        if(currentCustomerId != -1) {
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

    //Menu Category
    @Override
    public void onProductCategoryClick(int position){
        POS_Category category = product_categories.get(position);

        getProductCategoryFromRealm(category);

        //Filter products process [NOT YET]
        new getProductsByCategory(category.getPos_categ_id()).execute();

        Toast.makeText(contextpage, "" + category.getName(), Toast.LENGTH_SHORT).show();
    }
    //API GET Products by category
    public class getProductsByCategory extends AsyncTask<String, String, String> {

        private int category_id;
        private ArrayList<Product> product_list;

        public getProductsByCategory(int category_id){
            this.category_id = category_id;
            this.product_list = new ArrayList<>();
        }

        @Override
        protected String doInBackground(String... strings) {
            String url = "https://www.c3rewards.com/api/merchant/?module=products";
            String agent = "c092dc89b7aac085a210824fb57625db";
            String jsonUrl = url + "&agent=" + agent + "&pos_categ_id=" + category_id;
            System.out.println(jsonUrl);


            URL obj;
            try {
                obj = new URL(jsonUrl);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                // optional default is GET
                con.setRequestMethod("GET");
                //add request header
                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'GET' request to URL : " + jsonUrl);
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;

                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                System.out.println(response);
                String data = response.toString();
                try {
                    JSONObject json = new JSONObject(data);
                    String status = json.getString("status");

                    if (status.equals("OK")) {
                        JSONObject jresult = json.getJSONObject("result");
                        JSONArray jproducts = jresult.getJSONArray("products");

                        for(int i = 0; i < jproducts.length(); i++){
                            JSONObject jo = jproducts.getJSONObject(i);
                            for(int j = 0; j < list.size(); j++){
                                if(list.get(j).getProduct_id() == jo.getInt("product_id")){
                                    product_list.add(list.get(j));
                                    j = list.size();
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Log.e("error", "cannot fetch data");
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(!NetworkUtils.isNetworkAvailable(contextpage)){
                Toast.makeText(contextpage, "Required Internet Connection to filter", Toast.LENGTH_SHORT).show();
            }else{
                list.clear();
                list.addAll(product_list);
                productAdapter.notifyDataSetChanged();
            }
        }
    }

    //Menu Product
    @Override
    public void onMenuProductClick(int position) {
        showProductModifier(list.get(position), true);
        Toast.makeText(this, "" + list.get(position).getName(), Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onMenuProductLongClick(int position){
        showProductDetails(list.get(position));
    }

    @Override
    public void onResume() {
        super.onResume();

        int currentOrderId = cartSharedPreference.getInt("orderId", -1);
        Order order = realm.where(Order.class).equalTo("order_id", currentOrderId).findFirst();
        if(order != null) {
            currentOrder = realm.copyFromRealm(order);
        }
        order_lines.clear();
        getOrderLineFromRealm();

        updateOrderTotalAmount();
        refreshCartCurrentCustomer();
        refreshNote();
        refreshCustomerNumber();
    }
}