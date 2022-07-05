package com.findbulous.pos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.findbulous.pos.databinding.RegisterPageBinding;

public class RegisterPage extends AppCompatActivity {

    private RegisterPageBinding binding;

    private Context contextpage;
    private String statuslogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextpage = RegisterPage.this;
        binding = DataBindingUtil.setContentView(this, R.layout.register_page);

        String[] genders = getResources().getStringArray(R.array.gender);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.genderSpinner.setDropDownVerticalOffset(80);
        binding.genderSpinner.setAdapter(adapter);
        binding.genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

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
        binding.confirmPasswordShowHide.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        //Press
                        binding.confirmPasswordEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        binding.confirmPasswordEt.setSelection(binding.confirmPasswordEt.getText().length());
                        binding.confirmPasswordShowHide.setImageResource(R.drawable.ic_hide);
                        return true;
                    case MotionEvent.ACTION_UP:
                        //Release
                        binding.confirmPasswordEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        binding.confirmPasswordEt.setSelection(binding.confirmPasswordEt.getText().length());
                        binding.confirmPasswordShowHide.setImageResource(R.drawable.ic_eye);
                        return true;
                }
                return true;
            }
        });

        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputCheck()){
                    //register acc
                }
            }
        });
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contextpage, LoginPage.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean inputCheck(){
        if(nullOrEmptyCheck(binding.emailEt) && nullOrEmptyCheck(binding.passwordEt)
                && nullOrEmptyCheck(binding.confirmPasswordEt) && nullOrEmptyCheck(binding.usernameEt)){
            if(emailCheck(binding.emailEt)) {
                if (samePasswordCheck(binding.passwordEt, binding.confirmPasswordEt)) {
                    return true;
                }else{
                    Toast.makeText(contextpage, "Password and confirm password must be same", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }else{
                Toast.makeText(contextpage, "Invalid Email Address", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else{
            Toast.makeText(contextpage, "Please fill in all the details", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean samePasswordCheck(EditText password, EditText confirmPassword){
        if(password.getText().toString().equals(confirmPassword.getText().toString())){
            return true;
        }else{
            return false;
        }
    }

    private boolean nullOrEmptyCheck(EditText et){
        if(et.getText().toString().isEmpty())
            return false;
        return true;
    }

    private boolean emailCheck(EditText email){
        if(Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches())
            return true;
        return false;
    }
}