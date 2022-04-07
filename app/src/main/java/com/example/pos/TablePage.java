package com.example.pos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pos.databinding.TablePageBinding;
import com.google.android.material.button.MaterialButton;

public class TablePage extends AppCompatActivity {

    private TablePageBinding binding;
    //Cash in out popup
    private RadioButton cash_in_rb, cash_out_rb;
    private EditText cash_in_out_amount, cash_in_out_reason;
    private MaterialButton cash_in_out_cancel, cash_in_out_confirm;

    private String statuslogin;
    private Context contextpage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextpage = TablePage.this;
        binding = DataBindingUtil.setContentView(this, R.layout.table_page);

        //Toolbar Settings
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //Nav Settings
        binding.navbarLayoutInclude.navBarTables.setChecked(true);

        //Body Settings


        //OnClickListener
        //body
        {
        binding.tableNoticeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddonAndProceed(view);
                Toast.makeText(contextpage, "Show all table Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        //Toolbar buttons
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

    private void showAddonAndProceed(View view) {
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
//        popup.showAsDropDown(tv_discount, -620, -180);

//        //Popup Buttons
//        add_popup_negative_btn = (Button)layout.findViewById(R.id.add_discount_popup_negative_btn);
//        add_popup_positive_btn = (Button)layout.findViewById(R.id.add_discount_popup_positive_btn);
    }
}