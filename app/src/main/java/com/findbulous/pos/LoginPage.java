package com.findbulous.pos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.findbulous.pos.Network.NetworkUtils;
import com.findbulous.pos.databinding.LoginActivityBinding;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;

public class LoginPage extends AppCompatActivity {

    private LoginActivityBinding binding;

    private Context contextpage;
    private String statuslogin;

    private Realm realm;

    private ArrayList<POS_Category> product_categories;
    private ArrayList<Product> products;
    private ArrayList<Product_Tax> product_taxes;

    private ArrayList<Floor> floors;
    private ArrayList<State> states;
    private ArrayList<Table> tables;

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

        product_categories = new ArrayList<>();
        products = new ArrayList<>();
        product_taxes = new ArrayList<>();

        floors = new ArrayList<>();
        states = new ArrayList<>();
        tables = new ArrayList<>();

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contextpage, ChoosePOSPermissionPage.class);
                startActivity(intent);
                finish();
                //new loadProduct().execute();
                //new loadFloorAndTable().execute();
            }
        });
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //deleteRealm();
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

    public class loadProduct extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... strings) {
            String connection_error = "";
            String url = "https://www.c3rewards.com/api/merchant/?module=products";
            String agent = "c092dc89b7aac085a210824fb57625db";
            String jsonUrl =url + "&agent=" + agent;
            System.out.println(jsonUrl);

            int page = 1, productCounter = 0;
            while(page > 0){
                String jsonUrlPage = jsonUrl + "&page=" + page;
                URL obj;
                try{
                    obj = new URL(jsonUrlPage);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    // optional default is GET
                    con.setRequestMethod("GET");
                    //add request header
                    int responseCode = con.getResponseCode();
                    System.out.println("\nSending 'GET' request to URL : " + jsonUrlPage);
                    System.out.println("Response Code : " + responseCode);

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    System.out.println(response);
                    String data = response.toString();
                    try{
                        JSONObject json = new JSONObject(data);
                        String status = json.getString("status");

                        if (status.equals("OK")) {
                            JSONObject jresult = json.getJSONObject("result");
                            JSONArray jcategories = jresult.getJSONArray("pos_categories");
                            JSONArray jproducts = jresult.getJSONArray("products");

                            if((jproducts.length() != 0) || (jcategories.length() != 0)){
                                page++;
                            }else{
                                page = -1;
                            }
                            // page = -1 below for testing purpose
                            page = -1;


                            // Product Category
                            loadProductCategories(jcategories, true);
                            // Product
                            for (int a = 0; a < jproducts.length(); a++) {
                                JSONObject jo = jproducts.getJSONObject(a);
                                JSONArray jProductTax = null;
                                Product product = new Product(jo.getInt("product_id"), jo.getString("name"),
                                        jo.getString("default_code"), jo.getDouble("list_price"), jo.getDouble("standard_price"),
                                        jo.getDouble("margin"), jo.getDouble("margin_percent"),jo.getDouble("price_incl_tax"),
                                        jo.getDouble("price_excl_tax"), jo.getString("display_list_price"),jo.getString("display_standard_price"),
                                        jo.getString("display_margin"), jo.getString("display_margin_percent"), jo.getString("display_price_incl_tax"),
                                        jo.getString("display_price_excl_tax"));
                                // Product Taxes
                                if(jo.getJSONArray("taxes").length() > 0){
                                    jProductTax = jo.getJSONArray("taxes");
                                    for(int x = 0; x < jProductTax.length(); x++){
                                        JSONObject joProductTax = jProductTax.getJSONObject(x);
                                        String product_tax_id = product.getProduct_id() + joProductTax.getString("tax_id");
                                        Product_Tax product_tax = new Product_Tax(Integer.valueOf(product_tax_id),
                                                joProductTax.getDouble("amount"), joProductTax.getString("display_amount"), product);
                                        product_taxes.add(product_tax);
                                    }
                                }

                                products.add(product);
                                productCounter++;
                            }
                        }
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }catch (IOException e){
                    Log.e("error", "cannot fetch data");
                    page = -1;
                    connection_error = e + "";
                    System.out.println(connection_error);
                }
            }
            System.out.println("Product Counter: " + productCounter);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(!NetworkUtils.isNetworkAvailable(contextpage)){
                Toast.makeText(contextpage, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }else{
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.insertOrUpdate(products);
                        realm.insertOrUpdate(product_categories);
                        realm.insertOrUpdate(product_taxes);
                    }
                });
            }
        }
    }
    //Recursive retrieve category and sub-category and subsub-category and subsubsub...
    private RealmList<POS_Category> loadProductCategories(JSONArray array, boolean firstLevel){
        RealmList<POS_Category> categories_list = new RealmList<>();
        RealmList<POS_Category> sub_categories_list;
        POS_Category product_category;
        int counter = 0;
        while(counter < array.length()){
            try {
                JSONObject data = array.getJSONObject(counter);
                product_category = new POS_Category(data.getInt("pos_categ_id"),
                        data.getString("name"), null);

                // Is there any Sub-categories?
                JSONArray ja_sub_categories = data.getJSONArray("pos_categories");
                if (ja_sub_categories.length() > 0) {
                    //Second level category list
                    sub_categories_list = loadProductCategories(ja_sub_categories, false); //second lvl = third lvl
                    product_category.setPos_categories(sub_categories_list);
                }
                if(firstLevel)
                    product_categories.add(product_category);

                //Main level category list
                categories_list.add(product_category);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            counter++;
        }
        //Main level category list
        return categories_list;
    }

    public class loadFloorAndTable extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            String url = "https://www.c3rewards.com/api/merchant/?module=restaurants";
            String agent = "c092dc89b7aac085a210824fb57625db";
            String jsonUrl = url + "&agent=" + agent;
            System.out.println(jsonUrl);


            URL obj;
            try {
                obj = new URL(jsonUrl);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                // optional default is GET
                con.setRequestMethod("GET");
                //add request header
                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'GET' request to URL : " + jsonUrl);
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;

                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                System.out.println(response);
                String data = response.toString();
                try {
                    JSONObject json = new JSONObject(data);
                    String status = json.getString("status");

                    if (status.equals("OK")) {
                        JSONObject jresult = json.getJSONObject("result");
                        JSONArray jfloors = jresult.getJSONArray("floors");
                        JSONArray jstates = jresult.getJSONArray("states");

                        for(int i = 0; i < jstates.length(); i++){
                            JSONObject js = jstates.getJSONObject(i);
                            State state = new State((i+1), js.getString("code"), js.getString("name"));
                            states.add(state);
                        }

                        for (int a = 0; a < jfloors.length(); a++) {
                            JSONObject jf = jfloors.getJSONObject(a);
                            Floor floor = new Floor(jf.getInt("floor_id"), jf.getString("name"),
                                    jf.getInt("sequence"), jf.getString("active"));
                            floors.add(floor);

                            JSONArray jtables = jf.getJSONArray("tables");
                            for(int x = 0; x < jtables.length(); x++){
                                JSONObject jt = jtables.getJSONObject(x);
                                Table table = new Table(jt.getInt("table_id"), jt.getString("name"),
                                        jt.getDouble("position_h"), jt.getDouble("position_v"),
                                        jt.getDouble("width"), jt.getDouble("height"), jt.getInt("seats"),
                                        jt.getString("active"), jt.getString("state"), floor);
                                tables.add(table);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Log.e("error", "cannot fetch data");
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(!NetworkUtils.isNetworkAvailable(contextpage)){
                Toast.makeText(contextpage, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }else{
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.insertOrUpdate(floors);
                        realm.insertOrUpdate(tables);
                        realm.insertOrUpdate(states);
                    }
                });
            }
        }
    }

    private boolean emailCheck(EditText email){
        if(Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches())
            return true;
        return false;
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
}