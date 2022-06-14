package com.example.pos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pos.databinding.TablePageBinding;
import com.google.android.material.button.MaterialButton;

import org.w3c.dom.Text;

public class TablePage extends AppCompatActivity implements View.OnClickListener {

    private TablePageBinding binding;

    //Cash in out popup
    private RadioButton cash_in_rb, cash_out_rb;
    private EditText cash_in_out_amount, cash_in_out_reason;
    private MaterialButton cash_in_out_cancel, cash_in_out_confirm;
    //Sync popup
    private TextView product_sync_btn, transactions_sync_btn;

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

        int[][] empty = {
                {1,5},{1,10},{2,5},{2,10},{3,5},{3,10},
                {4,5},{4,10},{5,5},{5,10},{1,2},{7,8}
        };
        //Body Settings
        displayTables(7,18, empty);

        //OnClickListener
        //body
        {
        binding.tableNoticeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(contextpage, "Show all table Button Clicked", Toast.LENGTH_SHORT).show();
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

    private void showAddonAndProceed(View view, TextView tv) {
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

        TextView tvTableID = layout.findViewById(R.id.table_addon_proceed_popup_table_name);
        TextView tvOrderID = layout.findViewById(R.id.table_addon_proceed_popup_order_id);
        for(int i = 0; i < tv.getText().length(); i++){
            if(tv.getText().toString().substring(i,i+1).equalsIgnoreCase("\n")){
                tvTableID.setText(tv.getText().toString().substring(0, i));
                tvOrderID.setText(tv.getText().toString().substring(i+1, tv.getText().length()));
            }
        }

//        //Popup Buttons
//        add_popup_negative_btn = (Button)layout.findViewById(R.id.add_discount_popup_negative_btn);
//        add_popup_positive_btn = (Button)layout.findViewById(R.id.add_discount_popup_positive_btn);
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

    private void displayTables(int noRow, int noColumn, int[][] empty){
        int tableNumber = 1;

        for(int row = 1; row <= noRow; row++){
            TableRow tr = new TableRow(contextpage);
            for(int column = 1; column <= noColumn; column++){
                TextView table = new TextView(contextpage);

                table.setText("T"+ tableNumber +"\n"+"#00000");
                table.setWidth((int) (80 * getResources().getDisplayMetrics().density));
                table.setHeight((int) (80 * getResources().getDisplayMetrics().density));

                Drawable tvDrawable;
                tvDrawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));
                //the color is a direct color int and not a color resource
                DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.green));
                table.setBackground(tvDrawable);

                table.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                table.setGravity(Gravity.CENTER);
                table.setTextColor(Color.BLACK);
                table.setClickable(true);

                for(int x = 0; x < empty.length; x++){
                    for(int y = 0; y < empty[x].length; y++){
                        if(empty[x][0] == row)
                            if(empty[x][1] == column)
                                table.setVisibility(View.INVISIBLE);
                    }
                }

                table.setId(tableNumber);
                table.setOnClickListener(TablePage.this);

                tr.addView(table);
                tableNumber++;
            }
            binding.tableManagementLayout.addView(tr);
        }
    }

    @Override
    public void onClick(View v) {
        int clicked_id = v.getId();
        Drawable tvDrawable;
        tvDrawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_square_table_4_modified));
        //the color is a direct color int and not a color resource
        DrawableCompat.setTint(tvDrawable, getResources().getColor(R.color.red));
        v.setBackground(tvDrawable);
        TextView tv = (TextView) v.findViewById(v.getId());
        showAddonAndProceed(v, tv);

        Toast.makeText(TablePage.this, "table clicked" + v.getId() + "'" + tv.getText().charAt(2) + "'", Toast.LENGTH_SHORT).show();
    }
}