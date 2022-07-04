package com.findbulous.pos;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.databinding.DataBindingUtil;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.findbulous.pos.databinding.TablePageBinding;
import com.google.android.material.button.MaterialButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import io.realm.Realm;
import io.realm.RealmResults;

public class TablePage extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private TablePageBinding binding;
    //Body
    private boolean onlyVacantTable, tableSelecting, tableSwapping;
    private Table vacantTableSelected, longClickOccupiedTable, tableSwapFrom, tableSwapTo;
    private Order orderSwapTo, orderSwapFrom;
    private View lastClickedTableView;
    private ArrayList<Table> tableList;
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

        //insertDummyTableData();
        onlyVacantTable = false;
        tableSelecting = false;
        tableSwapping = false;
        orderSwapTo = null;
        orderSwapFrom = null;
        vacantTableSelected = null;
        tableSwapFrom = null;
        tableSwapTo = null;
        lastClickedTableView = null;
        tableList = new ArrayList<Table>();
        getProductFromRealm();

        displayTables(7,18, tableList);

        if(currentOrderSharedPreference.getInt("orderingState", -1) == 1){ //ordering
            int orderId = currentOrderSharedPreference.getInt("orderId", -1);
            Order result = realm.where(Order.class).equalTo("order_id", orderId).findFirst();
            if(result.getTable() != null) {
                binding.tableInformationTableName.setText("Table " + result.getTable().getTable_name());
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
                                AlertDialog alert = builder.setMessage("Change table " + currentOrder.getTable().getTable_name() +
                                        " to " + vacantTableSelected.getTable_name() + "?")
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
                    Toast.makeText(contextpage, "Logout Button Clicked", Toast.LENGTH_SHORT).show();
                }
            }
        );
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
        table.setVacant(false);
        table.setOccupied(true);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(table);
            }
        });
    }
    //Update table occupied to vacant
    private void tableOccupiedToVacant(Table table){
        table.setOccupied(false);
        table.setVacant(true);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(table);
            }
        });
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

        tvTableName.setText(clickedTable.getTable_name());
        Order order = realm.where(Order.class).equalTo("table.table_id", clickedTable.getTable_id()).findFirst();
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

    //Display tables
    private void displayTables(int noRow, int noColumn, ArrayList<Table> tableList){
        //int tableNumber = 1;
        int tableListCount = 0;

        for(int row = 1; row <= noRow; row++){
            TableRow tr = new TableRow(contextpage);
            for(int column = 1; column <= noColumn; column++){
                TextView table = new TextView(contextpage);

                table.setText(tableList.get(tableListCount).getTable_name() + "\n"
                        + tableList.get(tableListCount).getSeats());
                table.setWidth((int) (80 * getResources().getDisplayMetrics().density));
                table.setHeight((int) (80 * getResources().getDisplayMetrics().density));

                Drawable tvDrawable;
                tvDrawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));
                if(tableList.get(tableListCount).isVacant()) {
                    DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.green));
                }else if(tableList.get(tableListCount).isOnHold()){
                    DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.blue));
                }else{
                    DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.red));
                }
                table.setBackground(tvDrawable);

                table.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                table.setGravity(Gravity.CENTER);
                table.setTextColor(Color.BLACK);
                table.setClickable(true);

                if(!tableList.get(tableListCount).isActive()){
                    table.setVisibility(View.INVISIBLE);
                }
//                for(int x = 0; x < empty.length; x++){
//                    for(int y = 0; y < empty[x].length; y++){
//                        if(empty[x][0] == row)
//                            if(empty[x][1] == column)
//                                table.setVisibility(View.INVISIBLE);
//                    }
//                }

                table.setId(tableList.get(tableListCount).getTable_id());
                table.setOnClickListener(TablePage.this);
                table.setOnLongClickListener(TablePage.this);

                tr.addView(table);
