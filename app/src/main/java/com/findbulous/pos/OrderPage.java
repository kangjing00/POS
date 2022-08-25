package com.findbulous.pos;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
import android.widget.PopupWindow;
import android.widget.Toast;

import com.findbulous.pos.API.DeleteOneDraftOrder;
import com.findbulous.pos.Adapters.OrderOrderLineAdapter;
import com.findbulous.pos.Network.CheckConnection;
import com.findbulous.pos.Network.NetworkUtils;
import com.findbulous.pos.OrderFragments.FragmentOfflineOrder;
import com.findbulous.pos.OrderFragments.FragmentOrderHistory;
import com.findbulous.pos.OrderFragments.FragmentOrderOnGoing;
import com.findbulous.pos.OrderFragments.FragmentOrderOnHold;
import com.findbulous.pos.databinding.CashInOutPopupBinding;
import com.findbulous.pos.databinding.OrderPageBinding;
import com.findbulous.pos.databinding.ToolbarSyncPopupBinding;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

import io.realm.Realm;
import io.realm.RealmResults;

public class OrderPage extends CheckConnection {

    private OrderPageBinding binding;
    private FragmentManager fm;
    private FragmentTransaction ft;
    //Order Selected
    private Order orderSelected;
    //Recyclerview
    private OrderOrderLineAdapter orderOrderLineAdapter;
    private ArrayList<Order_Line> order_lines;
    //Realm
    private Realm realm;
    private SharedPreferences currentOrderSharedPreference, currentCustomerSharedPreference;
    // Creating an Editor object to edit(write to the file)
    private SharedPreferences.Editor currentOrderSharedPreferenceEdit, currentCustomerSharedPreferenceEdit;
    //POS config
    private POS_Config pos_config;
    private Currency currency;
    //Fragments
//    private FragmentOrderHistory fragmentOrderHistory;
//    private FragmentOrderOnHold fragmentOrderOnHold;
//    private FragmentOfflineOrder fragmentOfflineOrder;

    private String statuslogin;
    private Context contextpage;

    public OrderPage() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextpage = OrderPage.this;
        binding = DataBindingUtil.setContentView(this, R.layout.order_page);

        realm = Realm.getDefaultInstance();
        //Appbar Settings
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //Nav Settings
        binding.navbarLayoutInclude.navBarOrders.setChecked(true);

        //Body Settings
        //CurrentOrder Settings
        currentOrderSharedPreference = getSharedPreferences("CurrentOrder",MODE_MULTI_PROCESS);
        currentOrderSharedPreferenceEdit = currentOrderSharedPreference.edit();
        //CurrentCustomer Settings
        currentCustomerSharedPreference = getSharedPreferences("CurrentCustomer",MODE_MULTI_PROCESS);
        currentCustomerSharedPreferenceEdit = currentCustomerSharedPreference.edit();

        binding.toolbarLayoutIncl.addNewOrderBtn.setVisibility(View.VISIBLE);
        binding.orderHistoryRb.setChecked(true);
        orderSelected = new Order();
        //Recyclerview
        binding.orderDetailProductRv.setLayoutManager(new LinearLayoutManager(contextpage, LinearLayoutManager.VERTICAL, false));
        binding.orderDetailProductRv.setHasFixedSize(true);
        order_lines = new ArrayList<>();
        orderOrderLineAdapter = new OrderOrderLineAdapter(order_lines);
        binding.orderDetailProductRv.setAdapter(orderOrderLineAdapter);

        //Fragment Settings
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.disallowAddToBackStack();
        ft.replace(binding.orderFragmentFl.getId(), new FragmentOrderHistory()).commit();

