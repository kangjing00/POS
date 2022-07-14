package com.findbulous.pos;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.findbulous.pos.Adapters.CartOrderLineAdapter;
import com.findbulous.pos.Adapters.FloorAdapter;
import com.findbulous.pos.Adapters.TableAdapter;
import com.findbulous.pos.Network.CheckConnection;
import com.findbulous.pos.databinding.TablePageBinding;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
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
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.Timer;

import javax.net.ssl.HttpsURLConnection;

import io.realm.Realm;
import io.realm.RealmResults;

public class TablePage extends CheckConnection implements
                        FloorAdapter.FloorClickInterface, View.OnClickListener, View.OnLongClickListener {

    private TablePageBinding binding;
    //Body
    private boolean onlyVacantTable, tableSelecting, tableSwapping;
    private Table vacantTableSelected, longClickOccupiedTable, tableSwapFrom, tableSwapTo;
    private Order orderSwapTo, orderSwapFrom;
    private View lastClickedTableView;
    private ArrayList<Table> table_list;
    private ArrayList<Floor> floor_list;
    private ArrayList<Table> update_table_state;
    private FloorAdapter floorAdapter;
    //Table onHold and occupied popup
    private MaterialButton table_swap_transfer_btn, table_addon_btn, table_proceed_btn;
    //Cash in out popup
    private RadioButton cash_in_rb, cash_out_rb;
    private EditText cash_in_out_amount, cash_in_out_reason;
    private MaterialButton cash_in_out_cancel, cash_in_out_confirm;
    //Sync popup
    private TextView product_sync_btn, transactions_sync_btn;
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
        onlyVacantTable = false;
        tableSelecting = false;
        tableSwapping = false;
        orderSwapTo = null;
        orderSwapFrom = null;
        vacantTableSelected = null;
        tableSwapFrom = null;
        tableSwapTo = null;
        lastClickedTableView = null;
        floor_list = new ArrayList<Floor>();
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

        if(currentOrderSharedPreference.getInt("orderingState", -1) == 1){ //ordering
            int orderId = currentOrderSharedPreference.getInt("orderId", -1);
            Order result = realm.where(Order.class).equalTo("order_id", orderId).findFirst();
            if(result.getTable() != null) {
                binding.tableInformationTableName.setText("Table " + result.getTable().getName());
                binding.tableInformationOrderId.setText("#" + result.getOrder_id());
            }
            onlyVacantTable = true;
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
                    if (vacantTableSelected != null) {
                        if (onlyVacantTable) { //only vacant table can be selected
                            int orderId = currentOrderSharedPreference.getInt("orderId", -1);
                            Order result = realm.where(Order.class).equalTo("order_id", orderId).findFirst();
                            Order currentOrder = realm.copyFromRealm(result);

                            if (currentOrder.getTable() != null) {
                                AlertDialog alert = builder.setMessage("Change table " + currentOrder.getTable().getName() +
                                        " to " + vacantTableSelected.getName() + "?")
                                        .setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                tableOccupiedToVacant(currentOrder.getTable());
                                                currentOrder.setTable(vacantTableSelected);
                                                //update table status
                                                updateTableOccupied(vacantTableSelected);
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
                                currentOrder.setTable(vacantTableSelected);
                                //update table status
                                updateTableOccupied(vacantTableSelected);
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
                            addNewOrder(vacantTableSelected);
                            currentOrderSharedPreferenceEdit.putInt("orderingState", 1);    //ordering
                            currentOrderSharedPreferenceEdit.putInt("orderTypePosition", 1); //dine-in
                            currentOrderSharedPreferenceEdit.commit();
                            //update table status
                            updateTableOccupied(vacantTableSelected);
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
        binding.tableInformationCancelSwapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tableSwapping = false;
                turnOffSwapCancelBtn();
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
                    Toast.makeText(contextpage, "Refresh Button Clicked", Toast.LENGTH_SHORT).show();
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
            String url = "https://www.c3rewards.com/api/merchant/?module=restaurants&action=update_table_state";
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
        View layout = getLayoutInflater().inflate(R.layout.table_addon_proceed_popup, null);
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
        popup.showAsDropDown(view);

        TextView tvTableName = layout.findViewById(R.id.table_addon_proceed_popup_table_name);
        TextView tvOrderID = layout.findViewById(R.id.table_addon_proceed_popup_order_id);
        TextView orderTv = layout.findViewById(R.id.table_addon_proceed_popup_tv2);
        //Popup Buttons
        table_swap_transfer_btn = (MaterialButton)layout.findViewById(R.id.table_swap_transfer_btn);
        table_addon_btn = (MaterialButton)layout.findViewById(R.id.table_addon_btn);
        table_proceed_btn = (MaterialButton)layout.findViewById(R.id.table_proceed_btn);

        tvTableName.setText(clickedTable.getName());
        Order order = realm.where(Order.class).equalTo("table.table_id", clickedTable.getTable_id()).
                and().notEqualTo("state", "paid").findFirst();
        Customer customer = (order == null)? null : realm.where(Customer.class).equalTo("customer_id", order.getCustomer().getCustomer_id()).findFirst();
        int current_order_id = currentOrderSharedPreference.getInt("orderId", -1);

        if(order != null){
            tvOrderID.setText("#" + order.getOrder_id());
            tvOrderID.setVisibility(View.VISIBLE);
            orderTv.setVisibility(View.VISIBLE);
            table_swap_transfer_btn.setVisibility(View.VISIBLE);
        }else{
            table_swap_transfer_btn.setVisibility(View.GONE);
            orderTv.setVisibility(View.GONE);
            tvOrderID.setVisibility(View.GONE);
        }

        table_swap_transfer_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tableSwapping = true;
                turnOnSwapCancelBtn();
                tableSwapFrom = clickedTable;
                popup.dismiss();
                Toast.makeText(TablePage.this, "Choose a table for swapping or transferring", Toast.LENGTH_SHORT).show();
            }
        });
        table_addon_btn.setOnClickListener(new View.OnClickListener() {
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
        table_proceed_btn.setOnClickListener(new View.OnClickListener() {
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
    private void turnOnSwapCancelBtn(){
        binding.tableInformationBtn.setVisibility(View.GONE);
        binding.tableInformationCancelSwapBtn.setVisibility(View.VISIBLE);
    }
    private void turnOffSwapCancelBtn(){
        binding.tableInformationBtn.setVisibility(View.VISIBLE);
        binding.tableInformationCancelSwapBtn.setVisibility(View.GONE);
    }

    //Insert dummy data
//    private void insertDummyTableData(String idWord,int noRow, int noColumn, Floor floor){
//        boolean active = true;
//        Random random = new Random();
//        int randomNo = 0;
//
//        for(int i = 1; i <= (noRow * noColumn); i++){
//            randomNo = random.nextInt(9) + 2;
//            saveTableToDb(idWord + i, randomNo,true, false, false, active, floor);
//            if(i < 60) {
//                active = !active;
//            }else{
//                active = true;
//            }
//        }
//    }
//    private void insertDummyFloorData(){
//        boolean active = true;
//
//        Floor floor1 = saveFloorToDb("Ground Floor", true, 7, 18);
//        insertDummyTableData("G", 7, 18, floor1);
//        Floor floor2 = saveFloorToDb("First Floor", true, 5, 5);
//        insertDummyTableData("F", 5, 5, floor2);
//        Floor floor3 = saveFloorToDb("Second Floor", true, 10, 10);
//        insertDummyTableData("S", 10, 10, floor3);
//        Floor floor4 = saveFloorToDb("Third Floor", true, 5, 18);
//        insertDummyTableData("T", 5, 18, floor4);
//        Floor floor5 = saveFloorToDb("Fourth Floor", false, 0, 0);
//        insertDummyTableData("F", 0, 0, floor5);
//    }
    //Save table data to Realm local database
//    private void saveTableToDb(String tableName, int seats, boolean vacant, boolean onHold, boolean occupied, boolean active, Floor floor){
//        Table table = new Table();
//        Number id = realm.where(Table.class).max("table_id");
//
//        int nextID = -1;
//        System.out.println(id);
//        if(id == null){
//            nextID = 1;
//        }else{
//            nextID = id.intValue() + 1;
//        }
//
//        table.setTable_id(nextID);
//        table.setTable_name(tableName);
//        table.setSeats(seats);
//        table.setVacant(vacant);
//        table.setOnHold(onHold);
//        table.setOccupied(occupied);
//        table.setActive(active);
//        table.setFloor(floor);
//
//        realm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                realm.insertOrUpdate(table);
//            }
//        });
//    }
//    private Floor saveFloorToDb(String floorName, boolean active, int noRow, int noColumn){
//        Floor floor = new Floor();
//        Number id = realm.where(Floor.class).max("floor_id");
//
//        int nextID = -1;
//        System.out.println(id);
//        if(id == null){
//            nextID = 1;
//        }else{
//            nextID = id.intValue() + 1;
//        }
//        floor.setFloor_id(nextID);
//        floor.setFloor_name(floorName);
//        floor.setActive(active);
//        floor.setNoRow(noRow);
//        floor.setNoColumn(noColumn);
//
//        realm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                realm.insertOrUpdate(floor);
//            }
//        });
//
//        return floor;
//    }
    //Get data from Realm local database

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
        RealmResults<Table> tables = realm.where(Table.class).equalTo("floor.floor_id", floor.getFloor_id()).findAll();
        table_list.clear();
        table_list.addAll(realm.copyFromRealm(tables));
        binding.tableListRl.removeAllViews();

        int largest_h = 0, largest_v = 0, largestHeight = 0, largestWidth = 0;

        for(int i = 0; i < table_list.size(); i++){
            Table table = table_list.get(i);
            TextView tableTv = new TextView(contextpage);
            if(table.getActive().equalsIgnoreCase("t")){
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
                }else if(table.getState().equalsIgnoreCase("H")){
                    DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.blue));
                }else{
                    DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.red));
                }

                if(vacantTableSelected != null)
                    if(table.getTable_id() == vacantTableSelected.getTable_id()) {
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
        Floor floor = realm.where(Floor.class).equalTo("floor_id", clickedTable.getFloor().getFloor_id()).findFirst();
        TextView tv = (TextView) v.findViewById(v.getId());
        Drawable tvDrawable;
        tvDrawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));

        if(!tableSwapping) {
            if (clickedTable.getState().equalsIgnoreCase("V")) {

                if(vacantTableSelected != null){
                    tableSelecting = true;
                }

                if (!tableSelecting) {
                    //case: table is not selecting, operation: select a table
                    DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.darkOrange));
                    v.setBackground(tvDrawable);
                    lastClickedTableView = v;
                    tableSelecting = true;
                    vacantTableSelected = clickedTable;
                } else if (lastClickedTableView == v) {
                    //case: table is selecting and click on the selecting table, operation: make it unselect on same table
                    Drawable tvDrawable1;
                    tvDrawable1 = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));
                    DrawableCompat.setTint(tvDrawable1, getResources().getColor(R.color.green));
                    lastClickedTableView.setBackground(tvDrawable1);
                    tableSelecting = false;
                    vacantTableSelected = null;
                } else { // operation on the same floor and the same floor while navigate from other floor.
                    if(vacantTableSelected.getTable_id() == clickedTable.getTable_id()){
                        //case: table is selecting and click on the selecting table, operation: make it unselect on same table
                        DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.green));
                        v.setBackground(tvDrawable);
                        tableSelecting = false;
                        vacantTableSelected = null;
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
                        vacantTableSelected = clickedTable;
                    }

                }
            } else if (clickedTable.getState().equalsIgnoreCase("H")) {
                if (!onlyVacantTable) {//not only vacant table can be selected
                    showAddonAndProceed(v, clickedTable);
                } else {  //only vacant table can be selected
                    Toast.makeText(contextpage, "Only vacant table can be selected while ordering", Toast.LENGTH_SHORT).show();
                }
            } else { //occupied
                if (!onlyVacantTable) {//not only vacant table can be selected
                    showAddonAndProceed(v, clickedTable);
                } else {
                    Toast.makeText(contextpage, "Only vacant table can be selected while ordering", Toast.LENGTH_SHORT).show();
                }
            }
        }else{// swap or transfer table
            tableSwapTo = clickedTable;
            Order orderOfTableSwapTo = realm.where(Order.class).equalTo("table.table_id", tableSwapTo.getTable_id()).
                    and().notEqualTo("state", "paid").findFirst();
            Order orderOfTableSwapFrom = realm.where(Order.class).equalTo("table.table_id", tableSwapFrom.getTable_id()).
                    and().notEqualTo("state", "paid").findFirst();
            orderSwapFrom = realm.copyFromRealm(orderOfTableSwapFrom);
            //popup
            AlertDialog.Builder builder = new AlertDialog.Builder(TablePage.this);
            builder.setMessage("Swap / transfer table " + tableSwapFrom.getName() +
                    " to " + tableSwapTo.getName() + "?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(orderOfTableSwapTo != null){  //table for swap or transfer is has an order
                                orderSwapTo = realm.copyFromRealm(orderOfTableSwapTo);
                                if (orderSwapFrom.getState().equalsIgnoreCase("draft")) {
                                    tableSwapTo.setState("O");
                                } else if (orderSwapFrom.getState().equalsIgnoreCase("onHold")) {
                                    tableSwapTo.setState("H");
                                }

                                if(orderSwapTo.getState().equalsIgnoreCase("draft")){
                                    tableSwapFrom.setState("O");
                                }else if(orderSwapTo.getState().equalsIgnoreCase("onHold")){
                                    tableSwapFrom.setState("H");
                                }
                                orderSwapFrom.setTable(tableSwapTo);
                                orderSwapTo.setTable(tableSwapFrom);
                            }else{  //table for swap or transfer has no order
                                if(tableSwapTo.getState().equalsIgnoreCase("O")){
                                    tableSwapFrom.setState("O");
                                }else {
                                    tableSwapFrom.setState("V");
                                }

                                if (orderSwapFrom.getState().equalsIgnoreCase("draft")) {
                                    tableSwapTo.setState("O");
                                } else if (orderSwapFrom.getState().equalsIgnoreCase("onHold")) {
                                    tableSwapTo.setState("H");
                                }
                                orderSwapFrom.setTable(tableSwapTo);
                            }
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    realm.insertOrUpdate(orderSwapFrom);
                                    if(orderOfTableSwapTo != null) {
                                        realm.insertOrUpdate(orderSwapTo);
                                    }
                                    realm.insertOrUpdate(tableSwapTo);
                                    realm.insertOrUpdate(tableSwapFrom);
                                }
                            });
                            orderSwapFrom = null;
                            orderSwapTo = null;
                            tableSwapTo = null;
                            tableSwapFrom = null;
                            tableSwapping = false;
                            turnOffSwapCancelBtn();
                            //Temporarily
                            displayTables(floor);
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(contextpage, "Choose a table for swapping or transferring", Toast.LENGTH_SHORT).show();
                }
            }).setCancelable(false);

            AlertDialog alert = builder.create();
            if(tableSwapTo.getTable_id() == tableSwapFrom.getTable_id()){
                Toast.makeText(contextpage, "Not allow to swap / transfer to the same table.", Toast.LENGTH_SHORT).show();
            }else {
                alert.show();
            }
        }
        Toast.makeText(TablePage.this, "table clicked" + v.getId() + "'" + tv.getText().charAt(2) + "'", Toast.LENGTH_SHORT).show();
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
        Floor floor = realm.where(Floor.class).equalTo("floor_id", clickedTable.getFloor().getFloor_id()).findFirst();
        Order tableOrder = realm.where(Order.class).equalTo("table.table_id", clickedTable.getTable_id()).
                        and().notEqualTo("state", "paid").findFirst();
        String tableName = clickedTable.getName();

        if(tableOrder == null) {       //Table has no order(occupied) or vacant
            if(lastClickedTableView != null) {
                if (lastClickedTableView.getId() == clickedTable.getTable_id()) {
                    tableSelecting = false;
                    vacantTableSelected = null;
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
        WindowManager wm = (WindowManager) TablePage.this.getSystemService(Context.WINDOW_SERVICE);
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

    //Table clicked
//    @Override
//    public void onTableClick(int position, View v) {
//        Table clickedTable = table_list.get(position);
//
//        TextView tv = (TextView) v.findViewById(v.getId());
//        Drawable tvDrawable;
//        tvDrawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));
//
//        if(!tableSwapping) {
//            if (clickedTable.getState().equalsIgnoreCase("V")) {
//                if (!tableSelecting) {
//                    //case: table is not selecting, operation: select a table
//                    tableSelecting = true;
//                    DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.darkOrange));
//                    v.setBackground(tvDrawable);
//                    lastClickedTableView = v;
//                    vacantTableSelected = clickedTable;
//                } else if (lastClickedTableView == v) {
//                    //case: table is selecting and click on the selecting table, operation: make it unselect on same table
//                    tableSelecting = false;
//                    Drawable tvDrawable1;
//                    tvDrawable1 = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));
//                    DrawableCompat.setTint(tvDrawable1, getResources().getColor(R.color.green));
//                    lastClickedTableView.setBackground(tvDrawable1);
//                    vacantTableSelected = null;
//                } else {
//                    //case: selecting one table, and click to select another table, operation: unselect on last table & select table
//                    tableSelecting = true;
//                    DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.darkOrange));
//                    v.setBackground(tvDrawable);
//                    Drawable tvDrawable1;
//                    tvDrawable1 = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));
//                    DrawableCompat.setTint(tvDrawable1, getResources().getColor(R.color.green));
//                    lastClickedTableView.setBackground(tvDrawable1);
//                    lastClickedTableView = v;
//                    vacantTableSelected = clickedTable;
//                }
//            } else if (clickedTable.getState().equalsIgnoreCase("H")) {
//                if (!onlyVacantTable) {//not only vacant table can be selected
//                    showAddonAndProceed(v, clickedTable);
//                } else {  //only vacant table can be selected
//                    Toast.makeText(contextpage, "Only vacant table can be selected while ordering", Toast.LENGTH_SHORT).show();
//                }
//            } else { //occupied
//                if (!onlyVacantTable) {//not only vacant table can be selected
//                    showAddonAndProceed(v, clickedTable);
//                } else {
//                    Toast.makeText(contextpage, "Only vacant table can be selected while ordering", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }else{// swap or transfer table
//            tableSwapTo = clickedTable;
//            Order orderOfTableSwapTo = realm.where(Order.class).equalTo("table.table_id", tableSwapTo.getTable_id()).
//                    and().notEqualTo("state", "paid").findFirst();
//            Order orderOfTableSwapFrom = realm.where(Order.class).equalTo("table.table_id", tableSwapFrom.getTable_id()).
//                    and().notEqualTo("state", "paid").findFirst();
//            orderSwapFrom = realm.copyFromRealm(orderOfTableSwapFrom);
//            //popup
//            AlertDialog.Builder builder = new AlertDialog.Builder(TablePage.this);
//            builder.setMessage("Swap / transfer table " + tableSwapFrom.getName() +
//                    " to " + tableSwapTo.getName() + "?")
//                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            if(orderOfTableSwapTo != null){  //table for swap or transfer is has an order
//                                orderSwapTo = realm.copyFromRealm(orderOfTableSwapTo);
//
//                                if (orderSwapFrom.getState().equalsIgnoreCase("draft")) {
//                                    tableSwapTo.setState("O");
//                                } else if (orderSwapFrom.getState().equalsIgnoreCase("onHold")) {
//                                    tableSwapTo.setState("H");
//                                }
//
//                                if(orderSwapTo.getState().equalsIgnoreCase("draft")){
//                                    tableSwapFrom.setState("O");
//                                }else if(orderSwapTo.getState().equalsIgnoreCase("onHold")){
//                                    tableSwapFrom.setState("H");
//                                }
//                                orderSwapFrom.setTable(tableSwapTo);
//                                orderSwapTo.setTable(tableSwapFrom);
//                            }else{  //table for swap or transfer has no order
//                                if(tableSwapTo.getState().equalsIgnoreCase("O")){
//                                    tableSwapFrom.setState("O");
//                                }else {
//                                    tableSwapFrom.setState("V");
//                                }
//
//                                if (orderSwapFrom.getState().equalsIgnoreCase("draft")) {
//                                    tableSwapTo.setState("O");
//                                } else if (orderSwapFrom.getState().equalsIgnoreCase("onHold")) {
//                                    tableSwapTo.setState("H");
//                                }
//                                orderSwapFrom.setTable(tableSwapTo);
//                            }
//                            realm.executeTransaction(new Realm.Transaction() {
//                                @Override
//                                public void execute(Realm realm) {
//                                    realm.insertOrUpdate(orderSwapFrom);
//                                    if(orderOfTableSwapTo != null) {
//                                        realm.insertOrUpdate(orderSwapTo);
//                                    }
//                                    realm.insertOrUpdate(tableSwapTo);
//                                    realm.insertOrUpdate(tableSwapFrom);
//                                }
//                            });
//                            tableAdapter.notifyDataSetChanged();
//                            orderSwapFrom = null;
//                            orderSwapTo = null;
//                            tableSwapTo = null;
//                            tableSwapFrom = null;
//                            tableSwapping = false;
//                            turnOffSwapCancelBtn();
//                        }
//                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    Toast.makeText(contextpage, "Choose a table for swapping or transferring", Toast.LENGTH_SHORT).show();
//                }
//            }).setCancelable(false);
//            AlertDialog alert = builder.create();
//            if(tableSwapTo.getTable_id() == tableSwapFrom.getTable_id()){
//                Toast.makeText(contextpage, "Not allow to swap / transfer to the same table.", Toast.LENGTH_SHORT).show();
//            }else {
//                alert.show();
//            }
//        }
//    }
//    @Override
//    public void onTableLongClick(int position) {
//        longClickOccupiedTable = table_list.get(position);
//
//        Order tableOrder = realm.where(Order.class).equalTo("table.table_id", longClickOccupiedTable.getTable_id()).findFirst();
//        String tableName = longClickOccupiedTable.getName();
//
//        if(tableOrder == null) {       //Table has no order(occupied) or vacant
//            if(longClickOccupiedTable.getState().equalsIgnoreCase("V")){  //vacant table
//                longClickOccupiedTable.setState("O");
//                Toast.makeText(TablePage.this, "" + tableName + " occupied successfully", Toast.LENGTH_SHORT).show();
//            }else { //occupy table
//                longClickOccupiedTable.setState("V");
//                Toast.makeText(TablePage.this, "" + tableName + " occupy removed successfully", Toast.LENGTH_SHORT).show();
//            }
//
//            realm.executeTransaction(new Realm.Transaction() {
//                @Override
//                public void execute(Realm realm) {
//                    realm.insertOrUpdate(longClickOccupiedTable);
//                }
//            });
//            tableAdapter.notifyDataSetChanged();
//        }else{      //Table has an order
//            Toast.makeText(TablePage.this, "This table has an order", Toast.LENGTH_SHORT).show();
//        }
//    }
}