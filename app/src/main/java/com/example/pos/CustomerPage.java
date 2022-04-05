package com.example.pos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Toast;
import com.example.pos.databinding.CustomerPageBinding;

public class CustomerPage extends AppCompatActivity {

    private CustomerPageBinding binding;

    //Cart //Popup
    private Button add_discount_popup_negative_btn, add_discount_popup_positive_btn;
    private EditText add_discount_popup_et;
    private Button  add_note_popup_negative_btn, add_note_popup_positive_btn;
    private EditText add_note_popup_et;
    private ImageButton add_note_popup_exit_btn;
    // Storing data into SharedPreferences
    private SharedPreferences cartSharedPreference;
    // Creating an Editor object to edit(write to the file)
    private SharedPreferences.Editor cartSharedPreferenceEdit;

    private String statuslogin;
    private Context contextpage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextpage = CustomerPage.this;

        binding = DataBindingUtil.setContentView(this, R.layout.customer_page);
        //Toolbar Settings
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //Nav Settings
        binding.navbarLayoutInclude.navBarCustomers.setChecked(true);

        //Cart Settings
        cartSharedPreference = getSharedPreferences("CurrentOrder",MODE_MULTI_PROCESS);
        cartSharedPreferenceEdit = cartSharedPreference.edit();

        //Body Settings
        //Cart Note
        if(!cartSharedPreference.getString("cartNote", "").equalsIgnoreCase("")){
            binding.cartInclude.cartOrderNoteBtn.setTextColor(contextpage.getResources().getColor(R.color.green));
        }else{
            binding.cartInclude.cartOrderNoteBtn.setTextColor(contextpage.getResources().getColor(R.color.darkOrange));
        }
        //Cart Discount
        if(!cartSharedPreference.getString("cartDiscount", "0.00").equalsIgnoreCase("0.00")){
            binding.cartInclude.cartOrderSummaryDiscountRl.setVisibility(View.VISIBLE);
            binding.cartInclude.cartOrderSummaryDiscount.setText("- RM " + cartSharedPreference.getString("cartDiscount", "0.00"));
            binding.cartInclude.cartOrderDiscountBtn.setTextColor(contextpage.getResources().getColor(R.color.green));
        }


        //OnClickListener
        //body
        {
        binding.customerCurrentRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(contextpage, "Button Clicked", Toast.LENGTH_SHORT).show();
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
        //Cart buttons
        {
        binding.cartInclude.cartOrderDiscountBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                showCartOrderAddDiscountPopup(view);
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
                showCartOrderAddNotePopup(view);
                Toast.makeText(contextpage, "Order Note Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        binding.cartInclude.cartAddCustomer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(contextpage, "Add Customer Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        binding.cartInclude.cartOrderSummaryHoldBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(contextpage, "Hold Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        binding.cartInclude.cartOrderSummaryProceedBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contextpage, PaymentPage.class);
                startActivity(intent);
                Toast.makeText(contextpage, "Proceed Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        binding.cartInclude.cartBtnAdd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(contextpage, "Add Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        binding.cartInclude.cartBtnScan.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(contextpage, "Scan Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        binding.cartInclude.cartBtnReload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(contextpage, "Reload Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        binding.cartInclude.cartOrderSummaryDiscountCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cartSharedPreferenceEdit.putString("cartDiscount", "0.00");
                cartSharedPreferenceEdit.commit();
                binding.cartInclude.cartOrderSummaryDiscount.setText("- RM 0.00");
                binding.cartInclude.cartOrderSummaryDiscountRl.setVisibility(View.GONE);
                binding.cartInclude.cartOrderDiscountBtn.setTextColor(contextpage.getResources().getColor(R.color.darkOrange));
            }
        });
        }
    }

    private void showCartOrderAddNotePopup(View view) {
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
        add_note_popup_negative_btn = (Button)layout.findViewById(R.id.add_note_popup_negative_btn);
        add_note_popup_positive_btn = (Button)layout.findViewById(R.id.add_note_popup_positive_btn);
        add_note_popup_et = (EditText)layout.findViewById(R.id.add_note_popup_et);
        add_note_popup_exit_btn = (ImageButton)layout.findViewById(R.id.add_note_popup_exit_btn);

        add_note_popup_et.setText(cartSharedPreference.getString("cartNote", ""));

        add_note_popup_exit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.dismiss();
            }
        });
        add_note_popup_negative_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                add_note_popup_et.getText().clear();
                Toast.makeText(contextpage, "Clear", Toast.LENGTH_SHORT).show();
            }
        });
        add_note_popup_positive_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cartSharedPreferenceEdit.putString("cartNote", add_note_popup_et.getText().toString());
                cartSharedPreferenceEdit.commit();
                popup.dismiss();
                if(!cartSharedPreference.getString("cartNote", "").equalsIgnoreCase("")){
                    binding.cartInclude.cartOrderNoteBtn.setTextColor(contextpage.getResources().getColor(R.color.green));
                }else{
                    binding.cartInclude.cartOrderNoteBtn.setTextColor(contextpage.getResources().getColor(R.color.darkOrange));
                }
                Toast.makeText(contextpage, "Added & Updated", Toast.LENGTH_SHORT).show();
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
        popup.showAsDropDown(binding.cartInclude.tvDiscount, -620, -180);

        //Popup Buttons
        add_discount_popup_negative_btn = (Button)layout.findViewById(R.id.add_discount_popup_negative_btn);
        add_discount_popup_positive_btn = (Button)layout.findViewById(R.id.add_discount_popup_positive_btn);
        add_discount_popup_et = (EditText)layout.findViewById(R.id.add_discount_popup_et);

        add_discount_popup_et.setText(cartSharedPreference.getString("cartDiscount", "0.00"));

        add_discount_popup_negative_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                popup.dismiss();
                Toast.makeText(contextpage, "Cancel", Toast.LENGTH_SHORT).show();
            }
        });

        add_discount_popup_positive_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                cartSharedPreferenceEdit.putString("cartDiscount", add_discount_popup_et.getText().toString());
                cartSharedPreferenceEdit.commit();
                binding.cartInclude.cartOrderSummaryDiscount.setText("- RM " + add_discount_popup_et.getText().toString());
                popup.dismiss();
                Toast.makeText(contextpage, "Positive button from popup", Toast.LENGTH_SHORT).show();
            }
        });

        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                binding.cartInclude.tvDiscount.setTextColor(contextpage.getResources().getColor(R.color.black));
            }
        });
    }
}