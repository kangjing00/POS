package com.findbulous.pos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.Toast;

import com.findbulous.pos.Network.NetworkUtils;
import com.findbulous.pos.databinding.ChoosePosPermissionPageBinding;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

import io.realm.Realm;
import io.realm.RealmList;

public class ChoosePOSPermissionPage extends AppCompatActivity {

    private ChoosePosPermissionPageBinding binding;
//    private SharedPreferences posSharedPreference;
//    // Creating an Editor object to edit(write to the file)
//    private SharedPreferences.Editor posSharedPreferenceEdit;
    private boolean openedSessionExist;

    private POS_Config pos_config;
    private POS_Session pos_session;

    private ArrayList<POS_Category> product_categories;
    private ArrayList<Product> products;
    private ArrayList<Product_Tax> product_taxes;

    private ArrayList<Floor> floors;
    private ArrayList<State> states;
    private ArrayList<Table> tables;

    private Realm realm;
    private Context contextpage;
    private String statuslogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextpage = ChoosePOSPermissionPage.this;
        binding = DataBindingUtil.setContentView(this, R.layout.choose_pos_permission_page);

        realm = Realm.getDefaultInstance();
//        //Cart Settings
//        posSharedPreference = getSharedPreferences("CurrentPOS",MODE_MULTI_PROCESS);
//        posSharedPreferenceEdit = posSharedPreference.edit();

        openedSessionExist = false;
        pos_config = null;
        pos_session = null;
        product_categories = new ArrayList<>();
        products = new ArrayList<>();
        product_taxes = new ArrayList<>();
        floors = new ArrayList<>();
        states = new ArrayList<>();
        tables = new ArrayList<>();

        new getCheckOpenedSession().execute();
        //new loadProduct().execute();//<<<<<<<<<<<<<<<<<<<<<<<
        //new loadFloorAndTable().execute();//<<<<<<<<<<<<<<<<<<<


        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        Date todayDate = new Date();
        binding.todayDate.setText(dateFormatter.format(todayDate));

