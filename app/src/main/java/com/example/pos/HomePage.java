package com.example.pos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.example.pos.databinding.HomePageBinding;
import com.example.pos.databinding.ViewProductMenuBinding;

import java.util.ArrayList;

public class HomePage extends AppCompatActivity {

    private HomePageBinding binding;

    //Cart //Popup
    private Button add_discount_popup_negative_btn, add_discount_popup_positive_btn;
    private EditText add_discount_popup_et;
    private Button  add_note_popup_negative_btn, add_note_popup_positive_btn;
    private EditText add_note_popup_et;
    // Storing data into SharedPreferences
    private SharedPreferences cartSharedPreference;
    // Creating an Editor object to edit(write to the file)
    private SharedPreferences.Editor cartSharedPreferenceEdit;

    private String statuslogin;
    private Context contextpage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextpage = HomePage.this;

        binding = DataBindingUtil.setContentView(this, R.layout.home_page);
        //Appbar Settings
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //Nav Settings
        binding.navbarLayoutInclude.navBarHome.setChecked(true);

        //Cart Settings
        cartSharedPreference = getSharedPreferences("CurrentOrder",MODE_MULTI_PROCESS);
        cartSharedPreferenceEdit = cartSharedPreference.edit();

        //Body
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(contextpage, 4, LinearLayoutManager.HORIZONTAL, false);
        binding.productListRv.setLayoutManager(layoutManager);
        //TODO create product object and declare the below variable as global, the info get from database
        ArrayList<String> products = null;
        //product_list_rv.setAdapter(new ProductsAdapter(contextpage, products));
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

        add_note_popup_et.setText(cartSharedPreference.getString("cartNote", ""));

        add_note_popup_negative_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                popup.dismiss();
                Toast.makeText(contextpage, "Cancel", Toast.LENGTH_SHORT).show();
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


//    public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ProductsViewHolder>{
//        Context c;
//        //TODO change all the words 'String' below to the product object name
//        ArrayList<String> products;
//
//        public ProductsAdapter(Context c, ArrayList<String> products){
//            this.c = c;
//            this.products = products;
//        }
//
//        @Override
//        public ProductsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View v= LayoutInflater.from(c).inflate(R.layout.view_product_menu, parent, false);
//            return new ProductsViewHolder(v);
//        }
//
//        @Override
//        public void onBindViewHolder(final ProductsViewHolder holder, int position) {
//            final String product = products.get(position);
//
//            holder.menu_product_name.setText(product);
//            holder.menu_product_price.setText(product);
////            holder.menu_product_image.setImageResource();
//
//            holder.setItemClickListener(new ItemClickListener() {
//                @Override
//                public void onItemClick(int position) {
//                    //go somewhere
//                }
//            });
//        }
//
//        @Override
//        public int getItemCount() {
//            return products.size();
//        }
//
//        public class ProductsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
//
//           // private ViewProductMenuBinding binding;
//
//            TextView menu_product_name, menu_product_price;
//            ImageView menu_product_image;
//            ItemClickListener itemClickListener;
//
//            public ProductsViewHolder(View itemView) {
//                super(itemView);
//                menu_product_name = (TextView)itemView.findViewById(R.id.menu_product_name);
//                menu_product_price = (TextView)itemView.findViewById(R.id.menu_product_price);
//                menu_product_image = (ImageView)itemView.findViewById(R.id.menu_product_image);
//                itemView.setOnClickListener(this);
//            }
//
//            public void setItemClickListener(ItemClickListener itemClickListener){
//                this.itemClickListener = itemClickListener;
//            }
//
//            @Override
//            public void onClick(View v) {
//                this.itemClickListener.onItemClick(getLayoutPosition());
//            }
//        }
//    }

}