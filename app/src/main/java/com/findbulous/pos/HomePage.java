package com.findbulous.pos;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
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
import com.findbulous.pos.API.SetOrderDiscount;
import com.findbulous.pos.API.SetOrderNote;
import com.findbulous.pos.API.SetOrderTable;
import com.findbulous.pos.API.UpdateOneOrderLineConfig;
import com.findbulous.pos.API.UpdateOneOrderLineQtyDisc;
import com.findbulous.pos.API.UpdateOrderCustomerCount;
import com.findbulous.pos.API.UpdateTableState;
import com.findbulous.pos.Adapters.CartOrderLineAdapter;
import com.findbulous.pos.Adapters.ProductAdapter;
import com.findbulous.pos.Adapters.ProductCategoryAdapter;
import com.findbulous.pos.Network.CheckConnection;
import com.findbulous.pos.Network.NetworkUtils;
import com.findbulous.pos.databinding.CartOrderAddDiscountPopupBinding;
import com.findbulous.pos.databinding.CartOrderAddNotePopupBinding;
import com.findbulous.pos.databinding.CashInOutPopupBinding;
import com.findbulous.pos.databinding.HomePageBinding;
import com.findbulous.pos.databinding.ProductDetailsPopupBinding;
import com.findbulous.pos.databinding.ProductModifierPopupBinding;
import com.findbulous.pos.databinding.ToolbarSyncPopupBinding;
import com.google.android.material.button.MaterialButton;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class HomePage extends CheckConnection implements ProductCategoryAdapter.ProductCategoryClickInterface,
        ProductAdapter.OnItemClickListener, CartOrderLineAdapter.OnItemClickListener{

    private HomePageBinding binding;
    //Modifier popup color btn
    private ImageButton lastClickedColorBtn;
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
    //Pos Session and Pos Config
//    private POS_Session pos_session;
    private POS_Config pos_config;
    private Currency currency;
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
    //Product attribute
    private Attribute_Value[] allAttributes;
    //private String[] allAttributes_custom;
    private EditText[] allAttributes_custom;

    //POS Type
    private ArrayAdapter<String> orderTypes;
    private List<String> posOrderType = new ArrayList<String>();
    private String takeaway_posType = "Takeaway", dine_in_posType = "Dine-in";

    private String statuslogin;
    private Context contextpage;

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
        allAttributes_custom = null;
//        allAttributes_name = null;
//        allPrice_extra = null;
        allAttributes = null;


        lastClickedColorBtn = null;
        pos_config = realm.where(POS_Config.class).findFirst();
//        pos_session = realm.where(POS_Session.class).findFirst();
        currency = realm.copyFromRealm(realm.where(Currency.class).findFirst());
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

        binding.toolbarLayoutIncl.toolbarSearchLayout.setVisibility(View.VISIBLE);
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
                   String searchValue = binding.toolbarLayoutIncl.toolbarEtSearch.getText().toString().trim();
                   RealmResults<Product> results = realm.where(Product.class).contains("name", searchValue, Case.INSENSITIVE)
                           .findAll();
                   getProductCategoryFromRealm();
                   list.clear();
                   list.addAll(results);
                   categories_clicked_wo_child.clear();
                   productAdapter.notifyDataSetChanged();
                   binding.toolbarLayoutIncl.toolbarEtSearch.onEditorAction(EditorInfo.IME_ACTION_DONE);
                   binding.toolbarLayoutIncl.toolbarEtSearch.clearFocus();
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
//                   Toast.makeText(contextpage, "Home Button Clicked", Toast.LENGTH_SHORT).show();
               }
           }
        );
        binding.navbarLayoutInclude.navBarCustomers.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view) {
                   Intent intent = new Intent(contextpage, CustomerPage.class);
                   startActivity(intent);
                   finish();
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
                if((currentOrder.getLocal_order_id() != -1) && (order_lines.size() != 0))
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
        binding.cartInclude.cartOrderSummaryHoldBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(currentOrder.getLocal_order_id() == -1){
                    Toast.makeText(contextpage, "No order to hold", Toast.LENGTH_SHORT).show();
                }
//                else if(customerSharedPreference.getInt("customerID", -1) == -1){
//                    Toast.makeText(contextpage, "Please add a customer to this order before hold", Toast.LENGTH_SHORT).show();
//                }
                else {
                    showCartOrderAddNotePopup(binding.cartInclude.cartOrderSummaryHoldBtn.getId());
                }
            }
        });
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
        //getResources().getStringArray(R.array.order_types)
        orderTypes = new ArrayAdapter<String>(contextpage, R.layout.textview_spinner_item, posOrderType){
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
        orderTypes.setDropDownViewResource(R.layout.textview_spinner_item);
        binding.cartInclude.cartBtnPosType.setAdapter(orderTypes);
        binding.cartInclude.cartBtnPosType.setDropDownVerticalOffset(65);
        binding.cartInclude.cartBtnPosType.setSelection(cartSharedPreference.getInt("orderTypePosition", 1));

        if(currentOrder.getTable() != null){
            posOrderType.remove(dine_in_posType);
            dine_in_posType = currentOrder.getTable().getFloor().getName() + " / " + currentOrder.getTable().getName();
            posOrderType.add(dine_in_posType);
            orderTypes.notifyDataSetChanged();
        }

        binding.cartInclude.cartBtnPosType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int orderTypePosition = binding.cartInclude.cartBtnPosType.getSelectedItemPosition(); //0 = takeaway, 1 = dine-in
                //if change from dine-in to takeaway \/ takeaway to dine-in
                if((orderTypePosition == 0) && (currentOrder.getLocal_order_id() != -1)
                    && (currentOrder.getTable() != null)){//takeaway && order now && order has table
                    //update table
                    RealmResults<Order> results = realm.where(Order.class)
                            .equalTo("table.table_id", currentOrder.getTable().getTable_id())
                            .and().notEqualTo("state", "paid").and()
                            .notEqualTo("local_order_id", currentOrder.getLocal_order_id()).findAll();
                    if(results.size() == 0)
                        tableOccupiedToVacant(currentOrder.getTable());

                    RealmResults<Order_Line> order_lines = realm.where(Order_Line.class)
                            .equalTo("order.local_order_id", currentOrder.getLocal_order_id())
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
                //binding.cartInclude.cartBtnPosType.getSelectedItem().toString();
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
        all_list.addAll(getParentCategory(category, category_list));
        categories_clicked_wo_child.clear();
        categories_clicked_wo_child.addAll(all_list);
        //add sub-category
        for(int i = 0; i < category.getPos_categories().size(); i++){
            all_list.add(category.getPos_categories().get(i));
        }
        return all_list;
    }
    private ArrayList<POS_Category> getParentCategory(POS_Category category, ArrayList<POS_Category> category_list){
        ArrayList<POS_Category> parent_list = new ArrayList<>();

        int counter = 0;
        while(counter < category_list.size()){
            RealmList<POS_Category> sub_category = category_list.get(counter).getPos_categories();
            POS_Category parent_category = null;
            for(int i = 0; i < sub_category.size(); i++){
                if(category.getPos_categ_id() == sub_category.get(i).getPos_categ_id()){
                    parent_category = category_list.get(counter);
                    parent_list.addAll(getParentCategory(parent_category, category_list));
                }
            }
            counter++;
        }
        parent_list.add(category);
        return parent_list;
    }

    private void getProductFromRealm(){
        list.clear();
        RealmResults<Product> results = realm.where(Product.class).findAll();
        list.addAll(realm.copyFromRealm(results));
        productAdapter.notifyDataSetChanged();
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
        WindowManager wm = (WindowManager) HomePage.this.getSystemService(Context.WINDOW_SERVICE);
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
        if(currentOrder.getLocal_order_id() != -1){
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
                if(currentOrder.getLocal_order_id() != -1) {
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
        WindowManager wm = (WindowManager) HomePage.this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);


        if(currentOrder.getLocal_order_id() != -1) {
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
        if(btnID == binding.cartInclude.cartOrderNoteBtn.getId()){
            popupBinding.addNotePopupPositiveBtn.setText("Add & Update");
        }else if(btnID == binding.cartInclude.cartOrderSummaryHoldBtn.getId()){
            popupBinding.addNotePopupPositiveBtn.setText("Proceed");
        }

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
                        if(!NetworkUtils.isNetworkAvailable(contextpage)){  //no internet
                            startActivity(intent);
                            finish();
                            Toast.makeText(contextpage, "No Internet Connection, product added into order stored in local", Toast.LENGTH_SHORT).show();
                        }else {
                            new SetOrderNote(contextpage, currentOrder.getOrder_id(), currentOrder.getLocal_order_id(),
                                    note, intent, HomePage.this).execute();
                        }
                        Toast.makeText(contextpage, "Choose a table for this order", Toast.LENGTH_SHORT).show();
                    }else { //OnHold
                        onHoldCustomer = getCurrentCustomer();
                        currentOrder.setCustomer(onHoldCustomer);
                        currentOrder.setState("onHold");
                        currentOrder.setState_name("Onhold");
                        currentOrder.setNote(note);

                        if (currentOrder.getTable() != null) {
                            Table result = realm.where(Table.class).equalTo("table_id", currentOrder.getTable().getTable_id()).findFirst();
                            updateTableOnHold = realm.copyFromRealm(result);
                            updateTableOnHold.setState("O"); //before edit is H
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
                        cartSharedPreferenceEdit.putInt("localOrderId", -1);
                        cartSharedPreferenceEdit.commit();

                        updateOrderTotalAmount();
                        refreshCartCurrentCustomer();
                        currentOrder = new Order();
                        updateTableOnHold = new Table();
                        order_lines.clear();
                        orderLineAdapter.notifyDataSetChanged();
                        refreshNote();
                        refreshCustomerNumber();
                        resetPosType();
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
                    if(!NetworkUtils.isNetworkAvailable(contextpage)){  //no internet
                        Toast.makeText(contextpage, "No Internet Connection, product added into order stored in local", Toast.LENGTH_SHORT).show();
                    }else {
                        new SetOrderNote(contextpage, currentOrder.getOrder_id(), currentOrder.getLocal_order_id(),
                                note, null, null).execute();
                    }
                    Toast.makeText(contextpage, "Added & Updated", Toast.LENGTH_SHORT).show();
                }

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
        popup.setOutsideTouchable(false);
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

        if((currentOrder.getLocal_order_id() != -1) && (currentOrder.getDiscount_type() != null)) {
            if(currentOrder.getDiscount_type().equalsIgnoreCase("percentage")){
                popupBinding.addDiscountPopupEt.setText(String.valueOf(currentOrder.getDiscount()));
                popupBinding.addDiscountPopupRadioBtnPercentage.setChecked(true);
                popupBinding.addDiscountPopupRadioBtnAmount.setChecked(false);
                popupBinding.addDiscountPopupEt.setInputType(InputType.TYPE_CLASS_NUMBER);
            }else if(currentOrder.getDiscount_type().equalsIgnoreCase("fixed_amount")){
                popupBinding.addDiscountPopupEt.setText(String.valueOf(currentOrder.getDiscount()));
                popupBinding.addDiscountPopupRadioBtnPercentage.setChecked(false);
                popupBinding.addDiscountPopupRadioBtnAmount.setChecked(true);
                popupBinding.addDiscountPopupEt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }else{
                popupBinding.addDiscountPopupRadioBtnPercentage.setChecked(true);
                popupBinding.addDiscountPopupRadioBtnAmount.setChecked(false);
                popupBinding.addDiscountPopupEt.setText("0");
                popupBinding.addDiscountPopupEt.setInputType(InputType.TYPE_CLASS_NUMBER);
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

    private void showProductDetails(Product product){
        PopupWindow popup = new PopupWindow(contextpage);
        ProductDetailsPopupBinding popupBinding = ProductDetailsPopupBinding.inflate(getLayoutInflater());
        //View layout = getLayoutInflater().inflate(R.layout.product_details_popup, null);
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
        WindowManager wm = (WindowManager) HomePage.this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);

        popupBinding.setProduct(product);

        popupBinding.productDetailsDoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.dismiss();
            }
        });
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
            WindowManager wm = (WindowManager) HomePage.this.getSystemService(Context.WINDOW_SERVICE);
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
//            allAttributes_name = new String[attributes.size()];
//            allPrice_extra = new double[attributes.size()];

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
                        TextView tv = new TextView(contextpage);
                        if(attribute_value.getPrice_extra() > 0) {
                            tv.setText("( " + attribute_value.getDisplay_price_extra() + ")");
                        }else{
                            tv.setText("");
                        }
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
//                            allPrice_extra[i] = attribute_value.getPrice_extra();
//                            allAttributes_name[i] = attribute_value.getName();
                            btn.setBackground(select(btn.getId()));
                            lastClickedColorBtn = btn;
                        }
                        int finalI = i;
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                allAttributes[finalI] = attribute_value;
//                                allPrice_extra[finalI] = attribute_value.getPrice_extra();
//                                allAttributes_name[finalI] = attribute_value.getName();
                                if(btn != lastClickedColorBtn) {
                                    btn.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), android.R.anim.fade_in));
                                    lastClickedColorBtn.setBackground(unselect(lastClickedColorBtn.getId()));
                                    btn.setBackground(select(btn.getId()));
                                }
                                lastClickedColorBtn = btn;
                            }
                        });
                        ll.addView(btn);
                        ll.addView(tv);
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

        if(order_line == null){ //from menu because it is new order_line
            if(pos_config.isProduct_configurator() && !pos_config.isIface_orderline_customer_notes()) {
                RealmResults attribute_results = realm.where(Attribute.class).equalTo("product_tmpl_id", product.getProduct_tmpl_id())
                        .findAll();
                ArrayList<Attribute> attributes = (ArrayList<Attribute>) realm.copyFromRealm(attribute_results);
                if(attributes.size() == 0){
                    boolean sameProduct = false;
//                    for(int i = 0; i < order_lines.size(); i++) {
//                        if (product.getId() == order_lines.get(i).getProduct().getProduct_id()){
//                            int qty = order_lines.get(i).getQty() + 1;
//                            double price_before_discount = qty * order_lines.get(i).getPrice_unit();
//                            double amount_discount = 0;
//                            if(order_lines.get(i).getDiscount_type() != null) {
//                                if (order_lines.get(i).getDiscount_type().equalsIgnoreCase("percentage")) {
//                                    amount_discount = (price_before_discount * order_lines.get(i).getDiscount()) / 100;
//                                } else if (order_lines.get(i).getDiscount_type().equalsIgnoreCase("fixed_amount")) {
//                                    amount_discount = order_lines.get(i).getDiscount();
//                                }
//                            }
//                            double subtotal = price_before_discount - amount_discount;
//
//                            order_lines.get(i).setPrice_subtotal(subtotal);
//                            order_lines.get(i).setQty(qty);
//                            order_lines.get(i).setPrice_before_discount(price_before_discount);
//
//                            sameProduct = true;
//                            orderLineAdapter.notifyDataSetChanged();
//                            updateOrderTotalAmount();
//                        }
//                    }
                    if(!sameProduct)    //not same product
                        addProductToOrder(product, null, 0.0, null, null, null);
                    popup.dismiss();
                }
            }
            popupBinding.productModifierPopupPositiveBtn.setText("Add to Cart");
        }else{
            if(pos_config.isProduct_configurator() && !pos_config.isIface_orderline_customer_notes()) {
                RealmResults attribute_results = realm.where(Attribute.class).equalTo("product_tmpl_id", product.getProduct_tmpl_id())
                        .findAll();
                ArrayList<Attribute> attributes = (ArrayList<Attribute>) realm.copyFromRealm(attribute_results);
                if(attributes.size() == 0){
                    popup.dismiss();
                }
            }
            popupBinding.productModifierPopupPositiveBtn.setText("Update");
        }

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
//                    for(int i = 0; i < allPrice_extra.length; i++) {
//                        price_extra += allPrice_extra[i];
//                    }

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
                    if(order_line == null) {
                        addProductToOrder(product, product_attributesName, price_extra, customer_note,
                                allAttributes, allAttributes_custom);
                    }else{
                        updateOrderLineConfig(order_line, product, product_attributesName,
                                              price_extra, customer_note,
                                              allAttributes, allAttributes_custom);
                    }
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
    //Add & Update product to order
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
    private void addProductToOrder(Product product, String attributesName, double price_extra, String customer_note,
                                   Attribute_Value[] allAttributes, EditText[] allAttributes_custom){

        Number id = realm.where(Order_Line.class).max("local_order_line_id");
        int nextID = -1;
        System.out.println(id);
        if(id == null){
            nextID = 1;
        }else{
            nextID = id.intValue() + 1;
        }

        ArrayList<Product_Tax> product_taxes = new ArrayList<>();
        RealmResults product_taxes_results = realm.where(Product_Tax.class)
                .equalTo("product_tmpl_id", product.getProduct_tmpl_id()).findAll();
        product_taxes.addAll(realm.copyFromRealm(product_taxes_results));
        double price_unit = product.getList_price() + price_extra;
        double price_unit_excl_tax = calculate_price_unit_excl_tax(product, price_unit);
        double price_subtotal = price_unit_excl_tax * 1;
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


        int orderState = cartSharedPreference.getInt("orderingState", 0);
        if(orderState == 0) {
            cartSharedPreferenceEdit.putInt("orderingState", 1);
            cartSharedPreferenceEdit.commit();
            addNewDraftOrderWithOneProduct(product.getProduct_id(), customer_note, allAttributes, allAttributes_custom);
        }

        Order_Line newOrderLine = new Order_Line(nextID, -1, String.valueOf(order_lines.size()), 1, price_unit,
                price_subtotal, price_subtotal_incl, price_unit_excl_tax, currencyDisplayFormat(price_unit),
                currencyDisplayFormat(price_subtotal), currencyDisplayFormat(price_subtotal_incl),
                currencyDisplayFormat(price_unit_excl_tax), product_name, customer_note, null, 0.0,
                null, product.getStandard_price(), currencyDisplayFormat(product.getStandard_price()),
                price_extra, currencyDisplayFormat(price_extra), currentOrder, product, attributeValues);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(newOrderLine);
            }
        });
        order_lines.add(newOrderLine);
        orderLineAdapter.notifyDataSetChanged();
        updateOrderTotalAmount();

        if(!NetworkUtils.isNetworkAvailable(contextpage)){  //no internet
            Toast.makeText(contextpage, "No Internet Connection, product added into order stored in local", Toast.LENGTH_SHORT).show();
        }else{
            //call add product to order api
            if(orderState != 0) {
                new apiAddProductToOrder(currentOrder.getLocal_order_id(), currentOrder.getOrder_id(),
                        product.getProduct_id(), customer_note, allAttributes, allAttributes_custom).execute();
            }
        }
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

    //Add product to order api // Ordering=1 / true
    public class apiAddProductToOrder extends AsyncTask<String, String, String> {
        ProgressDialog pd = null;
        int local_order_id, order_id, product_id;
        String customer_note;
        EditText[] allAttributes_custom;
        Attribute_Value[] allAttributes;

        Order update_order;
        Table table;
        Customer customer;
        ArrayList<Order_Line> update_order_lines;

        public apiAddProductToOrder(int local_order_id, int order_id, int product_id, String customer_note,
                                    Attribute_Value[] allAttributes, EditText[] allAttributes_custom){
            this.local_order_id = local_order_id;
            this.order_id = order_id;
            this.product_id = product_id;
            this.customer_note = customer_note;
            this.allAttributes = allAttributes;
            this.allAttributes_custom = allAttributes_custom;
        }

        @Override
        protected void onPreExecute() {
            if(pd == null) {
                pd = createProgressDialog(contextpage);
                pd.show();
            }

            Order order = realm.where(Order.class).equalTo("local_order_id", local_order_id).findFirst();
            if(order.getTable() != null) {
                table = realm.copyFromRealm(order.getTable());
            }else{
                table = null;
            }
            if(order.getCustomer() != null) {
                customer = realm.copyFromRealm(order.getCustomer());
            }else{
                customer = null;
            }
            update_order = null;
            RealmResults<Order_Line> order_line_results;
            order_line_results = realm.where(Order_Line.class).equalTo("order.local_order_id", local_order_id).findAll();
            update_order_lines = new ArrayList<>();
            update_order_lines.addAll(realm.copyFromRealm(order_line_results));
        }

        @Override
        protected String doInBackground(String... strings) {
            long timeBefore = Calendar.getInstance().getTimeInMillis();
            String connection_error = "";

            String urlParameters = "&order_id=" + order_id +
                    "&products[0][product_id]=" + product_id + "&products[0][qty]=1";
            if(customer_note != null){
                urlParameters += "&customer_note=" + customer_note;
            }
            if(allAttributes != null){
                for(int i = 0; i < allAttributes.length; i++){
                    int product_template_attribute_value = allAttributes[i].getProduct_template_attribute_value_id();
                    if(allAttributes_custom[i] != null){
                        urlParameters += "&products[0][product_template_attribute_line_ids][]="
                                + product_template_attribute_value
                                + "&products[0][attribute_custom_values][" + product_template_attribute_value + "]="
                                + allAttributes_custom[i].getText().toString().trim();
                    }else {
                        urlParameters += "&products[0][product_template_attribute_line_ids][]="
                                + product_template_attribute_value;
                    }
                }
            }

            //Testing (check error)
            urlParameters += "&dev=1";

            byte[] postData = urlParameters.getBytes(Charset.forName("UTF-8"));
            int postDataLength = postData.length;

            String url = "https://www.c3rewards.com/api/merchant/?module=pos&action=save_order";
            String agent = "c092dc89b7aac085a210824fb57625db";
            String jsonUrl =url + "&agent=" + agent;
            System.out.println(jsonUrl);

            URL obj;
            try{
                obj = new URL(jsonUrl);
                HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setRequestProperty("charset", "utf-8");
                con.setRequestProperty("Content-Length", Integer.toString(postDataLength));

                // Send post request
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'POST' request to URL : " + jsonUrl);
                System.out.println("Post parameters : " + urlParameters);
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonElement je = JsonParser.parseString(String.valueOf(response));
                String prettyJsonString = gson.toJson(je);
                System.out.println(prettyJsonString);
                System.out.println("Post parameters : " + urlParameters);
                System.out.println(response);
                String data = response.toString();
                try{
                    JSONObject json = new JSONObject(data);
                    String status = json.getString("status");


                    if (status.equals("OK")) {
                        JSONObject jresult = json.getJSONObject("result");
                        JSONObject jo_order = jresult.getJSONObject("order");
                        JSONArray ja_order_line = jo_order.getJSONArray("order_lines");

                        //order
                        double tip_amount = 0;
                        boolean is_tipped = false;
                        int partner_id = 0;
                        String discount_type = null;
                        if(jo_order.getString("tip_amount").length() > 0){
                            tip_amount = jo_order.getDouble("tip_amount");
                        }
                        if(jo_order.getString("is_tipped").length() > 0){
                            is_tipped = jo_order.getBoolean("is_tipped");
                        }
                        if(jo_order.getString("partner_id").length() > 0){
                            partner_id = jo_order.getInt("partner_id");
                        }
                        if(jo_order.getString("discount_type").length() > 0){
                            discount_type = jo_order.getString("discount_type");
                        }
                        update_order = new Order(local_order_id, jo_order.getInt("order_id"), jo_order.getString("name"),
                                jo_order.getString("date_order"), jo_order.getString("pos_reference"),
                                jo_order.getString("state"), jo_order.getString("state_name"),
                                jo_order.getDouble("amount_tax"), jo_order.getDouble("amount_total"),
                                jo_order.getDouble("amount_paid"), jo_order.getDouble("amount_return"),
                                jo_order.getDouble("amount_subtotal"), tip_amount,
                                jo_order.getString("display_amount_tax"), jo_order.getString("display_amount_total"),
                                jo_order.getString("display_amount_paid"), jo_order.getString("display_amount_return"),
                                jo_order.getString("display_amount_subtotal"), jo_order.getString("display_tip_amount"),
                                is_tipped, table, customer, jo_order.getString("note"),
                                jo_order.getDouble("discount"), discount_type, jo_order.getInt("customer_count"),
                                jo_order.getInt("session_id"), jo_order.getInt("user_id"),
                                jo_order.getInt("company_id"), partner_id);

                        //order_line created
                        for(int i = 0; i < ja_order_line.length(); i++){
                            JSONObject jo_order_line = ja_order_line.getJSONObject(i);

                            if(update_order_lines.get(i).getOrder_line_id() != jo_order_line.getInt("order_line_id")){
                                double price_before_discount = jo_order_line.getDouble("price_subtotal");
                                update_order_lines.get(i).setPrice_before_discount(price_before_discount);
                                update_order_lines.get(i).setDisplay_price_before_discount(currencyDisplayFormat(price_before_discount));
                            }

                            update_order_lines.get(i).setOrder_line_id(jo_order_line.getInt("order_line_id"));
                            update_order_lines.get(i).setName(jo_order_line.getString("name"));
                            update_order_lines.get(i).setPrice_unit(jo_order_line.getDouble("price_unit"));
                            update_order_lines.get(i).setPrice_subtotal(jo_order_line.getDouble("price_subtotal"));
                            update_order_lines.get(i).setPrice_subtotal_incl(jo_order_line.getDouble("price_subtotal_incl"));
                            update_order_lines.get(i).setDisplay_price_unit(jo_order_line.getString("display_price_unit"));
                            update_order_lines.get(i).setDisplay_price_subtotal(jo_order_line.getString("display_price_subtotal"));
                            update_order_lines.get(i).setDisplay_price_subtotal_incl(jo_order_line.getString("display_price_subtotal_incl"));
                            update_order_lines.get(i).setFull_product_name(jo_order_line.getString("full_product_name"));
                            update_order_lines.get(i).setCustomer_note(jo_order_line.getString("customer_note"));

                            double total_cost = 0.0;
                            if(jo_order_line.getString("total_cost").length() > 0){
                                total_cost = jo_order_line.getDouble("total_cost");
                            }
                            update_order_lines.get(i).setTotal_cost(total_cost);
                            update_order_lines.get(i).setDisplay_total_cost(jo_order_line.getString("display_total_cost"));
                            double price_extra = 0.0;
                            if(jo_order_line.getString("price_extra").length() > 0){
                                price_extra = jo_order_line.getDouble("price_extra");
                            }
                            update_order_lines.get(i).setPrice_extra(price_extra);
                            update_order_lines.get(i).setDisplay_price_extra(jo_order_line.getString("display_price_extra"));
                        }
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }catch (IOException e){
                Log.e("error", "cannot fetch data");
                connection_error = e.getMessage() + "";
                System.out.println(connection_error);
            }

            long timeAfter = Calendar.getInstance().getTimeInMillis();
            System.out.println("Add product to order time taken: " + (timeAfter - timeBefore) + "ms");
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(!NetworkUtils.isNetworkAvailable(contextpage)){
                Toast.makeText(contextpage, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }else{
                currentOrder = update_order;
                order_lines.clear();
                order_lines.addAll(update_order_lines);
                orderLineAdapter.notifyDataSetChanged();

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.insertOrUpdate(update_order_lines);
                        realm.insertOrUpdate(currentOrder);
                    }
                });
            }
            if (pd != null)
                pd.dismiss();
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

    private void addNewDraftOrderWithOneProduct(int product_id, String customer_note,
                                                Attribute_Value[] attribute_values, EditText[] allAttributes_custom){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
        Date today = new Date();
        Order order = new Order();
        Number id = realm.where(Order.class).max("local_order_id");

        int nextID = -1;
        System.out.println(id);
        if(id == null){
            nextID = 1;
        }else{
            nextID = id.intValue() + 1;
        }

        order.setLocal_order_id(nextID);
        order.setDate_order(df.format(today));
        order.setState("draft");
        order.setState_name("Ongoing");
        order.setCustomer_count(1);
        order.setCustomer(getCurrentCustomer());

        currentOrder = order;
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(order);
            }
        });
        cartSharedPreferenceEdit.putInt("localOrderId", order.getLocal_order_id());
        cartSharedPreferenceEdit.commit();

        if(!NetworkUtils.isNetworkAvailable(contextpage)){  //no internet
            Toast.makeText(contextpage, "No Internet Connection, order created stored in local", Toast.LENGTH_SHORT).show();
        }else{
            Customer customer = getCurrentCustomer();
            if(getCurrentCustomer().getCustomer_id() == 0){
                customer = realm.copyFromRealm(getCurrentCustomer());
            }
            new apiAddNewDraftOrderWithOneProduct(order.getLocal_order_id(), customer,
                    product_id, customer_note, attribute_values, allAttributes_custom).execute();
        }

        refreshCustomerNumber();
    }
    //Add New Draft Order (While user add any product from homepage menu)
    public class apiAddNewDraftOrderWithOneProduct extends AsyncTask<String, String, String> {
        ProgressDialog pd = null;
        Order createdOrder = null;
        Customer customer;
        int localOrderId;
        boolean is_table_management = false;

        int product_id;
        String customer_note;
        Attribute_Value[] allAttributes;
        EditText[] allAttributes_custom;

        public apiAddNewDraftOrderWithOneProduct(int localOrderId, Customer customer, int product_id, String customer_note,
                                                 Attribute_Value[] allAttributes, EditText[] allAttributes_custom){
            this.customer = customer;
            this.localOrderId = localOrderId;
            this.product_id = product_id;
            this.customer_note = customer_note;
            this.allAttributes = allAttributes;
            this.allAttributes_custom = allAttributes_custom;
        }

        @Override
        protected void onPreExecute() {
            if(pd == null) {
                pd = createProgressDialog(contextpage);
                pd.show();
            }
            is_table_management = pos_config.isIs_table_management();
        }

        @Override
        protected String doInBackground(String... strings) {
            long timeBefore = Calendar.getInstance().getTimeInMillis();
            String connection_error = "";

            int customer_id = customer.getCustomer_id();
            String urlParameters = "";
            if(customer_id != 0){
                urlParameters += "&customer_id=" + customer_id;
            }
            if(is_table_management) {
                urlParameters += "&customer_count=1";
            }

            //Temporary (Bug Fixing)
            urlParameters += "&dev=1";

            byte[] postData = urlParameters.getBytes(Charset.forName("UTF-8"));
            int postDataLength = postData.length;

            String url = "https://www.c3rewards.com/api/merchant/?module=pos&action=save_order";
            String agent = "c092dc89b7aac085a210824fb57625db";
            String jsonUrl =url + "&agent=" + agent;
            System.out.println(jsonUrl);

            URL obj;
            try{
                obj = new URL(jsonUrl);
                HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setRequestProperty("charset", "utf-8");
                con.setRequestProperty("Content-Length", Integer.toString(postDataLength));

                // Send post request
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'POST' request to URL : " + jsonUrl);
                System.out.println("Post parameters : " + urlParameters);
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonElement je = JsonParser.parseString(String.valueOf(response));
                String prettyJsonString = gson.toJson(je);
                System.out.println(prettyJsonString);
                System.out.println("Post parameters : " + urlParameters);
                System.out.println(response);
                String data = response.toString();
                try{
                    JSONObject json = new JSONObject(data);
                    String status = json.getString("status");


                    if (status.equals("OK")) {
                        JSONObject jresult = json.getJSONObject("result");
                        JSONObject jo_order = jresult.getJSONObject("order");
                        double tip_amount = 0;
                        boolean is_tipped = false;
                        int partner_id = 0;
                        String discount_type = null;
                        if(jo_order.getString("tip_amount").length() > 0){
                            tip_amount = jo_order.getDouble("tip_amount");
                        }
                        if(jo_order.getString("is_tipped").length() > 0){
                            is_tipped = jo_order.getBoolean("is_tipped");
                        }
                        if(jo_order.getString("partner_id").length() > 0){
                            partner_id = jo_order.getInt("partner_id");
                        }
                        if(jo_order.getString("discount_type").length() > 0){
                            discount_type = jo_order.getString("discount_type");
                        }

                        createdOrder = new Order(localOrderId, jo_order.getInt("order_id"), jo_order.getString("name"),
                                jo_order.getString("date_order"), jo_order.getString("pos_reference"),
                                jo_order.getString("state"), jo_order.getString("state_name"),
                                jo_order.getDouble("amount_tax"), jo_order.getDouble("amount_total"),
                                jo_order.getDouble("amount_paid"), jo_order.getDouble("amount_return"),
                                jo_order.getDouble("amount_subtotal"), tip_amount,
                                jo_order.getString("display_amount_tax"), jo_order.getString("display_amount_total"),
                                jo_order.getString("display_amount_paid"), jo_order.getString("display_amount_return"),
                                jo_order.getString("display_amount_subtotal"), jo_order.getString("display_tip_amount"),
                                is_tipped, null, customer, jo_order.getString("note"),
                                jo_order.getDouble("discount"), discount_type,
                                jo_order.getInt("customer_count"), jo_order.getInt("session_id"),
                                jo_order.getInt("user_id"), jo_order.getInt("company_id"), partner_id);
                    }
                }catch (JSONException e) {
                        e.printStackTrace();
                    }
            }catch (IOException e){
                    Log.e("error", "cannot fetch data");
                    connection_error = e.getMessage() + "";
                    System.out.println(connection_error);
            }

            long timeAfter = Calendar.getInstance().getTimeInMillis();
            System.out.println("Add new draft order time taken: " + (timeAfter - timeBefore) + "ms");
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(!NetworkUtils.isNetworkAvailable(contextpage)){
                Toast.makeText(contextpage, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }else{
                currentOrder = createdOrder;
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.insertOrUpdate(currentOrder);
                    }
                });
                cartSharedPreferenceEdit.putInt("localOrderId", currentOrder.getLocal_order_id());
                cartSharedPreferenceEdit.commit();


                new apiAddProductToOrder(currentOrder.getLocal_order_id(), currentOrder.getOrder_id(),
                        product_id, customer_note, allAttributes, allAttributes_custom).execute();
            }
            if (pd != null)
                pd.dismiss();
        }
    }
    //Update Order
    //public class apiUpdateOrder extends AsyncTask<String, String, String>{
    //
    //}

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

                            if(!NetworkUtils.isNetworkAvailable(contextpage)) {
                                Toast.makeText(contextpage, "No Internet Connection.", Toast.LENGTH_SHORT).show();
                            }else{
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

    private void updateOrderTotalAmount(){
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
//            binding.cartInclude.cartOrderSummaryDiscount.setText(String.format("- RM %.2f", amount_order_discount));
            binding.cartInclude.cartOrderSummaryDiscount.setText(currencyDisplayFormat(amount_order_discount));
        }else{
//            binding.cartInclude.cartOrderSummaryDiscount.setText("- RM 0.00");
            binding.cartInclude.cartOrderSummaryDiscount.setText("- " + currencyDisplayFormat(0.00));
            binding.cartInclude.cartOrderSummaryDiscountRl.setVisibility(View.GONE);
            binding.cartInclude.cartOrderDiscountBtn.setTextColor(contextpage.getResources().getColor(R.color.darkOrange));
        }
//        binding.cartInclude.cartOrderSummarySubtotal.setText(String.format("RM %.2f", order_subtotal));
//        binding.cartInclude.cartOrderSummaryTax.setText(String.format("RM %.2f", total_tax_amount));
//        binding.cartInclude.cartOrderSummaryPayableAmount.setText(String.format("RM %.2f", amount_total));

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
        if(!NetworkUtils.isNetworkAvailable(contextpage)) {
            Toast.makeText(contextpage, "No Internet Connection.", Toast.LENGTH_SHORT).show();
        }else{
            new UpdateTableState(contextpage, table).execute();
        }
    }
    private void refreshNote(){
        if(currentOrder.getLocal_order_id() != -1){
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
        if(currentOrder.getLocal_order_id() == -1){
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
            orderTypes.notifyDataSetChanged();
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

    //Menu Category
    @Override
    public void onProductCategoryClick(int position){
        POS_Category category = product_categories.get(position);

        getProductCategoryFromRealm(category);

        ArrayList<POS_Category> allSubCategories = new ArrayList<>();
        Integer allSubCategoryIds[];
        allSubCategories.addAll(getAllSubCategory(category));
        allSubCategoryIds = new Integer[allSubCategories.size()];

        for (int back = (allSubCategories.size() - 1), front = 0; back >= 0; back--, front++) {
            allSubCategoryIds[front] = allSubCategories.get(back).getPos_categ_id();
        }

        RealmResults<Product> results = realm.where(Product.class)
                .in("category.pos_categ_id", allSubCategoryIds).findAll();
        list.clear();
        list.addAll(realm.copyFromRealm(results));

        productAdapter.notifyDataSetChanged();

        Toast.makeText(contextpage, "Filter Category: " + category.getName(), Toast.LENGTH_SHORT).show();
    }
    //Filter product by category and its sub-category
    private ArrayList<POS_Category> getAllSubCategory(POS_Category category){
        ArrayList<POS_Category> sub_category_list = new ArrayList<>();
        POS_Category sub_category = null;
        int counter = 0;
        while(counter < category.getPos_categories().size()) {
            if (category.getPos_categories().size() != 0) {
                sub_category = category.getPos_categories().get(counter);

                for (int i = 0; i < sub_category.getPos_categories().size(); i++) {
                    sub_category_list.addAll(getAllSubCategory(sub_category.getPos_categories().get(i)));
                }
                sub_category_list.add(sub_category);
            }
            counter++;
        }
        sub_category_list.add(category);

        return sub_category_list;
    }

    //Menu Product
    @Override
    public void onMenuProductClick(int position) {
        Product product = list.get(position);

        if(pos_config.isProduct_configurator() || pos_config.isIface_orderline_customer_notes()) {
            showProductModifier(null, product);
        }else{
            //add the product to order line directly
            boolean sameProduct = false;
//            for(int i = 0; i < order_lines.size(); i++) {
//                if (product.getId() == order_lines.get(i).getProduct().getProduct_id()){    //cart has the same product
//                    int qty = order_lines.get(i).getQty() + 1;
//                    double price_before_discount = qty * order_lines.get(i).getPrice_unit();
//                    double amount_discount = 0;
//                    if(order_lines.get(i).getDiscount_type() != null) {
//                        if (order_lines.get(i).getDiscount_type().equalsIgnoreCase("percentage")) {
//                            amount_discount = (price_before_discount * order_lines.get(i).getDiscount()) / 100;
//                        } else if (order_lines.get(i).getDiscount_type().equalsIgnoreCase("fixed_amount")) {
//                            amount_discount = order_lines.get(i).getDiscount();
//                        }
//                    }
//
//                    double subtotal = price_before_discount - amount_discount;
//
//                    order_lines.get(i).setPrice_subtotal(subtotal);
//                    order_lines.get(i).setQty(qty);
//                    order_lines.get(i).setPrice_before_discount(price_before_discount);
//
//                    sameProduct = true;
//                    orderLineAdapter.notifyDataSetChanged();
//                    updateOrderTotalAmount();
//                }
//            }
            if(!sameProduct)
                addProductToOrder(product, null, 0.0, null, null, null);
        }
        Toast.makeText(this, "" + product.getName(), Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onMenuProductLongClick(int position){
        showProductDetails(list.get(position));
    }

    @Override
    public void onResume() {
        super.onResume();

        int currentOrderId = cartSharedPreference.getInt("localOrderId", -1);
        Order order = realm.where(Order.class).equalTo("local_order_id", currentOrderId).findFirst();
        if(order != null) {
            currentOrder = realm.copyFromRealm(order);
        }
        order_lines.clear();
        getOrderLineFromRealm();

        pos_config = realm.where(POS_Config.class).findFirst();
        currency = realm.copyFromRealm(realm.where(Currency.class).findFirst());

        updateOrderTotalAmount();
        refreshCartCurrentCustomer();
        refreshNote();
        refreshCustomerNumber();

        //set default starting pos_categ
        if(pos_config.getIface_start_categ_id() > 0){
            POS_Category category = realm.where(POS_Category.class).equalTo("pos_categ_id", pos_config.getIface_start_categ_id()).
                    findFirst();

            getProductCategoryFromRealm(category);

            ArrayList<POS_Category> allSubCategories = new ArrayList<>();
            Integer allSubCategoryIds[];
            allSubCategories.addAll(getAllSubCategory(category));
            allSubCategoryIds = new Integer[allSubCategories.size()];

            for (int back = (allSubCategories.size() - 1), front = 0; back >= 0; back--, front++) {
                allSubCategoryIds[front] = allSubCategories.get(back).getPos_categ_id();
            }

            RealmResults<Product> results = realm.where(Product.class)
                    .in("category.pos_categ_id", allSubCategoryIds).findAll();
            list.clear();
            list.addAll(realm.copyFromRealm(results));

            productAdapter.notifyDataSetChanged();
        }
        //is_table_management?
        if(!pos_config.isIs_table_management()){
            binding.navbarLayoutInclude.navBarTables.setVisibility(View.GONE);
        }
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