package com.example.pos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.example.pos.PaymentTab.PaymentMethodPagerAdapter;
import com.example.pos.databinding.PaymentPageBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayoutMediator;

public class PaymentPage extends AppCompatActivity {

    private PaymentPageBinding binding;
    private PaymentMethodPagerAdapter paymentMethodPagerAdapter;
    private String[] titles = new String[]{"Cash", "Other Modes"};
    private PaymentPageViewModel viewModel;
    private Button add_popup_negative_btn, add_popup_positive_btn;
    private EditText add_popup_et;
    //Cash in out popup
    private RadioButton cash_in_rb, cash_out_rb;
    private EditText cash_in_out_amount, cash_in_out_reason;
    private MaterialButton cash_in_out_cancel, cash_in_out_confirm;
    //Sync popup
    private TextView product_sync_btn, transactions_sync_btn;

    private SharedPreferences currentOrderSharePreference;
    private SharedPreferences.Editor currentOrderSharePreferenceEdit;

    String statuslogin;
    Context contextpage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextpage = PaymentPage.this;
        binding = DataBindingUtil.setContentView(this, R.layout.payment_page);
        viewModel = new ViewModelProvider(this).get(PaymentPageViewModel.class);
        binding.setPaymentPageViewModel(viewModel);
        binding.setLifecycleOwner(this);

        currentOrderSharePreference = getSharedPreferences("CurrentOrder",MODE_MULTI_PROCESS);
        currentOrderSharePreferenceEdit = currentOrderSharePreference.edit();

        //Body Setting
        paymentMethodPagerAdapter = new PaymentMethodPagerAdapter(this);

        binding.paymentMethodViewPager.setAdapter(paymentMethodPagerAdapter);
        new TabLayoutMediator(binding.paymentMethodTl, binding.paymentMethodViewPager,
            (
                (
                    (tab, position) -> tab.setText(titles[position])
                )
            )
        ).attach();


        //OnClickListener
        //Body
        {
        binding.paymentBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Toast.makeText(contextpage, "Back Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        binding.paymentBarAddTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTipPopup(view);
                Toast.makeText(contextpage, "Add Tip Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        binding.paymentOrderDetailViewAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(contextpage, "View All Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        binding.paymentTipCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.textView9.setVisibility(View.GONE);
                binding.paymentTip.setVisibility(View.GONE);
                binding.getPaymentPageViewModel().setPayment_tip("0.00");
                binding.paymentTipCancelBtn.setVisibility(View.GONE);
            }
        });
        binding.paymentOrderDetailConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentOrderSharePreferenceEdit.putInt("orderingState", 0);
                currentOrderSharePreferenceEdit.putInt("orderId", -1);
                currentOrderSharePreferenceEdit.commit();
                Toast.makeText(contextpage, "Payment Success", Toast.LENGTH_SHORT).show();
                finish();
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

    private void showAddTipPopup(View view) {
        PopupWindow popup = new PopupWindow(contextpage);
        View layout = getLayoutInflater().inflate(R.layout.payment_add_tip_popup, null);
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
        binding.textView9.setVisibility(View.VISIBLE);
        binding.paymentTip.setVisibility(View.VISIBLE);
        binding.paymentTipCancelBtn.setVisibility(View.VISIBLE);
        //binding.cartInclude.tvDiscount.setTextColor(contextpage.getResources().getColor(R.color.darkOrange));
        popup.showAsDropDown(binding.paymentBarAddTip, -520, -180);

        //Popup Buttons
        add_popup_negative_btn = (Button)layout.findViewById(R.id.add_tip_popup_negative_btn);
        add_popup_positive_btn = (Button)layout.findViewById(R.id.add_tip_popup_positive_btn);
        add_popup_et = (EditText)layout.findViewById(R.id.add_tip_popup_et);
        add_popup_et.setText(binding.getPaymentPageViewModel().getPayment_tip().getValue());

        add_popup_negative_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                popup.dismiss();
                Toast.makeText(contextpage, "Cancel", Toast.LENGTH_SHORT).show();
            }
        });

        add_popup_positive_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                binding.getPaymentPageViewModel().setPayment_tip(add_popup_et.getText().toString());
                popup.dismiss();
                Toast.makeText(contextpage, "Positive button from popup", Toast.LENGTH_SHORT).show();
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
}