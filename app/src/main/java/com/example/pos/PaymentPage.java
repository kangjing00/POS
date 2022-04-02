package com.example.pos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pos.PaymentTab.PaymentMethodPagerAdapter;
import com.example.pos.databinding.PaymentPageBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayoutMediator;

public class PaymentPage extends AppCompatActivity {

    PaymentPageBinding binding;
    PaymentMethodPagerAdapter paymentMethodPagerAdapter;
    private String[] titles = new String[]{"Cash", "Other Modes"};

    String statuslogin;
    Context contextpage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextpage = PaymentPage.this;
        binding = DataBindingUtil.setContentView(this, R.layout.payment_page);

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

        binding.toolbarLayoutIncl.toolbarSelectTable.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view) {
                   Toast.makeText(contextpage, "Select Table Button Clicked", Toast.LENGTH_SHORT).show();
               }
           }
        );
        }
    }
}