//                tableNumber++;
                tableListCount++;
            }
            binding.tableManagementLayout.addView(tr);
        }
    }
    //Insert dummy data with alternatively available
    private void insertDummyTableData(){
        boolean active = true;
        Random random = new Random();
        int randomNo = 0;

        for(int i = 1; i < 127; i++){
            randomNo = random.nextInt(9) + 2;
            saveTableToDb("T" + i, randomNo,true, false, false, active);
            if(i < 60) {
                active = !active;
            }else{
                active = true;
            }
        }
    }
    //Save table data to Realm local database
    private void saveTableToDb(String tableName, int seats, boolean vacant, boolean onHold, boolean occupied, boolean active){
        Table table = new Table();
        Number id = realm.where(Table.class).max("table_id");

        int nextID = -1;
        System.out.println(id);
        if(id == null){
            nextID = 1;
        }else{
            nextID = id.intValue() + 1;
        }

        table.setTable_id(nextID);
        table.setTable_name(tableName);
        table.setSeats(seats);
        table.setVacant(vacant);
        table.setOnHold(onHold);
        table.setOccupied(occupied);
        table.setActive(active);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(table);
            }
        });
    }
    //Get table data from Realm local database
    private void getProductFromRealm() {
        RealmResults<Table> results = realm.where(Table.class).findAll();
        tableList.addAll(realm.copyFromRealm(results));
    }
    //Get table info
    private String getTableName(String string){
        String tableName = null;
        for(int i = 0; i < string.length(); i++){
            if(string.substring(i,i+1).equalsIgnoreCase("\n")){
                tableName = string.substring(0, i);
            }
        }
        return tableName;
    }
    private String getTableOrderID(String string){
        String orderID = null;
        for(int i = 0; i < string.length(); i++){
            if(string.substring(i,i+1).equalsIgnoreCase("\n")){
                orderID = string.substring(i+1);
            }
        }
        return orderID;
    }

    //Tables clicking setting
    @Override
    public void onClick(View v) {
        int clicked_id = v.getId();
        Table clickedTable = null;

        for(int i = 0; i < tableList.size(); i++){
            if(tableList.get(i).getTable_id() == v.getId()){
                clickedTable = tableList.get(i);
                break;
            }
        }

        TextView tv = (TextView) v.findViewById(v.getId());
        Drawable tvDrawable;
        tvDrawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));

        if(!tableSwapping) {
            if (clickedTable.isVacant()) {
                if (!tableSelecting) {
                    //case: table is not selecting, operation: select a table
                    tableSelecting = true;
                    DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.darkOrange));
                    v.setBackground(tvDrawable);
                    lastClickedTableView = v;
                    vacantTableSelected = clickedTable;
                } else if (lastClickedTableView == v) {
                    //case: table is selecting and click on the selecting table, operation: make it unselect on same table
                    tableSelecting = false;
                    Drawable tvDrawable1;
                    tvDrawable1 = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));
                    DrawableCompat.setTint(tvDrawable1, getResources().getColor(R.color.green));
                    lastClickedTableView.setBackground(tvDrawable1);
                    vacantTableSelected = null;
                } else {
                    //case: selecting one table, and click to select another table, operation: unselect on last table & select table
                    tableSelecting = true;
                    DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.darkOrange));
                    v.setBackground(tvDrawable);
                    Drawable tvDrawable1;
                    tvDrawable1 = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));
                    DrawableCompat.setTint(tvDrawable1, getResources().getColor(R.color.green));
                    lastClickedTableView.setBackground(tvDrawable1);
                    lastClickedTableView = v;
                    vacantTableSelected = clickedTable;
                }
            } else if (clickedTable.isOnHold()) {
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
            builder.setMessage("Swap / transfer table " + tableSwapFrom.getTable_name() +
                    " to " + tableSwapTo.getTable_name() + "?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(orderOfTableSwapTo != null){  //table for swap or transfer is has an order
                                orderSwapTo = realm.copyFromRealm(orderOfTableSwapTo);
                                tableSwapTo.setVacant(false);
                                if (orderSwapFrom.getState().equalsIgnoreCase("draft")) {
                                    tableSwapTo.setOccupied(true);
                                    tableSwapTo.setOnHold(false);
                                } else if (orderSwapFrom.getState().equalsIgnoreCase("onHold")) {
                                    tableSwapTo.setOccupied(false);
                                    tableSwapTo.setOnHold(true);
                                }
                                tableSwapFrom.setVacant(false);
                                if(orderSwapTo.getState().equalsIgnoreCase("draft")){
                                    tableSwapFrom.setOccupied(true);
                                    tableSwapFrom.setOnHold(false);
                                }else if(orderSwapTo.getState().equalsIgnoreCase("onHold")){
                                    tableSwapFrom.setOccupied(false);
                                    tableSwapFrom.setOnHold(true);
                                }
                                orderSwapFrom.setTable(tableSwapTo);
                                orderSwapTo.setTable(tableSwapFrom);
                            }else{  //table for swap or transfer has no order
                                if(tableSwapTo.isOccupied()){
                                    tableSwapFrom.setOccupied(true);
                                    tableSwapFrom.setOnHold(false);
                                    tableSwapFrom.setVacant(false);
                                }else {
                                    tableSwapFrom.setOccupied(false);
                                    tableSwapFrom.setOnHold(false);
                                    tableSwapFrom.setVacant(true);
                                }

                                tableSwapTo.setVacant(false);
                                if (orderSwapFrom.getState().equalsIgnoreCase("draft")) {
                                    tableSwapTo.setOccupied(true);
                                    tableSwapTo.setOnHold(false);
                                } else if (orderSwapFrom.getState().equalsIgnoreCase("onHold")) {
                                    tableSwapTo.setOccupied(false);
                                    tableSwapTo.setOnHold(true);
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
                            Intent intent = new Intent(contextpage, TablePage.class);
                            startActivity(intent);
                            finish();
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
        for(int i = 0; i < tableList.size(); i++){
            if(tableList.get(i).getTable_id() == v.getId()){
                clickedTable = tableList.get(i);
                break;
            }
        }
        Order tableOrder = realm.where(Order.class).equalTo("table.table_id", clickedTable.getTable_id()).findFirst();
        TextView tv = (TextView) v.findViewById(v.getId());
        String tableName = getTableName(tv.getText().toString());

        if(tableOrder == null) {       //Table has no order(occupied) or vacant
            if(clickedTable.isVacant()){  //vacant table
                Drawable tvDrawable;
                tvDrawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));
                //the color is a direct color int and not a color resource
                DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.red));
                v.setBackground(tvDrawable);

                clickedTable.setVacant(false);
                clickedTable.setOccupied(true);
                Toast.makeText(TablePage.this, "" + tableName + " occupied successfully", Toast.LENGTH_SHORT).show();
            }else { //occupy table
                Drawable tvDrawable;
                tvDrawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));
                //the color is a direct color int and not a color resource
                DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.green));
                v.setBackground(tvDrawable);

                clickedTable.setVacant(true);
                clickedTable.setOccupied(false);
                Toast.makeText(TablePage.this, "" + tableName + " occupy removed successfully", Toast.LENGTH_SHORT).show();
            }
            longClickOccupiedTable = clickedTable;
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.insertOrUpdate(longClickOccupiedTable);
                }
            });
        }else{      //Table has an order
            Toast.makeText(TablePage.this, "This table has an order", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}