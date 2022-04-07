package com.example.pos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.pos.CashierFragments.FragmentCashierDrawer;
import com.example.pos.OrderFragments.FragmentOfflineOrder;
import com.example.pos.OrderFragments.FragmentOrderHistory;
import com.example.pos.OrderFragments.FragmentOrderOnHold;
import com.example.pos.databinding.OrderPageBinding;

public class OrderPage extends AppCompatActivity {

    private OrderPageBinding binding;
    private FragmentManager fm;
    private FragmentTransaction ft;

    private String statuslogin;
    private Context contextpage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextpage = OrderPage.this;
        binding = DataBindingUtil.setContentView(this, R.layout.order_page);
        //Appbar Settings
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //Nav Settings
        binding.navbarLayoutInclude.navBarOrders.setChecked(true);

        //Body Settings
        binding.orderHistoryRb.setChecked(true);
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
            }
        });
        binding.offlineOrderRb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ft = fm.beginTransaction();
                ft.replace(binding.orderFragmentFl.getId(), new FragmentOfflineOrder()).commit();
                binding.syncOrderBtn.setVisibility(View.VISIBLE);
                binding.orderRelativeLayout.setVisibility(View.VISIBLE);
            }
        });
        binding.orderOnHoldRb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ft = fm.beginTransaction();
                ft.replace(binding.orderFragmentFl.getId(), new FragmentOrderOnHold()).commit();
                binding.orderRelativeLayout.setVisibility(View.GONE);
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

        binding.toolbarLayoutIncl.toolbarSelectTable.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Toast.makeText(contextpage, "Select Table Button Clicked", Toast.LENGTH_SHORT).show();
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
}