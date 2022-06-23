package com.findbulous.pos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.findbulous.pos.databinding.ChoosePosPermissionPageBinding;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChoosePOSPermissionPage extends AppCompatActivity {

    private ChoosePosPermissionPageBinding binding;

    private Context contextpage;
    private String statuslogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextpage = ChoosePOSPermissionPage.this;
        binding = DataBindingUtil.setContentView(this, R.layout.choose_pos_permission_page);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        Date todayDate = new Date();
        binding.todayDate.setText(dateFormatter.format(todayDate));

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contextpage, LoginPage.class);
                startActivity(intent);
                finish();
            }
        });

        binding.orderOnlyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contextpage, HomePage.class);
                startActivity(intent);
                finish();
            }
        });
        binding.cashierBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contextpage, HomePage.class);
                startActivity(intent);
                finish();
            }
        });
        binding.kdsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contextpage, HomePage.class);
                startActivity(intent);
                finish();
            }
        });
    }
}