        //OnClickListener
        //Body
        {
        //Fragment Changing Button
        binding.orderHistoryRb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentOrderHistory();
            }
        });
        binding.offlineOrderRb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentOrderOffline();
            }
        });
        binding.orderOnHoldRb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ft = fm.beginTransaction();
                ft.replace(binding.orderFragmentFl.getId(), new FragmentOrderOnHold()).commit();
                binding.orderRelativeLayout.setVisibility(View.GONE);
                visibleTipCashBalance();
                resetOrderSelected();
            }
        });
        binding.orderOnGoingRb.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                fragmentOrderOnGoing();
            }
        });

        //Action button
        binding.removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(orderSelected != null){
                    if(orderSelected.getLocal_order_id() != -1){
                        removeOrder(orderSelected);
                    }
                }else{
                    Toast.makeText(contextpage, "Please select an order for removing", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.resumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(orderSelected != null){
                    if(orderSelected.getLocal_order_id() != -1) {
                        resumeOrder(orderSelected);
                    }
                }else{
                    Toast.makeText(contextpage, "Please select an order for resuming", Toast.LENGTH_SHORT).show();
                }
            }
        });



        }
        //Toolbar buttons
        {
        binding.toolbarLayoutIncl.addNewOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentOrderSharedPreference.getInt("orderingState", -1) == 1) {
                    currentCustomerSharedPreferenceEdit.putInt("customerID", -1);
                    currentCustomerSharedPreferenceEdit.putString("customerName", null);
                    currentCustomerSharedPreferenceEdit.putString("customerEmail", null);
                    currentCustomerSharedPreferenceEdit.putString("customerPhoneNo", null);
                    currentCustomerSharedPreferenceEdit.putString("customerIdentityNo", null);
                    currentCustomerSharedPreferenceEdit.putString("customerBirthdate", null);
                    currentCustomerSharedPreferenceEdit.commit();
                }

                addNewDraftOrder();
            }
        });

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
//                   Toast.makeText(contextpage, "Logout Button Clicked", Toast.LENGTH_SHORT).show();
               }
           }
        );
        }
    }

    private void fragmentOrderHistory(){
        ft = fm.beginTransaction();
        ft.replace(binding.orderFragmentFl.getId(), new FragmentOrderHistory()).commit();
        binding.orderDetailBtnLl.setVisibility(View.VISIBLE);
        binding.orderOnGoingBtnLl.setVisibility(View.GONE);
        binding.syncOrderBtn.setVisibility(View.GONE);
        binding.orderRelativeLayout.setVisibility(View.VISIBLE);
        visibleTipCashBalance();
        resetOrderSelected();
    }
    private void fragmentOrderOffline(){
        ft = fm.beginTransaction();
        ft.replace(binding.orderFragmentFl.getId(), new FragmentOfflineOrder()).commit();
        binding.orderDetailBtnLl.setVisibility(View.VISIBLE);
        binding.orderOnGoingBtnLl.setVisibility(View.GONE);
        binding.syncOrderBtn.setVisibility(View.VISIBLE);
        binding.orderRelativeLayout.setVisibility(View.VISIBLE);
        visibleTipCashBalance();
        resetOrderSelected();
    }
    private void fragmentOrderOnGoing(){
        ft = fm.beginTransaction();
        ft.replace(binding.orderFragmentFl.getId(), new FragmentOrderOnGoing()).commit();
        binding.orderDetailBtnLl.setVisibility(View.GONE);
        binding.orderOnGoingBtnLl.setVisibility(View.VISIBLE);
        invisibleTipCashBalance();
        resetOrderSelected();
    }

    private void removeOrder(Order orderSelected){
        int local_order_id = orderSelected.getLocal_order_id();
        Order orderRemove = realm.where(Order.class).equalTo("local_order_id", local_order_id).findFirst();
        RealmResults<Order_Line> orderLineRemove = realm.where(Order_Line.class)
                                        .equalTo("order.local_order_id", local_order_id).findAll();

        if(local_order_id == currentOrderSharedPreference.getInt("localOrderId", -1)){
            currentOrderSharedPreferenceEdit.putInt("orderingState", 0);
            currentOrderSharedPreferenceEdit.putInt("localOrderId", -1);
            currentOrderSharedPreferenceEdit.commit();

            currentCustomerSharedPreferenceEdit.putInt("customerID", -1);
            currentCustomerSharedPreferenceEdit.putString("customerName", null);
            currentCustomerSharedPreferenceEdit.putString("customerEmail", null);
            currentCustomerSharedPreferenceEdit.putString("customerPhoneNo", null);
            currentCustomerSharedPreferenceEdit.putString("customerIdentityNo", null);
            currentCustomerSharedPreferenceEdit.putString("customerBirthdate", null);
            currentCustomerSharedPreferenceEdit.commit();
        }

        Table tableUpdate = null;
        if(orderRemove.getTable() != null){
            tableUpdate = realm.copyFromRealm(orderRemove.getTable());

            RealmResults<Order> results = realm.where(Order.class)
                    .equalTo("table.table_id", orderRemove.getTable().getTable_id())
                    .and().notEqualTo("state", "paid").and()
                    .notEqualTo("local_order_id", orderRemove.getLocal_order_id()).findAll();
            if(results.size() == 0)
                tableUpdate.setState("V");
        }

        if(orderSelected.getCustomer() != null) {
            if(orderSelected.getCustomer().getCustomer_id() != 0) {
                RealmResults results = realm.where(Order.class)
                        .equalTo("customer.customer_id", orderSelected.getCustomer().getCustomer_id())
                        .and().notEqualTo("local_order_id", orderSelected.getLocal_order_id()).findAll();
                if (results.size() < 1) {
                    Customer remove_Customer = realm.where(Customer.class)
                            .equalTo("customer_id", orderSelected.getCustomer().getCustomer_id())
                            .findFirst();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            remove_Customer.deleteFromRealm();
                        }
                    });
                }
            }
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

        if(!NetworkUtils.isNetworkAvailable(contextpage)){  //no internet
            Toast.makeText(contextpage, "No Internet Connection, order created stored in local", Toast.LENGTH_SHORT).show();
        }else{
            new DeleteOneDraftOrder(contextpage, orderSelected.getOrder_id()).execute();
        }

        fragmentOrderOnGoing();

        Toast.makeText(contextpage, "An order has been removed", Toast.LENGTH_SHORT).show();
    }

    private void resumeOrder(Order orderSelected){
        //set current order to the order Selected
        if(orderSelected.getCustomer() != null) {
            currentCustomerSharedPreferenceEdit.putInt("customerID", orderSelected.getCustomer().getCustomer_id());
            currentCustomerSharedPreferenceEdit.putString("customerName", orderSelected.getCustomer().getCustomer_name());
            currentCustomerSharedPreferenceEdit.putString("customerEmail", orderSelected.getCustomer().getCustomer_email());
            currentCustomerSharedPreferenceEdit.putString("customerPhoneNo", orderSelected.getCustomer().getCustomer_phoneNo());
            currentCustomerSharedPreferenceEdit.putString("customerIdentityNo", orderSelected.getCustomer().getCustomer_identityNo());
            currentCustomerSharedPreferenceEdit.putString("customerBirthdate", orderSelected.getCustomer().getCustomer_birthdate());
            currentCustomerSharedPreferenceEdit.commit();
        }

        currentOrderSharedPreferenceEdit.putInt("orderingState", 1);
        currentOrderSharedPreferenceEdit.putInt("localOrderId", orderSelected.getLocal_order_id());
        if(orderSelected.getTable() != null) {
            currentOrderSharedPreferenceEdit.putInt("orderTypePosition", 1);
        }else{
            currentOrderSharedPreferenceEdit.putInt("orderTypePosition", 0);
        }
        currentOrderSharedPreferenceEdit.commit();


        //go HomePage
        Intent intent = new Intent(contextpage, HomePage.class);
        startActivity(intent);
        finish();

    }

    private Customer getCurrentCustomer(){
        Customer customer = null;
        int current_customer_id = currentCustomerSharedPreference.getInt("customerID", -1);
        String customer_name = currentCustomerSharedPreference.getString("customerName", null);
        String customerPhoneNo = currentCustomerSharedPreference.getString("customerPhoneNo", null);
        String customerEmail = currentCustomerSharedPreference.getString("customerEmail", null);
        String customerIdentityNo = currentCustomerSharedPreference.getString("customerIdentityNo", null);
        String customerBirthdate = currentCustomerSharedPreference.getString("customerBirthdate", null);
        if(current_customer_id != -1){
            customer = new Customer(current_customer_id, customer_name, customerEmail, customerPhoneNo, customerIdentityNo, customerBirthdate);
        }else{
            customer = realm.where(Customer.class).equalTo("customer_id", 0).findFirst();
        }

        return customer;
    }
    //Create new draft order
    private void addNewDraftOrder(){
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

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(order);
            }
        });

        Intent intent = new Intent(contextpage, HomePage.class);

        if(!NetworkUtils.isNetworkAvailable(contextpage)){  //no internet
            Toast.makeText(contextpage, "No Internet Connection, order created stored in local", Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
        }else{
            Customer customer = getCurrentCustomer();
            if(getCurrentCustomer().getCustomer_id() == 0){
                customer = realm.copyFromRealm(getCurrentCustomer());
            }
            new apiAddNewDraftOrder(order.getLocal_order_id(), customer,intent).execute();
        }

        currentOrderSharedPreferenceEdit.putInt("orderingState", 1);    //ordering
        currentOrderSharedPreferenceEdit.putInt("localOrderId", order.getLocal_order_id());
        currentOrderSharedPreferenceEdit.commit();
    }
    //Add New Draft Order
    public class apiAddNewDraftOrder extends AsyncTask<String, String, String> {
        private ProgressDialog pd = null;

        private int localOrderId;
        private Order createdOrder = null;
        private Intent intent;
        private Customer customer;

        public apiAddNewDraftOrder(int localOrderId, Customer customer, Intent intent){
            this.localOrderId = localOrderId;
            this.customer = customer;
            this.intent = intent;
        }

        @Override
        protected void onPreExecute() {
            if(pd == null) {
                pd = createProgressDialog(contextpage);
                pd.show();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            long timeBefore = Calendar.getInstance().getTimeInMillis();
            String connection_error = "";

            int customer_id = customer.getCustomer_id();
            String urlParameters = "&customer_count=1";
            if(customer_id != 0){
                urlParameters += "&customer_id=" + customer_id;
            }

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
                                is_tipped, null, customer, jo_order.getString("note"), jo_order.getDouble("discount"),
                                discount_type, jo_order.getInt("customer_count"), jo_order.getInt("session_id"),
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
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.insertOrUpdate(createdOrder);
                    }
                });
                currentOrderSharedPreferenceEdit.putInt("localOrderId", createdOrder.getLocal_order_id());
                currentOrderSharedPreferenceEdit.commit();

            }
            if (pd != null)
                pd.dismiss();

            if(intent != null) {
                startActivity(intent);
                finish();
            }
        }
    }


    private void visibleTipCashBalance(){
        binding.tipTv.setVisibility(View.VISIBLE);
        binding.orderDetailTip.setVisibility(View.VISIBLE);
        binding.paidTv.setVisibility(View.VISIBLE);
        binding.orderDetailPaid.setVisibility(View.VISIBLE);
        binding.balanceTv.setVisibility(View.VISIBLE);
        binding.orderDetailBalance.setVisibility(View.VISIBLE);
    }

    private void invisibleTipCashBalance(){
        binding.tipTv.setVisibility(View.GONE);
        binding.orderDetailTip.setVisibility(View.GONE);
        binding.paidTv.setVisibility(View.GONE);
        binding.orderDetailPaid.setVisibility(View.GONE);
        binding.balanceTv.setVisibility(View.GONE);
        binding.orderDetailBalance.setVisibility(View.GONE);
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
        WindowManager wm = (WindowManager) OrderPage.this.getSystemService(Context.WINDOW_SERVICE);
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
//        View layout = getLayoutInflater().inflate(R.layout.toolbar_sync_popup, null);
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

    public class apiSearchOrder extends AsyncTask<String, String, String> {
        ProgressDialog pd = null;
        Order order = null;
        ArrayList<Product> products = new ArrayList<>();

        public apiSearchOrder(Order order){
            this.order = order;
        }

        @Override
        protected void onPreExecute(){
            if(pd == null) {
                pd = createProgressDialog(contextpage);
                pd.show();
            }
            RealmResults<Product> results = realm.where(Product.class).findAll();
            products.clear();
            if(results.size() > 0)
                products.addAll(realm.copyFromRealm(results));
            else
                System.out.println("Database Product = null");
        }

        @Override
        protected String doInBackground(String... strings) {
            String url = "https://www.c3rewards.com/api/merchant/?module=pos&action=orders";
            String agent = "c092dc89b7aac085a210824fb57625db";
            String jsonUrl = url + "&agent=" + agent + "&order_id=" + order.getOrder_id();
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
                        JSONObject jorders = jresult.getJSONObject("order");
                        JSONArray jOrderLines = jorders.getJSONArray("order_lines");

                        for(int i = 0; i < jOrderLines.length(); i++){
                            JSONObject jo = jOrderLines.getJSONObject(i);
                            double price_before_discount = jo.getDouble("price_unit") * jo.getInt("qty");
                            int product_id = jo.getInt("product_id");
                            Product product = null;
                            for(int x = 0; x < products.size(); x++){
                                if(product_id == products.get(x).getProduct_id()){
                                    product = products.get(x);
                                }
                            }
                            String discount_type = null;
                            if((jo.getString("discount_type").length() > 0) ||
                                    (jo.getString("discount_type") != null)){
                                discount_type = jo.getString("discount_type");
                            }
                            double total_cost = 0.0;
                            if(jo.getString("total_cost").length() > 0){
                                total_cost = jo.getDouble("total_cost");
                            }
                            double discount = 0;
                            if(jo.getString("discount").length() > 0){
                                discount = jo.getDouble("discount");
                            }
                            double price_extra = 0;
                            if(jo.getString("price_extra").length() > 0){
                                price_extra = jo.getDouble("price_extra");
                            }
                            Order_Line orderLine = new Order_Line((i +1),
                                jo.getInt("order_line_id"), jo.getString("name"), jo.getInt("qty"),
                                jo.getDouble("price_unit"), jo.getDouble("price_subtotal"), jo.getDouble("price_subtotal_incl"),
                                price_before_discount, jo.getString("display_price_unit"), jo.getString("display_price_subtotal"),
                                jo.getString("display_price_subtotal_incl"), currencyDisplayFormat(price_before_discount),
                                jo.getString("full_product_name"), jo.getString("customer_note"), discount_type,
                                discount, jo.getString("display_discount"),
                                total_cost, jo.getString("display_total_cost"), price_extra,
                                jo.getString("display_price_extra"), order, product, null);

                            order_lines.add(orderLine);
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
        protected void onPostExecute(String s){
            if(!NetworkUtils.isNetworkAvailable(contextpage)){
                Toast.makeText(contextpage, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }else {
                if (pd != null) {
                    pd.dismiss();
                }
                orderOrderLineAdapter.notifyDataSetChanged();
            }
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

    public void setOrderSelected(Order order){
        orderSelected = order;
        RealmResults<Order_Line> results = realm.where(Order_Line.class).equalTo("order.order_id", orderSelected.getOrder_id()).findAll();
        order_lines.clear();
        if(results.size() > 0) {
            order_lines.addAll(realm.copyFromRealm(results));
        }else{
            new apiSearchOrder(orderSelected).execute();
        }
        orderOrderLineAdapter.notifyDataSetChanged();

        double tax = orderSelected.getAmount_tax();
        double tip = orderSelected.getTip_amount();
        double amount_total = orderSelected.getAmount_total();
        double balance = orderSelected.getAmount_return();
        double amount_paid = orderSelected.getAmount_paid() + balance;
        double subtotal = 0.0, total_price_subtotal_incl = 0.0, product_discount = 0.0, amount_order_discount = 0.0;
        for(int i = 0; i < order_lines.size(); i++){
            subtotal += order_lines.get(i).getPrice_before_discount();
            total_price_subtotal_incl += order_lines.get(i).getPrice_subtotal_incl();
            if(order_lines.get(i).getDiscount_type() != null) {
                if (order_lines.get(i).getDiscount_type().equalsIgnoreCase("percentage")) {
                    double amount_discount = (order_lines.get(i).getPrice_before_discount() * order_lines.get(i).getDiscount()) / 100;
                    product_discount += amount_discount;
                } else if (order_lines.get(i).getDiscount_type().equalsIgnoreCase("fixed_amount")) {
                    product_discount += order_lines.get(i).getDiscount();
                }
            }
        }
        if(product_discount != 0.0){
            product_discount = -product_discount;
        }

        if(orderSelected.getDiscount_type() != null){
            if(orderSelected.getDiscount_type().equalsIgnoreCase("percentage")){
                amount_order_discount = -((total_price_subtotal_incl * orderSelected.getDiscount()) / 100);
            }else if(orderSelected.getDiscount_type().equalsIgnoreCase("fixed_amount")){
                amount_order_discount = -orderSelected.getDiscount();
            }
        }

        binding.orderDetailTax.setText(String.format("%.2f", tax));
        binding.orderDetailTip.setText(String.format("%.2f", tip));
        binding.orderDetailGrandTotal.setText(String.format("%.2f", amount_total));
        binding.orderDetailPaid.setText(String.format("%.2f", amount_paid));
        binding.orderDetailBalance.setText(String.format("%.2f", balance));
        binding.orderDetailSubtotal.setText(String.format("%.2f", subtotal));
        binding.orderDetailProductDiscount.setText(String.format("%.2f", product_discount));
        binding.orderDetailOrderDiscount.setText(String.format("%.2f", amount_order_discount));

        binding.orderDetailOrderId.setText("#" + orderSelected.getOrder_id());
        if(orderSelected.getTable() != null){
            binding.orderDetailType.setText("Dine-in - " + orderSelected.getTable().getFloor().getName() + " / "
                    + orderSelected.getTable().getName());
        }else {
            binding.orderDetailType.setText("Takeaway");
        }
        if(orderSelected.getCustomer() != null){
            binding.orderDetailCustomerName.setText(orderSelected.getCustomer().getCustomer_name());
        }else{
            binding.orderDetailCustomerName.setText("[Customer]");
        }
    }

    public void resetOrderSelected(){
        orderSelected = null;
        order_lines.clear();
        orderOrderLineAdapter.notifyDataSetChanged();
        binding.orderDetailTax.setText("0.00");
        binding.orderDetailTip.setText("0.00");
        binding.orderDetailGrandTotal.setText("0.00");
        binding.orderDetailPaid.setText("0.00");
        binding.orderDetailBalance.setText("0.00");
        binding.orderDetailSubtotal.setText("0.00");
        binding.orderDetailProductDiscount.setText("0.00");
        binding.orderDetailOrderDiscount.setText("0.00");

        binding.orderDetailOrderId.setText("#00000");
        binding.orderDetailType.setText("[Order Type]");
        binding.orderDetailCustomerName.setText("[Customer Name]");
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

    @Override
    public void onResume() {
        super.onResume();
        pos_config = realm.where(POS_Config.class).findFirst();
        currency = realm.copyFromRealm(realm.where(Currency.class).findFirst());
        //is_table_management?
        if(!pos_config.isIs_table_management()){
            binding.navbarLayoutInclude.navBarTables.setVisibility(View.GONE);
        }
    }
}