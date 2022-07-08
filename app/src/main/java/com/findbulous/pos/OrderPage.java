package com.findbulous.pos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.findbulous.pos.Adapters.OrderOrderLineAdapter;
import com.findbulous.pos.Network.CheckConnection;
import com.findbulous.pos.OrderFragments.FragmentOfflineOrder;
import com.findbulous.pos.OrderFragments.FragmentOrderHistory;
import com.findbulous.pos.OrderFragments.FragmentOrderOnHold;
import com.findbulous.pos.databinding.OrderPageBinding;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class OrderPage extends CheckConnection {

    private OrderPageBinding binding;
    private FragmentManager fm;
    private FragmentTransaction ft;
    //Cash in out popup
    private RadioButton cash_in_rb, cash_out_rb;
    private EditText cash_in_out_amount, cash_in_out_reason;
    private MaterialButton cash_in_out_cancel, cash_in_out_confirm;
    //Sync popup
    private TextView product_sync_btn, transactions_sync_btn;
    //Order Selected
    private Order orderSelected;
    //Recyclerview
    private OrderOrderLineAdapter orderOrderLineAdapter;
    private ArrayList<Order_Line> order_lines;
    //Realm
    private Realm realm;
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
        binding.orderHistoryRb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ft = fm.beginTransaction();
                ft.replace(binding.orderFragmentFl.getId(), new FragmentOrderHistory()).commit();
                binding.syncOrderBtn.setVisibility(View.GONE);
                binding.orderRelativeLayout.setVisibility(View.VISIBLE);
                resetOrderSelected();
            }
        });
        binding.offlineOrderRb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ft = fm.beginTransaction();
                ft.replace(binding.orderFragmentFl.getId(), new FragmentOfflineOrder()).commit();
                binding.syncOrderBtn.setVisibility(View.VISIBLE);
                binding.orderRelativeLayout.setVisibility(View.VISIBLE);
                resetOrderSelected();
            }
        });
        binding.orderOnHoldRb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ft = fm.beginTransaction();
                ft.replace(binding.orderFragmentFl.getId(), new FragmentOrderOnHold()).commit();
                binding.orderRelativeLayout.setVisibility(View.GONE);
                resetOrderSelected();
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
        WindowManager wm = (WindowManager) OrderPage.this.getSystemService(Context.WINDOW_SERVICE);
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

    public void setOrderSelected(Order order){
        orderSelected = order;
        RealmResults<Order_Line> results = realm.where(Order_Line.class).equalTo("order.order_id", orderSelected.getOrder_id()).findAll();
        order_lines.clear();
        order_lines.addAll(realm.copyFromRealm(results));
        orderOrderLineAdapter.notifyDataSetChanged();

        double tax = orderSelected.getAmount_tax();
        double tip = orderSelected.getTip_amount();
        double amount_total = orderSelected.getAmount_total();
        double amount_paid = orderSelected.getAmount_paid();
        double balance = amount_paid - amount_total;
        double subtotal = 0.0, discount = 0.0;
        for(int i = 0; i < order_lines.size(); i++){
            subtotal += order_lines.get(i).getPrice_total();
            discount += ((order_lines.get(i).getPrice_total() * order_lines.get(i).getDiscount()) / 100);
        }
        if(discount != 0.0){
            discount = -discount;
        }

        binding.orderDetailTax.setText(String.format("%.2f", tax));
        binding.orderDetailTip.setText(String.format("%.2f", tip));
        binding.orderDetailGrandTotal.setText(String.format("%.2f", amount_total));
        binding.orderDetailCash.setText(String.format("%.2f", amount_paid));
        binding.orderDetailBalance.setText(String.format("%.2f", balance));
        binding.orderDetailSubtotal.setText(String.format("%.2f", subtotal));
        binding.orderDetailDiscount.setText(String.format("%.2f", discount));

        binding.orderDetailOrderId.setText("#" + orderSelected.getOrder_id());
        if(orderSelected.getTable() != null){
            binding.orderDetailType.setText("Dine-in - " + orderSelected.getTable().getName());
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
        order_lines.clear();
        orderOrderLineAdapter.notifyDataSetChanged();
        binding.orderDetailTax.setText("0.00");
        binding.orderDetailTip.setText("0.00");
        binding.orderDetailGrandTotal.setText("0.00");
        binding.orderDetailCash.setText("0.00");
        binding.orderDetailBalance.setText("0.00");
        binding.orderDetailSubtotal.setText("0.00");
        binding.orderDetailDiscount.setText("0.00");

        binding.orderDetailOrderId.setText("#00000");
        binding.orderDetailType.setText("[Order Type]");
        binding.orderDetailCustomerName.setText("[Customer Name]");
    }
}