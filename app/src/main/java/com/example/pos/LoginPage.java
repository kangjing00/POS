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

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class LoginPage extends AppCompatActivity {

    private LoginActivityBinding binding;

    private Context contextpage;
    private String statuslogin;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextpage = LoginPage.this;

        binding = DataBindingUtil.setContentView(this, R.layout.login_activity);

        //Realm Configuration
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("Realm Database")
                .allowWritesOnUiThread(true).build();
        Realm.setDefaultConfiguration(config);
        realm = Realm.getDefaultInstance();


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
                deleteRealm();
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




    private void deleteRealm(){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
    }

    private void insertDummyProductData(){
        for(int i = 1; i < 100; i++){
            saveToDb("Nasi Lemak " + i, 10.50);
        }
    }

    private Product saveToDb(String product_name, double product_price) {
        Product product = new Product();
        //val dataModel2 = DataModel2()
        //var id2:Number? = realm.where<DataModel2>(DataModel2::class.java).max("id")
        Number id = realm.where(Product.class).max("product_id");

//        val nextID2: Int = if(id2 == null){
//            1
//        }else{
//            id2.toInt() + 2
//        }

        int nextID = -1;
        System.out.println(id);
        if(id == null){
            nextID = 1;
        }else{
            nextID = id.intValue() + 1;
        }

        product.setProduct_id(nextID);
        product.setProduct_name(product_name);
        product.setProduct_price(product_price);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                //it.insert(dataModel)
                //it.copyToRealmOrUpdate(dataModel)
                realm.insertOrUpdate(product);
                //it.copyToRealm(dataModel2)
            }
        });

        return product;
    }
}