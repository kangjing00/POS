package com.findbulous.pos;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.findbulous.pos.Adapters.FloorAdapter;
import com.findbulous.pos.Adapters.TableOrderAdapter;
import com.findbulous.pos.Network.CheckConnection;
import com.findbulous.pos.databinding.CashInOutPopupBinding;
import com.findbulous.pos.databinding.TableAddonProceedPopupBinding;
import com.findbulous.pos.databinding.TableOrderSelectionPopupBinding;
import com.findbulous.pos.databinding.TablePageBinding;
import com.findbulous.pos.databinding.ToolbarSyncPopupBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class TablePage extends CheckConnection implements
                        FloorAdapter.FloorClickInterface, View.OnClickListener, View.OnLongClickListener,
                        TableOrderAdapter.TableOrderClickInterface{

    private TablePageBinding binding;
    //Body
    private boolean ordering, tableSelecting, tableTransferring;
    private Table tableSelected, longClickOccupiedTable, tableTransferFrom, tableTransferTo;
    private Order orderForTransfer, tableOrderSelected;
    private View lastClickedTableView;

    //Order Recyclerview of the table
    private ArrayList<Order> tableOrders;
    private TableOrderAdapter tableOrderAdapter;

    private ArrayList<Table> table_list;
    private ArrayList<Floor> floor_list;
    private ArrayList<Table> update_table_state;
    private FloorAdapter floorAdapter;
    //Realm
    private Realm realm;
    private SharedPreferences currentOrderSharedPreference, currentCustomerSharedPreference;
    // Creating an Editor object to edit(write to the file)
    private SharedPreferences.Editor currentOrderSharedPreferenceEdit, currentCustomerSharedPreferenceEdit;

    private String statuslogin;
    private Context contextpage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextpage = TablePage.this;

        binding = DataBindingUtil.setContentView(this, R.layout.table_page);

        realm = Realm.getDefaultInstance();

        //Toolbar Settings
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //Nav Settings
        binding.navbarLayoutInclude.navBarTables.setChecked(true);

        //Body Settings
        //CurrentOrder Settings
        currentOrderSharedPreference = getSharedPreferences("CurrentOrder",MODE_MULTI_PROCESS);
        currentOrderSharedPreferenceEdit = currentOrderSharedPreference.edit();
        //CurrentCustomer Settings
        currentCustomerSharedPreference = getSharedPreferences("CurrentCustomer",MODE_MULTI_PROCESS);
        currentCustomerSharedPreferenceEdit = currentCustomerSharedPreference.edit();

        //insertDummyFloorData();
        ordering = false;
        tableSelecting = false;
        tableTransferring = false;
        orderForTransfer = null;
        tableOrderSelected = null;
        tableSelected = null;
        tableTransferFrom = null;
        tableTransferTo = null;
        lastClickedTableView = null;
        tableOrders = new ArrayList<>();
        table_list = new ArrayList<Table>();
        update_table_state = new ArrayList<Table>();

        //Floor Recycler view
        binding.floorRv.setLayoutManager(new LinearLayoutManager(contextpage, LinearLayoutManager.HORIZONTAL, false));
        binding.floorRv.setHasFixedSize(true);
        floor_list = new ArrayList<>();
        floorAdapter = new FloorAdapter(floor_list, this);
        getFloorFromRealm();
        binding.floorRv.setAdapter(floorAdapter);

        displayTables(floor_list.get(0));

        //Order Recyclerview of the table
        tableOrders = new ArrayList<>();
        tableOrderAdapter = new TableOrderAdapter(tableOrders, this);

        if(currentOrderSharedPreference.getInt("orderingState", -1) == 1){ //ordering
            int orderId = currentOrderSharedPreference.getInt("orderId", -1);
            Order result = realm.where(Order.class).equalTo("order_id", orderId).findFirst();
            if(result.getTable() != null) {
                binding.tableInformationTableName.setText(result.getTable().getFloor().getName()
                        + " / " + result.getTable().getName());
                binding.tableInformationOrderId.setText("#" + result.getOrder_id());
            }
            ordering = true;
        }

        //OnClickListener
        //body
        {
        //popup
        AlertDialog.Builder builder = new AlertDialog.Builder(TablePage.this);

        binding.tableInformationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tableSelecting) {
                    if (tableSelected != null) {
                        if (ordering) { // ordering [only vacant table can be selected]
                            int orderId = currentOrderSharedPreference.getInt("orderId", -1);
                            Order result = realm.where(Order.class).equalTo("order_id", orderId).findFirst();
                            Order currentOrder = realm.copyFromRealm(result);

                            if (currentOrder.getTable() != null) {
                                AlertDialog alert = builder.setMessage("Order Transfer from " + currentOrder.getTable().getName() +
                                    " to " + tableSelected.getName() + "?")
                                    .setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            RealmResults<Order> results = realm.where(Order.class)
                                                    .equalTo("table.table_id", currentOrder.getTable().getTable_id())
                                                    .and().notEqualTo("state", "paid").and()
                                                    .notEqualTo("order_id", currentOrder.getOrder_id()).findAll();
                                            if(results.size() == 0)
                                                tableOccupiedToVacant(currentOrder.getTable());
                                            currentOrder.setTable(tableSelected);
                                            //update table status
                                            updateTableOccupied(tableSelected);
                                            realm.executeTransaction(new Realm.Transaction() {
                                                @Override
                                                public void execute(Realm realm) {
                                                    realm.insertOrUpdate(currentOrder);
                                                }
                                            });
                                            Intent intent = new Intent(contextpage, HomePage.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }).setNegativeButton("No", null).create();
                                alert.show();
                            } else {
                                currentOrder.setTable(tableSelected);
                                //update table status
                                updateTableOccupied(tableSelected);
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        realm.insertOrUpdate(currentOrder);
                                    }
                                });
                                Intent intent = new Intent(contextpage, HomePage.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            //select & place order
                            //add new order, update current orderingState and orderType
                            addNewOrder(tableSelected);
                            currentOrderSharedPreferenceEdit.putInt("orderingState", 1);    //ordering
                            currentOrderSharedPreferenceEdit.putInt("orderTypePosition", 1); //dine-in
                            currentOrderSharedPreferenceEdit.commit();
                            //update table status
                            updateTableOccupied(tableSelected);
                            Intent intent = new Intent(contextpage, HomePage.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                    Toast.makeText(contextpage, "Select & Place Order", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(contextpage, "Please select a table", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.tableInformationCancelTransferBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tableTransferring = false;
                tableOrderSelected = null;
                turnOffTransferCancelBtn();
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
        //Navigation bar buttons
        {
        binding.navbarLayoutInclude.navBarHome.setOnClickListener(new View.OnClickListener(){
                  @Override
                  public void onClick(View view) {
                      Intent intent = new Intent(contextpage, HomePage.class);
                      startActivity(intent);
                      finish();
//                      Toast.makeText(contextpage, "Home Button Clicked", Toast.LENGTH_SHORT).show();
                  }
              }
        );
        binding.navbarLayoutInclude.navBarCustomers.setOnClickListener(new View.OnClickListener(){
                   @Override
                   public void onClick(View view) {
                       Intent intent = new Intent(contextpage, CustomerPage.class);
                       startActivity(intent);
                       finish();
//                       Toast.makeText(contextpage, "Customers Button Clicked", Toast.LENGTH_SHORT).show();
                   }
               }
        );
        binding.navbarLayoutInclude.navBarTables.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
//                    Toast.makeText(contextpage, "Tables Button Clicked", Toast.LENGTH_SHORT).show();
                }
            }
        );
        binding.navbarLayoutInclude.navBarCashier.setOnClickListener(new View.OnClickListener(){
                 @Override
                 public void onClick(View view) {
                     Intent intent = new Intent(contextpage, CashierPage.class);
                     startActivity(intent);
                     finish();
//                     Toast.makeText(contextpage, "Cashier Button Clicked", Toast.LENGTH_SHORT).show();
                 }
             }
        );
        binding.navbarLayoutInclude.navBarOrders.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(contextpage, OrderPage.class);
                    startActivity(intent);
                    finish();
//                    Toast.makeText(contextpage, "Orders Button Clicked", Toast.LENGTH_SHORT).show();
                }
            }
        );
        binding.navbarLayoutInclude.navBarReports.setOnClickListener(new View.OnClickListener(){
                 @Override
                 public void onClick(View view) {
//                     Toast.makeText(contextpage, "Reports Button Clicked", Toast.LENGTH_SHORT).show();
                 }
             }
        );
        binding.navbarLayoutInclude.navBarSettings.setOnClickListener(new View.OnClickListener(){
                  @Override
                  public void onClick(View view) {
                      Intent intent = new Intent(contextpage, SettingPage.class);
                      startActivity(intent);
                      finish();
//                      Toast.makeText(contextpage, "Settings Button Clicked", Toast.LENGTH_SHORT).show();
                  }
              }
        );
        binding.navbarLayoutInclude.navBarProfile.setOnClickListener(new View.OnClickListener(){
                 @Override
                 public void onClick(View view) {
//                     Toast.makeText(contextpage, "Profile Button Clicked", Toast.LENGTH_SHORT).show();
                 }
             }
        );
        binding.navbarLayoutInclude.navbarLogout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
