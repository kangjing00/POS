package com.example.pos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.pos.databinding.RegisterPageBinding;

public class RegisterPage extends AppCompatActivity {

    RegisterPageBinding binding;

    private Context contextpage;
    private String statuslogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextpage = RegisterPage.this;
        binding = DataBindingUtil.setContentView(this, R.layout.register_page);

        binding.button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contextpage, LoginPage.class);
                startActivity(intent);
                finish();
            }
        });
    }
}