        binding.orderOnlyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToHomePage();
            }
        });
        binding.cashierBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToHomePage();
            }
        });
        binding.kdsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contextpage, LoginPage.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void goToHomePage(){
        if(openedSessionExist){
            Toast.makeText(contextpage, "Session Exist, Action: Continue Session", Toast.LENGTH_SHORT).show();
            System.out.println("Session Exist, Action: Continue Session");
            Intent intent = new Intent(contextpage, HomePage.class);
            startActivity(intent);
            finish();
        }else{
            showOpeningCashControlPopup();
            System.out.println("Session Does Not Exist, Action: Start New Session");
            Toast.makeText(contextpage, "Session Does Not Exist, Action: Start New Session", Toast.LENGTH_SHORT).show();
        }
//        if(pos_config.isIs_table_management()){
//            new loadFloorAndTable().execute();
//        }
    }

    private void showOpeningCashControlPopup(){
        PopupWindow popup = new PopupWindow(contextpage);
        View layout = getLayoutInflater().inflate(R.layout.opening_cash_control_popup, null);
        popup.setContentView(layout);
        // Set content width and height
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        // Show anchored to button
        popup.setElevation(8);
        popup.setBackgroundDrawable(null);

        popup.showAtLocation(binding.getRoot(), Gravity.CENTER, 0, 0);
        //blur background
        View container = (View) popup.getContentView().getParent();
        WindowManager wm = (WindowManager) ChoosePOSPermissionPage.this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);

        MaterialButton openSessionBtn = layout.findViewById(R.id.open_session_btn);
        EditText opening_cash_et = layout.findViewById(R.id.opening_cash_et);
        EditText opening_note_et = layout.findViewById(R.id.opening_note_et);

        openSessionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateOpeningCash(opening_cash_et.getText().toString())){
                    double opening_cash = Double.valueOf(opening_cash_et.getText().toString());
                    new openNewSession(opening_cash, opening_note_et.getText().toString()).execute();
                    new loadProduct().execute();
                }else{
                    Toast.makeText(contextpage, "Please provide a valid opening cash amount", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateOpeningCash(String opening_cash){
        if((!opening_cash.isEmpty()) && (opening_cash != null) && (opening_cash != "") && (opening_cash != ".")){
            return true;
        }

        return false;
    }

    public class openNewSession extends AsyncTask<String, String, String> {
        boolean no_connection = false;
        String connection_error = "";

        double cash_opening = 0.0;
        String openingNotes = "";

        public openNewSession(double cash_opening, String openingNotes){
            this.cash_opening = cash_opening;
            this.openingNotes = openingNotes;
        }

        @Override
        protected String doInBackground(String... strings) {

            String urlParameters = "&opening_notes=" + openingNotes;
            byte[] postData = urlParameters.getBytes(Charset.forName("UTF-8"));
            int postDataLength = postData.length;

            String url = "https://www.c3rewards.com/api/merchant/?module=pos&action=start_session";
            String agent = "c092dc89b7aac085a210824fb57625db";
            url += "&agent=" + agent;

            URL obj;
            try{
                obj = new URL(url);
                HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setRequestProperty("charset", "utf-8");
                con.setRequestProperty("Content-Length", Integer.toString(postDataLength));

                // Send post request
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'POST' request to URL : " + url);
                System.out.println("Post parameters : " + urlParameters);
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                System.out.println("RESPONSE UPDATE : " + response.toString());
                // {"status":"OK","message":"Success","result":[...]}
                try {
                    JSONObject json = new JSONObject(response.toString());
                    String status = json.getString("status");

                    if (status.equals("OK"))
                    {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Log.e("error", "cannot fetch data 1" + e);

                no_connection = true;
                connection_error = e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(no_connection){
                System.out.println("Connection Error Message: " + connection_error);
            }else{

            }
        }
    }

    //Retrieve pos session and pos config
    //Y(es) = Continue Session N(o) = Start New Session
    public class getCheckOpenedSession extends AsyncTask<String, String, String> {
        private boolean sessionExist;

        @Override
        protected String doInBackground(String... strings) {
            long timeBefore = Calendar.getInstance().getTimeInMillis();

            sessionExist = false;

            String connection_error = "";
            String url = "https://www.c3rewards.com/api/merchant/?module=pos&action=check_opened_session";
            String agent = "c092dc89b7aac085a210824fb57625db";
            String jsonUrl =url + "&agent=" + agent;
            System.out.println(jsonUrl);

            URL obj;
            try{
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
                try{
                    JSONObject json = new JSONObject(data);
                    String status = json.getString("status");

                    if (status.equals("OK")) {
                        JSONObject jresult = json.getJSONObject("result");
                        JSONObject jpos_session = jresult.getJSONObject("pos_session");
                        JSONObject jpos_config = jresult.getJSONObject("pos_config");
                        String session_exist = jresult.getString("opened_session_exist");

                        if(session_exist.equalsIgnoreCase("Y")){
                            sessionExist = true;
                        }else{ //else if(session_exist.equalsIgnoreCase("N"))
                            sessionExist = false;
                        }

                        int start_categ_id = -1;
                        if((!jpos_config.getString("iface_start_categ_id").isEmpty()) &&
                                (jpos_config.getString("iface_start_categ_id") != null)){
                            start_categ_id = jpos_config.getInt("iface_start_categ_id");
                        }

                        pos_config = new POS_Config(jpos_config.getInt("id"), jpos_config.getString("name"),
                                jpos_config.getBoolean("is_table_management"), jpos_config.getBoolean("iface_tipproduct"),
                                jpos_config.getBoolean("iface_orderline_customer_notes"), start_categ_id,
                                jpos_config.getBoolean("iface_orderline_notes"), jpos_config.getBoolean("manual_discount"));

                        pos_session = new POS_Session(jpos_session.getInt("id"), jpos_session.getString("name"),
                                jpos_session.getString("start_at"), jpos_session.getString("state"), pos_config);

                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }catch (IOException e){
                Log.e("error", "cannot fetch data");
                connection_error = e + "";
                System.out.println(connection_error);
            }

            long timeAfter = Calendar.getInstance().getTimeInMillis();
            System.out.println("Get Opened Session Exist time: " + (timeAfter - timeBefore) + "ms");
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(!NetworkUtils.isNetworkAvailable(contextpage)){
                Toast.makeText(contextpage, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }else{
                openedSessionExist = sessionExist;
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.insertOrUpdate(pos_config);
                        realm.insertOrUpdate(pos_session);
                    }
                });
//                long timeBefore = Calendar.getInstance().getTimeInMillis();
//
//
//
//                long timeAfter = Calendar.getInstance().getTimeInMillis();
//                System.out.println("Get Opened Session Exist time: " + (timeAfter - timeBefore) + "ms");
            }
        }
    }


    public class loadProduct extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... strings) {
            long timeBefore = Calendar.getInstance().getTimeInMillis();

            String connection_error = "";
            String url = "https://www.c3rewards.com/api/merchant/?module=pos&action=products";
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

                            if((jproducts.length() != 0)){ //|| (jcategories.length() != 0)
                                page++;
                            }else{
                                page = -1;
                            }
                            // Product Category
                            if(page == 2)
                                loadProductCategories(jcategories, true);
                            // Product
                            for (int a = 0; a < jproducts.length(); a++) {
                                JSONObject jo = jproducts.getJSONObject(a);
                                JSONArray jProductTax = null;
                                int category_id = -1;
                                if(!(jo.getString("pos_categ_id").isEmpty())){
                                    category_id = jo.getInt("pos_categ_id");
                                }
                                POS_Category category = null;
                                for(int i = 0; i < product_categories.size(); i++){
                                    if(product_categories.get(i).getPos_categ_id() == category_id){
                                        category = product_categories.get(i);
                                    }
                                }

                                Product product = new Product(jo.getInt("product_id"), jo.getString("name"),
                                        jo.getString("default_code"), jo.getDouble("list_price"), jo.getDouble("standard_price"),
                                        jo.getDouble("margin"), jo.getDouble("margin_percent"),jo.getDouble("price_incl_tax"),
                                        jo.getDouble("price_excl_tax"), jo.getString("display_list_price"),jo.getString("display_standard_price"),
                                        jo.getString("display_margin"), jo.getString("display_margin_percent"), jo.getString("display_price_incl_tax"),
                                        jo.getString("display_price_excl_tax"), category);

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
            long timeAfter = Calendar.getInstance().getTimeInMillis();
            System.out.println("Load product & category time: " + (timeAfter - timeBefore) + "ms");
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(!NetworkUtils.isNetworkAvailable(contextpage)){
                Toast.makeText(contextpage, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }else{
                long timeBefore = Calendar.getInstance().getTimeInMillis();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.insertOrUpdate(products);
                        realm.insertOrUpdate(product_categories);
                        realm.insertOrUpdate(product_taxes);
                    }
                });
                long timeAfter = Calendar.getInstance().getTimeInMillis();
                System.out.println("Update product & category to realm time: " + (timeAfter - timeBefore) + "ms");
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

                System.out.println("p1111: " + product_category.getPos_categ_id());
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
            String url = "https://www.c3rewards.com/api/merchant/?module=pos&action=restaurant_floors";
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
}