//                    Toast.makeText(contextpage, "Logout Button Clicked", Toast.LENGTH_SHORT).show();
                }
            }
        );
        }
    }

    //Update table state to online database
    public class updateTable extends AsyncTask<String, String, String> {
        boolean no_connection = false;
        String connection_error = "";

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... strings) {
            long timeBefore = Calendar.getInstance().getTimeInMillis();

            String urlParameters = "";
            for(int i =0; i < update_table_state.size(); i ++){
                urlParameters += "&states[" + update_table_state.get(i).getTable_id() + "]=" + update_table_state.get(i).getState();
            }
            byte[] postData = urlParameters.getBytes(Charset.forName("UTF-8"));
            int postDataLength = postData.length;

            String agent = "c092dc89b7aac085a210824fb57625db";
            String url = "https://www.c3rewards.com/api/merchant/?module=pos&action=restaurant_update_table_state";
            url += "&agent=" + agent;

            URL obj;
            try{
                obj = new URL(url);
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
                System.out.println("\nSending 'POST' request to URL : " + url);
                System.out.println("Post parameters : " + urlParameters);
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                System.out.println("RESPONSE UPDATE : " + response.toString());
                // {"status":"OK","message":"Success","result":[...]}
                try {
                    JSONObject json = new JSONObject(response.toString());
                    String status = json.getString("status");

                    if (status.equals("OK"))
                    {
                        update_table_state.clear();
//                        JSONObject jresult = json.getJSONObject("result");
//                        JSONArray jfloors = jresult.getJSONArray("floors");
//
//                        for (int a = 0; a < jfloors.length(); a++) {
//                            JSONObject jf = jfloors.getJSONObject(a);
//                            Floor floor = new Floor(jf.getInt("floor_id"), jf.getString("name"),
//                                    jf.getInt("sequence"), jf.getString("active"));
//
//                            JSONArray jtables = jf.getJSONArray("tables");
//                            for(int x = 0; x < jtables.length(); x++){
//                                JSONObject jt = jtables.getJSONObject(x);
//                                Table table = new Table(jt.getInt("table_id"), jt.getString("name"),
//                                        jt.getDouble("position_h"), jt.getDouble("position_v"),
//                                        jt.getDouble("width"), jt.getDouble("height"), jt.getInt("seats"),
//                                        jt.getString("active"), jt.getString("state"), floor);
//                                System.out.println("Floor: " + floor.getFloor_id() + "    Table: " + table.getTable_id() + " State: " + table.getState());
//                            }
//                        }
                        long timeAfter = Calendar.getInstance().getTimeInMillis();
                        System.out.println("Update Time: " + (timeAfter - timeBefore) + "ms");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Log.e("error", "cannot fetch data 1" + e);

                no_connection = true;
                connection_error = e.getMessage();

            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(no_connection){
                System.out.println("Connection Error Message: " + connection_error);
            }
        }
    }

    //Create new order
    private void addNewOrder(Table vacantTableSelected){
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
        order.setTable(vacantTableSelected);
        order.setCustomer_count(1);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(order);
            }
        });
        currentOrderSharedPreferenceEdit.putInt("orderId", order.getOrder_id());
        currentOrderSharedPreferenceEdit.commit();
    }
    //Update table occupied
    private void updateTableOccupied(Table table){
        table.setState("O");
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(table);
            }
        });
    }
    //Update table occupied to vacant
    private void tableOccupiedToVacant(Table table){
        table.setState("V");
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(table);
            }
        });
    }
    //OnHold and Occupied table popup
    private void showAddonAndProceed(View view, Table clickedTable) {
        PopupWindow popup = new PopupWindow(contextpage);
        TableAddonProceedPopupBinding popupBinding = TableAddonProceedPopupBinding.inflate(getLayoutInflater());
//        View layout = getLayoutInflater().inflate(R.layout.table_addon_proceed_popup, null);
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
        popup.showAsDropDown(view);


        popupBinding.tableAddonProceedPopupTableName.setText("Table " + clickedTable.getName());
        Order order = realm.where(Order.class).equalTo("table.table_id", clickedTable.getTable_id()).
                and().notEqualTo("state", "paid").findFirst();
        Customer customer = (order == null)? null : realm.where(Customer.class).equalTo("customer_id", order.getCustomer().getCustomer_id()).findFirst();
        int current_order_id = currentOrderSharedPreference.getInt("orderId", -1);

        if(order != null){
//            popupBinding.tableAddonProceedPopupOrderId.setText("#" + order.getOrder_id());
//            popupBinding.tableAddonProceedPopupOrderId.setVisibility(View.VISIBLE);
//            popupBinding.tableAddonProceedPopupTv2.setVisibility(View.VISIBLE);
            popupBinding.tableTransferBtn.setVisibility(View.VISIBLE);
        }else{
            popupBinding.tableTransferBtn.setVisibility(View.GONE);
//            popupBinding.tableAddonProceedPopupTv2.setVisibility(View.GONE);
//            popupBinding.tableAddonProceedPopupOrderId.setVisibility(View.GONE);
        }

        popupBinding.addMoreOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        popupBinding.tableTransferBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tableTransferring = true;
                turnOnTransferCancelBtn();
                tableTransferFrom = clickedTable;
                popup.dismiss();
                Toast.makeText(TablePage.this, "Choose a table for transferring", Toast.LENGTH_SHORT).show();
            }
        });
        popupBinding.tableAddonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(current_order_id == -1) { //no current order
                    if(order != null) { //the table has an order
                        currentCustomerSharedPreferenceEdit.putInt("customerID", customer.getCustomer_id());
                        currentCustomerSharedPreferenceEdit.putString("customerName", customer.getCustomer_name());
                        currentCustomerSharedPreferenceEdit.putString("customerEmail", customer.getCustomer_email());
                        currentCustomerSharedPreferenceEdit.putString("customerPhoneNo", customer.getCustomer_phoneNo());
                        currentCustomerSharedPreferenceEdit.putString("customerIdentityNo", customer.getCustomer_identityNo());
                        currentCustomerSharedPreferenceEdit.putString("customerBirthdate", customer.getCustomer_birthdate());
                        currentCustomerSharedPreferenceEdit.commit();

                        currentOrderSharedPreferenceEdit.putInt("orderingState", 1);
                        currentOrderSharedPreferenceEdit.putInt("orderId", order.getOrder_id());
                        currentOrderSharedPreferenceEdit.putInt("orderTypePosition", 1);
                        currentOrderSharedPreferenceEdit.commit();

                        Intent intent = new Intent(contextpage, HomePage.class);
                        startActivity(intent);
                        finish();
                    }else{ //the table has no order
//                        addNewOrder(clickedTable);
//                        currentOrderSharedPreferenceEdit.putInt("orderingState", 1);    //ordering
//                        currentOrderSharedPreferenceEdit.putInt("orderTypePosition", 1); //dine-in
//                        currentOrderSharedPreferenceEdit.commit();
//                        //update table status
//                        updateTableOccupied(clickedTable);
//                        Intent intent = new Intent(contextpage, HomePage.class);
//                        startActivity(intent);
//                        finish();
                    }
                }else{
                    Toast.makeText(contextpage, "Can not resume, an order is in process", Toast.LENGTH_SHORT).show();
                }
                popup.dismiss();
            }
        });
        popupBinding.tableProceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(current_order_id == -1) { //no current order
                    if(order != null) { //the table has an order
                        currentCustomerSharedPreferenceEdit.putInt("customerID", customer.getCustomer_id());
                        currentCustomerSharedPreferenceEdit.putString("customerName", customer.getCustomer_name());
                        currentCustomerSharedPreferenceEdit.putString("customerEmail", customer.getCustomer_email());
                        currentCustomerSharedPreferenceEdit.putString("customerPhoneNo", customer.getCustomer_phoneNo());
                        currentCustomerSharedPreferenceEdit.putString("customerIdentityNo", customer.getCustomer_identityNo());
                        currentCustomerSharedPreferenceEdit.putString("customerBirthdate", customer.getCustomer_birthdate());
                        currentCustomerSharedPreferenceEdit.commit();

                        currentOrderSharedPreferenceEdit.putInt("orderingState", 1);
                        currentOrderSharedPreferenceEdit.putInt("orderId", order.getOrder_id());
                        currentOrderSharedPreferenceEdit.putInt("orderTypePosition", 1);
                        currentOrderSharedPreferenceEdit.commit();

                        if (order.getOrder_lines().size() == 0) { //empty cart
                            Toast.makeText(TablePage.this, "Please proceed payment with at least 1 product", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(contextpage, HomePage.class);
                            startActivity(intent);
                        } else { //ready to payment
                            Intent intent = new Intent(contextpage, PaymentPage.class);
                            startActivity(intent);
                        }
                        finish();
                    }else{ //the table has no order

                    }
                }else{
                    Toast.makeText(contextpage, "Can not resume, an order is in process", Toast.LENGTH_SHORT).show();
                }
                popup.dismiss();
            }
        });

    }
    private void showOrderSelection(Table clickedTable){
        PopupWindow popup = new PopupWindow(contextpage);
        TableOrderSelectionPopupBinding popupBinding = TableOrderSelectionPopupBinding.inflate(getLayoutInflater());
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
        WindowManager wm = (WindowManager) TablePage.this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);

        tableOrderSelected = null;

        popupBinding.title.setText(popupBinding.title.getText().toString() + " (" + clickedTable.getFloor().getName() + " / "
                            + clickedTable.getName() + ")");

        //Recyclerview
        popupBinding.orderListRv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        popupBinding.orderListRv.setHasFixedSize(true);
        RealmResults<Order> results = realm.where(Order.class).equalTo("table.table_id", clickedTable.getTable_id()).and()
                .notEqualTo("state", "paid").findAll();
        tableOrders.clear();
        tableOrders.addAll(realm.copyFromRealm(results));
        popupBinding.orderListRv.setAdapter(tableOrderAdapter);
        tableOrderAdapter.notifyDataSetChanged();

        popupBinding.noOfOrder.setText("(" + tableOrders.size() + " Orders)");

        if(tableOrders.size() == 0){
            popupBinding.tableOrderListLl.setVisibility(View.GONE);
            popupBinding.orderListRv.setVisibility(View.GONE);
            popupBinding.tableOrderSelectionActionLl.setVisibility(View.GONE);
            popupBinding.line1.setVisibility(View.INVISIBLE);
        }

        popupBinding.addMoreOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ordering) { // ordering
                    int orderId = currentOrderSharedPreference.getInt("orderId", -1);
                    Order result = realm.where(Order.class).equalTo("order_id", orderId).findFirst();
                    Order currentOrder = realm.copyFromRealm(result);

                    currentOrder.setTable(clickedTable);
                    //update table status
                    updateTableOccupied(clickedTable);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.insertOrUpdate(currentOrder);
                        }
                    });
                }else{
                    //select & place order
                    //add new order, update current orderingState and orderType
                    addNewOrder(clickedTable);
                    currentOrderSharedPreferenceEdit.putInt("orderingState", 1);    //ordering
                    currentOrderSharedPreferenceEdit.putInt("orderTypePosition", 1); //dine-in
                    currentOrderSharedPreferenceEdit.commit();
                    //update table status
                    updateTableOccupied(clickedTable);
                }
                Intent intent = new Intent(contextpage, HomePage.class);
                startActivity(intent);
                finish();
                popup.dismiss();
            }
        });
        popupBinding.transferOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tableOrderSelected != null) {
                    tableTransferring = true;
                    turnOnTransferCancelBtn();
                    tableTransferFrom = clickedTable;
                    popup.dismiss();
                    Toast.makeText(TablePage.this, "Choose a table for transferring", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(contextpage, "Please select an order for transferring", Toast.LENGTH_SHORT).show();
                }                
            }
        });

        popupBinding.addonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tableOrderSelected != null) {
                    int current_order_id = currentOrderSharedPreference.getInt("orderId", -1);
                    if (current_order_id == -1) { //no current order
                        currentCustomerSharedPreferenceEdit.putInt("customerID", tableOrderSelected.getCustomer().getCustomer_id());
                        currentCustomerSharedPreferenceEdit.putString("customerName", tableOrderSelected.getCustomer().getCustomer_name());
                        currentCustomerSharedPreferenceEdit.putString("customerEmail", tableOrderSelected.getCustomer().getCustomer_email());
                        currentCustomerSharedPreferenceEdit.putString("customerPhoneNo", tableOrderSelected.getCustomer().getCustomer_phoneNo());
                        currentCustomerSharedPreferenceEdit.putString("customerIdentityNo", tableOrderSelected.getCustomer().getCustomer_identityNo());
                        currentCustomerSharedPreferenceEdit.putString("customerBirthdate", tableOrderSelected.getCustomer().getCustomer_birthdate());
                        currentCustomerSharedPreferenceEdit.commit();

                        currentOrderSharedPreferenceEdit.putInt("orderingState", 1);
                        currentOrderSharedPreferenceEdit.putInt("orderId", tableOrderSelected.getOrder_id());
                        currentOrderSharedPreferenceEdit.putInt("orderTypePosition", 1);
                        currentOrderSharedPreferenceEdit.commit();

                        tableOrderSelected = null;
                        popup.dismiss();
                        Intent intent = new Intent(contextpage, HomePage.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(contextpage, "Can not resume, an order is in process", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(contextpage, "Please select an order for adding products", Toast.LENGTH_SHORT).show();
                }
            }
        });
        popupBinding.proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tableOrderSelected != null) {
                    int current_order_id = currentOrderSharedPreference.getInt("orderId", -1);
                    if (current_order_id == -1) { //no current order
                        currentCustomerSharedPreferenceEdit.putInt("customerID", tableOrderSelected.getCustomer().getCustomer_id());
                        currentCustomerSharedPreferenceEdit.putString("customerName", tableOrderSelected.getCustomer().getCustomer_name());
                        currentCustomerSharedPreferenceEdit.putString("customerEmail", tableOrderSelected.getCustomer().getCustomer_email());
                        currentCustomerSharedPreferenceEdit.putString("customerPhoneNo", tableOrderSelected.getCustomer().getCustomer_phoneNo());
                        currentCustomerSharedPreferenceEdit.putString("customerIdentityNo", tableOrderSelected.getCustomer().getCustomer_identityNo());
                        currentCustomerSharedPreferenceEdit.putString("customerBirthdate", tableOrderSelected.getCustomer().getCustomer_birthdate());
                        currentCustomerSharedPreferenceEdit.commit();

                        currentOrderSharedPreferenceEdit.putInt("orderingState", 1);
                        currentOrderSharedPreferenceEdit.putInt("orderId", tableOrderSelected.getOrder_id());
                        currentOrderSharedPreferenceEdit.putInt("orderTypePosition", 1);
                        currentOrderSharedPreferenceEdit.commit();

                        RealmResults<Order_Line> order_lines = realm.where(Order_Line.class)
                                .equalTo("order.order_id", tableOrderSelected.getOrder_id()).findAll();

                        if (order_lines.size() == 0) { //empty cart
                            Toast.makeText(TablePage.this, "Please proceed payment with at least 1 product", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(contextpage, HomePage.class);
                            startActivity(intent);
                        } else { //ready to payment
                            Intent intent = new Intent(contextpage, PaymentPage.class);
                            startActivity(intent);
                        }
                        popup.dismiss();
                        finish();
                    } else {
                        Toast.makeText(contextpage, "Can not resume, an order is in process", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(contextpage, "Please select an order for proceeding to payment", Toast.LENGTH_SHORT).show();
                }
            }
        });

        popupBinding.removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
    private void turnOnTransferCancelBtn(){
        binding.tableInformationBtn.setVisibility(View.GONE);
        binding.tableInformationCancelTransferBtn.setVisibility(View.VISIBLE);
    }
    private void turnOffTransferCancelBtn(){
        binding.tableInformationBtn.setVisibility(View.VISIBLE);
        binding.tableInformationCancelTransferBtn.setVisibility(View.GONE);
    }
    @Override
    public void onTableOrderSelect(int position) {
        tableOrderSelected = tableOrders.get(position);
    }

    //Get Floor data from local realm
    private void getFloorFromRealm(){
        RealmResults<Floor> results = realm.where(Floor.class).findAll();
        floor_list.addAll(realm.copyFromRealm(results));

//        System.out.println("Before Shuffle:");
//        for(int i = 0; i < floor_list.size(); i++){
//            System.out.println(floor_list.get(i).getSequence());
//        }
//        Collections.shuffle(floor_list);
//        System.out.println("After Shuffle:");
//        for(int i = 0; i < floor_list.size(); i++){
//            System.out.println(floor_list.get(i).getSequence());
//        }
//        quickSort(floor_list, 0, floor_list.size()-1);
//        System.out.println("After QuickSort:");
//        for(int i = 0; i < floor_list.size(); i++){
//            System.out.println(floor_list.get(i).getSequence());
//        }
    }
    private void quickSort(ArrayList<Floor> floors, int begin, int end) {
        if (begin < end) {
            int partitionIndex = partition(floors, begin, end);

            quickSort(floors, begin, partitionIndex-1);
            quickSort(floors, partitionIndex+1, end);
        }
    }
    private int partition(ArrayList<Floor> floors, int begin, int end) {
        int pivot = floors.get(end).getSequence();
        int i = (begin-1);

        for (int j = begin; j < end; j++) {
            if (floors.get(j).getSequence() <= pivot) {
                i++;

                Floor swapTemp = floors.get(i);
                floors.remove(i);
                floors.add(i, floors.get(j));
                floors.remove(j);
                floors.add(j, swapTemp);
            }
        }

        Floor swapTemp = floors.get(i+1);
        floors.remove(i+1);
        floors.add(i+1, floors.get(end-1));
        floors.remove(end);
        floors.add(end, swapTemp);

        return i+1;
    }

    //Display & Refresh Tables
    private void displayTables(Floor floor){
        RealmResults<Table> tables = realm.where(Table.class).equalTo("floor.id", floor.getId()).findAll();
        table_list.clear();
        table_list.addAll(realm.copyFromRealm(tables));
        binding.tableListRl.removeAllViews();

        int largest_h = 0, largest_v = 0, largestHeight = 0, largestWidth = 0;

        for(int i = 0; i < table_list.size(); i++){
            Table table = table_list.get(i);
            TextView tableTv = new TextView(contextpage);
            if(table.isActive()){
                tableTv.setText(table.getName() + "\n" + table.getSeats());
                tableTv.setWidth((int) ((table.getWidth())* getResources().getDisplayMetrics().density));
                tableTv.setHeight((int) ((table.getHeight())* getResources().getDisplayMetrics().density));
                tableTv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                tableTv.setGravity(Gravity.CENTER);
                tableTv.setTextColor(Color.BLACK);
                tableTv.setClickable(true);
                tableTv.setX((int)((table.getPosition_h())* getResources().getDisplayMetrics().density));
                tableTv.setY((int)((table.getPosition_v())* getResources().getDisplayMetrics().density));
                Drawable tvDrawable;
                tvDrawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));
                if(table.getState().equalsIgnoreCase("V")) {
                    DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.green));
                }else if(table.getState().equalsIgnoreCase("O")){
                    DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.red));
                }else{
                    //DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.blue));
                }

                if(tableSelected != null)
                    if(table.getTable_id() == tableSelected.getTable_id()) {
                        DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.darkOrange));
                        lastClickedTableView = tableTv;
                    }

                tableTv.setBackground(tvDrawable);

                tableTv.setId(table.getTable_id());
                tableTv.setOnClickListener(TablePage.this);
                tableTv.setOnLongClickListener(TablePage.this);
                binding.tableListRl.addView(tableTv);
            }
            if(table.getPosition_v() >= largest_v)
                largest_v = (int)table.getPosition_v();
            if(table.getPosition_h() >= largest_h)
                largest_h = (int)table.getPosition_h();
            if(table.getWidth() >= largestWidth)
                largestWidth = (int)table.getWidth();
            if(table.getHeight() >= largestHeight)
                largestHeight = (int)table.getHeight();
        }
        int minimumHeight = largestHeight + largest_v;
        int minimumWidth = largestWidth + largest_h;
        binding.tableListRl.setMinimumHeight((int)((minimumHeight + 100)* getResources().getDisplayMetrics().density));
        binding.tableListRl.setMinimumWidth((int)((minimumWidth + 100)* getResources().getDisplayMetrics().density));
    }
    //Floor clicked, filter the table_list
    @Override
    public void onFloorClick(int position) {
        Floor floor = floor_list.get(position);
        displayTables(floor);
    }
    //Table click
    @Override
    public void onClick(View v) {
        Table clickedTable = null;
        for(int i = 0; i < table_list.size(); i++){
            if(table_list.get(i).getTable_id() == v.getId()){
                clickedTable = table_list.get(i);
                break;
            }
        }
        Floor floor = realm.where(Floor.class).equalTo("id", clickedTable.getFloor().getId()).findFirst();
        TextView tv = (TextView) v.findViewById(v.getId());
        Drawable tvDrawable;
        tvDrawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));

        if(!tableTransferring) {
            if(!ordering){  //Currently not ordering
                if(clickedTable.getState().equalsIgnoreCase("V")){
                    if(tableSelected != null){
                        tableSelecting = true;
                    }

                    if (!tableSelecting) {
                        //case: table is not selecting, operation: select a table
                        DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.darkOrange));
                        v.setBackground(tvDrawable);
                        lastClickedTableView = v;
                        tableSelecting = true;
                        tableSelected = clickedTable;
                    } else if (lastClickedTableView == v) {
                        //case: table is selecting and click on the selecting table, operation: make it unselect on same table
                        Drawable tvDrawable1;
                        tvDrawable1 = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));
                        DrawableCompat.setTint(tvDrawable1, getResources().getColor(R.color.green));
                        lastClickedTableView.setBackground(tvDrawable1);
                        tableSelecting = false;
                        tableSelected = null;
                    } else { // operation on the same floor and the same floor while navigate from other floor.
                        if(tableSelected.getTable_id() == clickedTable.getTable_id()){
                            //case: table is selecting and click on the selecting table, operation: make it unselect on same table
                            DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.green));
                            v.setBackground(tvDrawable);
                            tableSelecting = false;
                            tableSelected = null;
                            lastClickedTableView = null;
                        }else {
                            //case: selecting one table, and click to select another table, operation: unselect on last table & select table
                            DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.darkOrange));
                            v.setBackground(tvDrawable);
                            Drawable tvDrawable1;
                            tvDrawable1 = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));
                            DrawableCompat.setTint(tvDrawable1, getResources().getColor(R.color.green));
                            lastClickedTableView.setBackground(tvDrawable1);
                            lastClickedTableView = v;
                            tableSelecting = true;
                            tableSelected = clickedTable;
                        }
                    }
                }else if(clickedTable.getState().equalsIgnoreCase("O")){
                    showOrderSelection(clickedTable);
                }else{//obsolete "table onHold"
                    //showAddonAndProceed(v, clickedTable);
                }
            }else{      // Ordering rn
                if(tableSelected != null){
                    tableSelecting = true;
                }

                int currentOrderId = currentOrderSharedPreference.getInt("orderId", -1);
                Order order = realm.where(Order.class).equalTo("order_id", currentOrderId).findFirst();
                Order currentOrder = null;
                if(order != null) {
                    currentOrder = realm.copyFromRealm(order);
                }
                if(currentOrder != null){
                    if(currentOrder.getTable() != null) {
                        if (clickedTable.getTable_id() != currentOrder.getTable().getTable_id()) {
                            if (!tableSelecting) {
                                //case: table is not selecting, operation: select a table
                                DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.darkOrange));
                                v.setBackground(tvDrawable);
                                lastClickedTableView = v;
                                tableSelecting = true;
                                tableSelected = clickedTable;
                            } else if (lastClickedTableView == v) {
                                //case: table is selecting and click on the selecting table, operation: make it unselect on same table
                                Drawable tvDrawable1;
                                tvDrawable1 = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));
                                if (tableSelected.getState().equalsIgnoreCase("V"))
                                    DrawableCompat.setTint(tvDrawable1, getResources().getColor(R.color.green));
                                else if (tableSelected.getState().equalsIgnoreCase("O"))
                                    DrawableCompat.setTint(tvDrawable1, getResources().getColor(R.color.red));
                                else
                                    DrawableCompat.setTint(tvDrawable1, getResources().getColor(R.color.red));

                                lastClickedTableView.setBackground(tvDrawable1);
                                tableSelecting = false;
                                tableSelected = null;
                            } else { // operation on the same floor and the same floor while navigate from other floor.
                                if (tableSelected.getTable_id() == clickedTable.getTable_id()) {
                                    //case: table is selecting and click on the selecting table, operation: make it unselect on same table
                                    if (tableSelected.getState().equalsIgnoreCase("V"))
                                        DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.green));
                                    else if (tableSelected.getState().equalsIgnoreCase("O"))
                                        DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.red));
                                    else
                                        DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.red));

                                    v.setBackground(tvDrawable);
                                    tableSelecting = false;
                                    tableSelected = null;
                                    lastClickedTableView = null;
                                } else {
                                    //case: selecting one table, and click to select another table, operation: unselect on last table & select table
                                    DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.darkOrange));
                                    v.setBackground(tvDrawable);
                                    Drawable tvDrawable1;
                                    tvDrawable1 = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));
                                    if (tableSelected.getState().equalsIgnoreCase("V"))
                                        DrawableCompat.setTint(tvDrawable1, getResources().getColor(R.color.green));
                                    else if (tableSelected.getState().equalsIgnoreCase("O"))
                                        DrawableCompat.setTint(tvDrawable1, getResources().getColor(R.color.red));
                                    else
                                        DrawableCompat.setTint(tvDrawable1, getResources().getColor(R.color.red));

                                    lastClickedTableView.setBackground(tvDrawable1);
                                    lastClickedTableView = v;
                                    tableSelecting = true;
                                    tableSelected = clickedTable;
                                }
                            }
                        } else {
                            Toast.makeText(contextpage, "It is same table, select other table if you want to transfer order to other table", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        if(clickedTable.getState().equalsIgnoreCase("V")){
                            if(tableSelected != null){
                                tableSelecting = true;
                            }

                            if (!tableSelecting) {
                                //case: table is not selecting, operation: select a table
                                DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.darkOrange));
                                v.setBackground(tvDrawable);
                                lastClickedTableView = v;
                                tableSelecting = true;
                                tableSelected = clickedTable;
                            } else if (lastClickedTableView == v) {
                                //case: table is selecting and click on the selecting table, operation: make it unselect on same table
                                Drawable tvDrawable1;
                                tvDrawable1 = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));
                                DrawableCompat.setTint(tvDrawable1, getResources().getColor(R.color.green));
                                lastClickedTableView.setBackground(tvDrawable1);
                                tableSelecting = false;
                                tableSelected = null;
                            } else { // operation on the same floor and the same floor while navigate from other floor.
                                if(tableSelected.getTable_id() == clickedTable.getTable_id()){
                                    //case: table is selecting and click on the selecting table, operation: make it unselect on same table
                                    DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.green));
                                    v.setBackground(tvDrawable);
                                    tableSelecting = false;
                                    tableSelected = null;
                                    lastClickedTableView = null;
                                }else {
                                    //case: selecting one table, and click to select another table, operation: unselect on last table & select table
                                    DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.darkOrange));
                                    v.setBackground(tvDrawable);
                                    Drawable tvDrawable1;
                                    tvDrawable1 = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));
                                    DrawableCompat.setTint(tvDrawable1, getResources().getColor(R.color.green));
                                    lastClickedTableView.setBackground(tvDrawable1);
                                    lastClickedTableView = v;
                                    tableSelecting = true;
                                    tableSelected = clickedTable;
                                }
                            }
                        }else if(clickedTable.getState().equalsIgnoreCase("O")){
                            showOrderSelection(clickedTable);
                        }else{//obsolete "table onHold"
                            //showAddonAndProceed(v, clickedTable);
                        }
                    }
                }
            }
        }else{// Transfer table
            tableTransferTo = clickedTable;
            RealmResults<Order> ordersOfTableTransferFrom = realm.where(Order.class).equalTo("table.table_id", tableTransferFrom.getTable_id())
                    .and().notEqualTo("state", "paid")
                    .and().notEqualTo("order_id", tableOrderSelected.getOrder_id()).findAll();

            Order orderOfTableTransferFrom = realm.where(Order.class).equalTo("table.table_id", tableTransferFrom.getTable_id())
                    .and().notEqualTo("state", "paid").and()
                    .equalTo("order_id", tableOrderSelected.getOrder_id()).findFirst();
            orderForTransfer = realm.copyFromRealm(orderOfTableTransferFrom);

            //popup
            AlertDialog.Builder builder = new AlertDialog.Builder(TablePage.this);
            builder.setMessage("Order Transfer from " + tableTransferFrom.getName() +
                " to " + tableTransferTo.getName() + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(ordersOfTableTransferFrom.size() < 1){
                            tableTransferFrom.setState("V");
                        }
                        tableTransferTo.setState("O");
                        tableOrderSelected.setTable(tableTransferTo);

                        realm.executeTransaction(new Realm.Transaction() {
                             @Override
                             public void execute(Realm realm) {
                                 realm.insertOrUpdate(tableOrderSelected);
                                 realm.insertOrUpdate(tableTransferTo);
                                 realm.insertOrUpdate(tableTransferFrom);
                             }
                         });

                        tableOrderSelected = null;
                        tableTransferTo = null;
                        tableTransferFrom = null;
                        tableTransferring = false;
                        turnOffTransferCancelBtn();
                        //Temporarily
                        displayTables(floor);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(contextpage, "Choose a table for transferring", Toast.LENGTH_SHORT).show();
                        }
                    }).setCancelable(false);
            AlertDialog alert = builder.create();
            if(tableTransferTo.getTable_id() == tableTransferFrom.getTable_id()){
                Toast.makeText(contextpage, "Not allow to transfer to the same table.", Toast.LENGTH_SHORT).show();
            }else {
                alert.show();
            }
        }
    }
    @Override
    public boolean onLongClick(View v) {
        Table clickedTable = null;
        longClickOccupiedTable = null;
        for(int i = 0; i < table_list.size(); i++){
            if(table_list.get(i).getTable_id() == v.getId()){
                clickedTable = table_list.get(i);
                break;
            }
        }
        Floor floor = realm.where(Floor.class).equalTo("id", clickedTable.getFloor().getId()).findFirst();
        Order tableOrder = realm.where(Order.class).equalTo("table.table_id", clickedTable.getTable_id()).
                        and().notEqualTo("state", "paid").findFirst();
        String tableName = clickedTable.getName();

        if(tableOrder == null) {       //Table has no order(occupied) or vacant
            if(lastClickedTableView != null) {
                if (lastClickedTableView.getId() == clickedTable.getTable_id()) {
                    tableSelecting = false;
                    tableSelected = null;
                    lastClickedTableView = null;
                }
            }

            if(clickedTable.getState().equalsIgnoreCase("V")){  //vacant table
                clickedTable.setState("O");
                Drawable tvDrawable;
                tvDrawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));
                DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.red));
                v.setBackground(tvDrawable);
                Toast.makeText(TablePage.this, "" + tableName + " occupied successfully", Toast.LENGTH_SHORT).show();
            }else { //occupy table
                clickedTable.setState("V");
                Drawable tvDrawable1;
                tvDrawable1 = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));
                DrawableCompat.setTint(tvDrawable1, getResources().getColor(R.color.green));
                v.setBackground(tvDrawable1);
                Toast.makeText(TablePage.this, "" + tableName + " occupy removed successfully", Toast.LENGTH_SHORT).show();
            }
            longClickOccupiedTable = clickedTable;
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.insertOrUpdate(longClickOccupiedTable);
                }
            });
            update_table_state.add(clickedTable);

            //displayTables(floor);
            //API
            new updateTable().execute();
        }else{      //Table has an order
            Toast.makeText(TablePage.this, "This table has an order", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    //Toolbar
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
        WindowManager wm = (WindowManager) TablePage.this.getSystemService(Context.WINDOW_SERVICE);
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
}