package com.example.pos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;

import com.example.pos.databinding.LoginActivityBinding;

public class LoginPage extends AppCompatActivity {

    private LoginActivityBinding binding;

    private Context contextpage;
    private String statuslogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextpage = LoginPage.this;
        binding = DataBindingUtil.setContentView(this, R.layout.login_activity);

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contextpage, ChoosePOSPermissionPage.class);
                startActivity(intent);
                finish();
            }
        });
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contextpage, RegisterPage.class);
                startActivity(intent);
                finish();
            }
        });
        binding.passwordShowHide.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        //Press
                        binding.passwordEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        binding.passwordEt.setSelection(binding.passwordEt.getText().length());
                        binding.passwordShowHide.setImageResource(R.drawable.ic_hide);
                        return true;
                    case MotionEvent.ACTION_UP:
                        //Release
                        binding.passwordEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        binding.passwordEt.setSelection(binding.passwordEt.getText().length());
                        binding.passwordShowHide.setImageResource(R.drawable.ic_eye);
                        return true;
                }
                return true;
            }
        });
    }

    public void setPreferences(Context context, String key, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences("LoginSuccess", Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.commit();

    }

    public String getPreferences(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences("LoginSuccess",	Context.MODE_PRIVATE);
        String position = prefs.getString(key, "");
        return position;
    }
}