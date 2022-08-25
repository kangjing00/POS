package com.findbulous.pos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.findbulous.pos.Network.NetworkUtils;
import com.findbulous.pos.databinding.ChoosePosPermissionPageBinding;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

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
import io.realm.RealmResults;

public class ChoosePOSPermissionPage extends AppCompatActivity {

    private ChoosePosPermissionPageBinding binding;
//    private SharedPreferences posSharedPreference;
//    // Creating an Editor object to edit(write to the file)
//    private SharedPreferences.Editor posSharedPreferenceEdit;
    //Session Existence
    private boolean openedSessionExist;
    //POS Session and POS Configuration
    private POS_Config pos_config;
    private POS_Session pos_session;
    private Currency currency;
    //Products (including product tax) and Categories
    private ArrayList<POS_Category> product_categories;
    private ArrayList<Product> products;
    private ArrayList<Attribute> attributes;
    private ArrayList<Attribute_Value> attribute_values;
    private ArrayList<Product_Tax> product_taxes;
    //Tables (including table states) and Floors
    private ArrayList<Floor> floors;
    private ArrayList<Table_State> tableStates;
    private ArrayList<Table> tables;
    //Order (including order states)
    private ArrayList<Order> orders;
    private ArrayList<Order_State> orderStates;
    //Order Customer from cloud
    private ArrayList<Customer> customers;
    //Temporarily
//    private boolean finishApiLoad;
    private ProgressDialog pd;

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
        currency = null;
        product_categories = new ArrayList<>();
        products = new ArrayList<>();
        attributes = new ArrayList<>();
        attribute_values = new ArrayList<>();
        product_taxes = new ArrayList<>();
        floors = new ArrayList<>();
        tableStates = new ArrayList<>();
        tables = new ArrayList<>();
        orders = new ArrayList<>();
        orderStates = new ArrayList<>();
        customers = new ArrayList<>();

        //Temporarily
//        finishApiLoad = false;
        pd = null;

        if (!NetworkUtils.isNetworkAvailable(contextpage)) {
            Toast.makeText(contextpage, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }else {
            new getCheckOpenedSession().execute();
            new loadProduct().execute();//<<<<<<<<<<<<<<<<<<<<<<<
            new loadFloorAndTable().execute();//<<<<<<<<<<<<<<<<<<<
            new loadPaymentMethods().execute();
        }

        Customer guestAccInRealm = realm.where(Customer.class).equalTo("customer_id", 0).findFirst();
        if(guestAccInRealm == null) {
            Customer guestAcc = new Customer(0, "Guest", "guestEmail", null, null, null);
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.insertOrUpdate(guestAcc);
                }
            });
        }else{
            System.out.println("guestAccInRealm: " + guestAccInRealm);
        }


        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
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
                    if(!NetworkUtils.isNetworkAvailable(contextpage)){
                        Toast.makeText(contextpage, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    }else {
                        new openNewSession(opening_cash, opening_note_et.getText().toString()).execute();
                    }
                    popup.dismiss();
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
        private boolean no_connection = false;
        private String connection_error = "";

        private boolean sessionExist = false;
        private double cash_opening = 0.0;
        private String openingNotes = "";

        public openNewSession(double cash_opening, String openingNotes){
            this.cash_opening = cash_opening;
            this.openingNotes = openingNotes;
        }

        @Override
        protected void onPreExecute() {
            if(pd == null) {
                pd = createProgressDialog(contextpage);
                pd.show();
            }
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
                        JSONObject jresult = json.getJSONObject("result");
                        JSONObject jpos_session = jresult.getJSONObject("pos_session");
                        JSONObject jpos_config = jresult.getJSONObject("pos_config");
                        JSONObject jcurrency = jresult.getJSONObject("currency");

                        if(jpos_session.getString("state").equalsIgnoreCase("opened")){
                            sessionExist = true;
                        }

                        int start_categ_id = -1;
                        if((!jpos_config.getString("iface_start_categ_id").isEmpty()) &&
                                (jpos_config.getString("iface_start_categ_id") != null)){
                            start_categ_id = jpos_config.getInt("iface_start_categ_id");
                        }

                        boolean product_configurator = false, iface_tipproduct = false,
                                iface_orderline_customer_notes = false;
                        if(jpos_config.getString("product_configurator").length() > 0) {
                            product_configurator = jpos_config.getBoolean("product_configurator");
                        }
                        if(jpos_config.getString("iface_tipproduct").length() > 0){
                            iface_tipproduct = jpos_config.getBoolean("iface_tipproduct");
                        }
                        if(jpos_config.getString("iface_orderline_customer_notes").length() > 0){
                            iface_orderline_customer_notes = jpos_config.getBoolean("iface_orderline_customer_notes");
                        }
                        int tip_product_id = -1;
                        if(jpos_config.getString("tip_product_id").length() > 0){
                            tip_product_id = jpos_config.getInt("tip_product_id");
                        }
                        pos_config = new POS_Config(jpos_config.getInt("id"), jpos_config.getString("name"),
                                jpos_config.getBoolean("is_table_management"), iface_tipproduct,
                                iface_orderline_customer_notes, start_categ_id,
                                jpos_config.getBoolean("iface_orderline_notes"), jpos_config.getBoolean("manual_discount"),
                                product_configurator, jpos_config.getInt("company_id"), tip_product_id);

                        pos_session = new POS_Session(jpos_session.getInt("id"), jpos_session.getString("name"),
                                jpos_session.getString("start_at"), jpos_session.getString("stop_at"),
                                jpos_session.getString("state"), jpos_session.getString("opening_notes"),
                                jpos_session.getInt("login_number"), pos_config, jpos_session.getInt("user_id"));

                        currency = new Currency(jcurrency.getInt("id"), jcurrency.getString("name"),
                                jcurrency.getString("symbol"), jcurrency.getString("full_name"),
                                jcurrency.getDouble("rounding"), jcurrency.getInt("decimal_places"),
                                jcurrency.getBoolean("active"), jcurrency.getString("position"),
                                jcurrency.getString("currency_unit_label"), jcurrency.getString("currency_subunit_label"));
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
            openedSessionExist = sessionExist;
            if(no_connection){
                System.out.println("Connection Error Message: " + connection_error);
                if (pd != null)
                    pd.dismiss();
            }else{
                new loadProduct().execute();
                new loadFloorAndTable().execute();
                new loadPaymentMethods().execute();

                RealmResults<Currency> currencies = realm.where(Currency.class).findAll();

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        if(pos_config != null)
                            realm.insertOrUpdate(pos_config);
                        if(pos_session != null)
                            realm.insertOrUpdate(pos_session);
                        if(currency != null) {
                            currencies.deleteAllFromRealm();
                            realm.insertOrUpdate(currency);
                        }
                    }
                });

                //Go to homepage
                goToHomePage();
            }
        }
    }

    //Retrieve pos session and pos config
    //Y(es) = Continue Session N(o) = Start New Session
    public class getCheckOpenedSession extends AsyncTask<String, String, String> {
        private boolean sessionExist, error;
        private String error_msg;

        @Override
        protected void onPreExecute() {
            if(pd == null) {
                pd = createProgressDialog(contextpage);
                pd.show();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            long timeBefore = Calendar.getInstance().getTimeInMillis();

            error = false;
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
                        JSONObject jcurrency = jresult.getJSONObject("currency");
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

                        boolean product_configurator = false, iface_tipproduct = false,
                                iface_orderline_customer_notes = false;
                        if(jpos_config.getString("product_configurator").length() > 0) {
                            product_configurator = jpos_config.getBoolean("product_configurator");
                        }
                        if(jpos_config.getString("iface_tipproduct").length() > 0){
                            iface_tipproduct = jpos_config.getBoolean("iface_tipproduct");
                        }
                        if(jpos_config.getString("iface_orderline_customer_notes").length() > 0){
                            iface_orderline_customer_notes = jpos_config.getBoolean("iface_orderline_customer_notes");
                        }
                        int tip_product_id = -1;
                        if(jpos_config.getString("tip_product_id").length() > 0){
                            tip_product_id = jpos_config.getInt("tip_product_id");
                        }
                        pos_config = new POS_Config(jpos_config.getInt("id"), jpos_config.getString("name"),
                                jpos_config.getBoolean("is_table_management"), iface_tipproduct,
                                iface_orderline_customer_notes, start_categ_id,
                                jpos_config.getBoolean("iface_orderline_notes"), jpos_config.getBoolean("manual_discount"),
                                product_configurator, jpos_config.getInt("company_id"), tip_product_id);

                        pos_session = new POS_Session(jpos_session.getInt("id"), jpos_session.getString("name"),
                                jpos_session.getString("start_at"), jpos_session.getString("stop_at"),
                                jpos_session.getString("state"), jpos_session.getString("opening_notes"),
                                jpos_session.getInt("login_number"), pos_config, jpos_session.getInt("user_id"));

                        currency = new Currency(jcurrency.getInt("id"), jcurrency.getString("name"),
                                jcurrency.getString("symbol"), jcurrency.getString("full_name"),
                                jcurrency.getDouble("rounding"), jcurrency.getInt("decimal_places"),
                                jcurrency.getBoolean("active"), jcurrency.getString("position"),
                                jcurrency.getString("currency_unit_label"), jcurrency.getString("currency_subunit_label"));
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                    error = true;
                    error_msg = e.toString();
                }
            }catch (IOException e){
                Log.e("error", "cannot fetch data");
                connection_error = e + "";
                error = true;
                error_msg += " " + e;
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
                if (pd != null)
                    pd.dismiss();
            }else{
                if(error){
                  System.out.println("Error Message: " + error_msg);
                }else {
                    openedSessionExist = sessionExist;
                    RealmResults<Currency> currencies = realm.where(Currency.class).findAll();

                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            if(pos_config != null)
                                realm.insertOrUpdate(pos_config);
                            if(pos_session != null)
                                realm.insertOrUpdate(pos_session);
                            if(currency != null) {
                                currencies.deleteAllFromRealm();
                                realm.insertOrUpdate(currency);
                            }
                        }
                    });
                }
            }
        }
    }


    public class loadProduct extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            if(pd == null) {
                pd = createProgressDialog(contextpage);
                pd.show();
            }
        }

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
                                loadProductCategories(jcategories);
                            // Product
                            for (int a = 0; a < jproducts.length(); a++) {
                                JSONObject jo = jproducts.getJSONObject(a);
                                JSONObject jTti_by_amount_type = jo.getJSONObject("total_taxes_incl_by_amount_type");
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

                                JSONArray jAttributes = jo.getJSONArray("attributes");
                                if(jAttributes.length() > 0){
                                    for(int i = 0; i < jAttributes.length(); i++){
                                        JSONObject joAttribute = jAttributes.getJSONObject(i);
                                        JSONArray jAttribute_values = joAttribute.getJSONArray("values");
                                        if(jAttribute_values.length() > 0){
                                            for(int x = 0; x < jAttribute_values.length(); x++){
                                                JSONObject joAttribute_value = jAttribute_values.getJSONObject(x);
                                                boolean is_custom = false;
                                                if(joAttribute_value.getString("is_custom").length() > 0){
                                                    is_custom = joAttribute_value.getBoolean("is_custom");
                                                }
                                                Attribute_Value attribute_value = new Attribute_Value(joAttribute_value.getInt("id"),
                                                        joAttribute_value.getString("name"), joAttribute_value.getString("html_color"),
                                                        joAttribute_value.getInt("sequence"), joAttribute_value.getInt("attribute_id"),
                                                        joAttribute_value.getInt("color"), joAttribute_value.getInt("product_attribute_value_id"),
                                                        joAttribute_value.getInt("attribute_line_id"), joAttribute_value.getInt("product_tmpl_id"),
                                                        joAttribute_value.getInt("product_template_attribute_value_id"), joAttribute_value.getBoolean("ptav_active"),
                                                        is_custom, joAttribute_value.getDouble("price_extra"), joAttribute_value.getString("display_price_extra"));
                                                attribute_values.add(attribute_value);
                                            }
                                            int sequence = 0;
                                            if(joAttribute.getString("sequence").length() > 0){
                                                sequence = joAttribute.getInt("sequence");
                                            }
                                            Attribute attribute = new Attribute(joAttribute.getInt("id"), joAttribute.getString("name"),
                                                    joAttribute.getString("create_variant"), joAttribute.getString("display_type"),
                                                    joAttribute.getString("visibility"), sequence,
                                                    joAttribute.getInt("product_tmpl_id"), joAttribute.getInt("attribute_id"),
                                                    joAttribute.getInt("value_count"), joAttribute.getInt("attribute_line_id"),
                                                    joAttribute.getBoolean("active"));
                                            attributes.add(attribute);
                                        }
                                    }
                                }


                                Product product = new Product(jo.getInt("id"), jo.getInt("product_id"), jo.getInt("product_tmpl_id"),
                                        jo.getString("name"), jo.getString("default_code"), jo.getDouble("list_price"),
                                        jo.getDouble("standard_price"), jo.getDouble("margin"), jo.getDouble("margin_percent"),
                                        jo.getDouble("price_incl_tax"), jo.getDouble("price_excl_tax"),
                                        jTti_by_amount_type.getDouble("fixed"), jTti_by_amount_type.getDouble("percent"),
                                        jTti_by_amount_type.getDouble("division"), jo.getString("display_list_price"),
                                        jo.getString("display_standard_price"), jo.getString("display_margin"),
                                        jo.getString("display_margin_percent"), jo.getString("display_price_incl_tax"),
                                        jo.getString("display_price_excl_tax"), category);

                                // Product Taxes
                                if(jo.getJSONArray("taxes").length() > 0){
                                    jProductTax = jo.getJSONArray("taxes");
                                    for(int x = 0; x < jProductTax.length(); x++){
                                        JSONObject joProductTax = jProductTax.getJSONObject(x);
                                        Product_Tax product_tax = new Product_Tax(joProductTax.getInt("tax_id"), jo.getInt("product_tmpl_id"),
                                                joProductTax.getString("name"), joProductTax.getString("amount_type"),
                                                joProductTax.getDouble("amount"), joProductTax.getDouble("actual_amount"),
                                                joProductTax.getBoolean("include_base_amount"), joProductTax.getBoolean("price_include"),
                                                joProductTax.getString("display_amount"), joProductTax.getString("display_actual_amount"),
                                                product);
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
                if (pd != null)
                    pd.dismiss();
            }else{
                long timeBefore = Calendar.getInstance().getTimeInMillis();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.insertOrUpdate(products);
                        realm.insertOrUpdate(attributes);
                        realm.insertOrUpdate(attribute_values);
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
    private RealmList<POS_Category> loadProductCategories(JSONArray array){
        RealmList<POS_Category> categories_list = new RealmList<>();
        RealmList<POS_Category> sub_categories_list;
        POS_Category product_category;
        int counter = 0;
        while(counter < array.length()){
            try {
                JSONObject data = array.getJSONObject(counter);
                int sequence = -1;
                try{
                    String sequence_string = data.getString("sequence");
                    if(sequence_string.length() > 0){
                        sequence = data.getInt("sequence");
                    }
                }catch (Exception e){
                    System.out.println("Error Message: " + e);
                }
                product_category = new POS_Category(data.getInt("id"), data.getString("name"),
                        sequence, data.getInt("pos_categ_id"), null);

                // Is there any Sub-categories?
                JSONArray ja_sub_categories = data.getJSONArray("pos_categories");
                if (ja_sub_categories.length() > 0) {
                    //Second level category list
                    sub_categories_list = loadProductCategories(ja_sub_categories); //second lvl = third lvl
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
        protected void onPreExecute() {
            if(pd == null) {
                pd = createProgressDialog(contextpage);
                pd.show();
            }
        }

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
                            Table_State tableState = new Table_State((i+1), js.getString("code"), js.getString("name"));
                            tableStates.add(tableState);
                        }

                        for (int a = 0; a < jfloors.length(); a++) {
                            JSONObject jf = jfloors.getJSONObject(a);
                            Floor floor = new Floor(jf.getInt("id"), jf.getString("name"),
                                    jf.getInt("pos_config_id"), jf.getInt("sequence"),
                                    jf.getBoolean("active"), jf.getInt("floor_id"));
                            floors.add(floor);

                            JSONArray jtables = jf.getJSONArray("tables");
                            for(int x = 0; x < jtables.length(); x++){
                                JSONObject jt = jtables.getJSONObject(x);
                                Table table = new Table(jt.getInt("table_id"), jt.getString("name"),
                                        jt.getDouble("position_h"), jt.getDouble("position_v"),
                                        jt.getDouble("width"), jt.getDouble("height"), jt.getInt("seats"),
                                        jt.getBoolean("active"), jt.getString("state"), floor);
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
                if (pd != null)
                    pd.dismiss();
            }else{
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.insertOrUpdate(floors);
                        realm.insertOrUpdate(tables);
                        realm.insertOrUpdate(tableStates);
                    }
                });

                new loadOrder().execute();
            }
        }
    }
//    public class getCustomer extends AsyncTask<String, String, String> {
//        private int customer_id;
//        private Customer customer;
//
//        public getCustomer(int customer_id){
//            this.customer_id = customer_id;
//        }
//
//        public Customer getCustomer(){
//            return customer;
//        }
//
//        @Override
//        protected String doInBackground(String... strings) {
//            String url = "https://www.c3rewards.com/api/merchant/?module=customers";
//            String agent = "c092dc89b7aac085a210824fb57625db";
//            String jsonUrl =url + "&agent=" + agent;
//            jsonUrl = jsonUrl + "&customer_id=" + customer_id;
//            System.out.println(jsonUrl);
//
//            URL obj;
//            try {
//                obj = new URL(jsonUrl);
//                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//                // optional default is GET
//                con.setRequestMethod("GET");
//                //add request header
//                int responseCode = con.getResponseCode();
//                System.out.println("\nSending 'GET' request to URL : " + jsonUrl);
//                System.out.println("Response Code : " + responseCode);
//
//                BufferedReader in = new BufferedReader(
//                        new InputStreamReader(con.getInputStream()));
//                String inputLine;
//
//                StringBuilder response = new StringBuilder();
//
//                while ((inputLine = in.readLine()) != null) {
//                    response.append(inputLine);
//                }
//                in.close();
//
//                System.out.println(response);
//                String data = response.toString();
//                try {
//                    JSONObject json = new JSONObject(data);
//                    String status = json.getString("status");
//
//                    if (status.equals("OK")) {
//                        JSONObject jresult = json.getJSONObject("result");
//                        JSONObject jcustomer = jresult.getJSONObject("customer");
//
//                        customer = new Customer(Integer.valueOf(jcustomer.getString("customer_id")),
//                                jcustomer.getString("full_name"), jcustomer.getString("email"),
//                                jcustomer.getString("tel_no"), jcustomer.getString("ic_no"), jcustomer.getString("date_birth"));
//
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            } catch (IOException e) {
//                Log.e("error", "cannot fetch data");
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            if(!NetworkUtils.isNetworkAvailable(contextpage)){
//                Toast.makeText(contextpage, "Internet Connection Lost", Toast.LENGTH_SHORT).show();
//            }else{
//                realm.executeTransaction(new Realm.Transaction() {
//                    @Override
//                    public void execute(Realm realm) {
//                        realm.insertOrUpdate(customer);
//                    }
//                });
//            }
//        }
//    }
    public class loadOrder extends AsyncTask<String, String, String>{
        private Customer guestAcc;
        private ArrayList<Order> orderInRealm;

        @Override
        protected void onPreExecute(){
            if(pd == null) {
                pd = createProgressDialog(contextpage);
                pd.show();
            }
            RealmResults<Order> results = realm.where(Order.class).findAll();
            orderInRealm = new ArrayList<>();
            orderInRealm.addAll(realm.copyFromRealm(results));

            guestAcc = realm.where(Customer.class).equalTo("customer_id", 0).findFirst();
        }

        @Override
        protected String doInBackground(String... strings) {
            long timeBefore = Calendar.getInstance().getTimeInMillis();

            String url = "https://www.c3rewards.com/api/merchant/?module=pos&action=orders";
            String agent = "c092dc89b7aac085a210824fb57625db";
            String jsonUrl = url + "&agent=" + agent;
            System.out.println(jsonUrl);

            int page = 1, orderCounter = 0;
            while(page > 0) {
                String jsonUrlPage = jsonUrl + "&page=" + page;
                URL obj;
                try {
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

                    try {
                        JSONObject json = new JSONObject(data);
                        String status = json.getString("status");

                        if (status.equals("OK")) {
                            JSONObject jresult = json.getJSONObject("result");
                            JSONArray jstates = jresult.getJSONArray("states");
                            JSONArray jorders = jresult.getJSONArray("orders");

                            if((jorders.length() != 0)){ //|| (jcategories.length() != 0)
                                page++;
                            }else{
                                page = -1;
                            }

                            //Order States
                            if(page == 2) {
                                for (int i = 0; i < jstates.length(); i++) {
                                    JSONObject jo = jstates.getJSONObject(i);
                                    Order_State orderState = new Order_State((i + 1), jo.getString("code"), jo.getString("name"));
                                    orderStates.add(orderState);
                                }
                            }

                            //Orders
                            for (int i = 0; i < jorders.length(); i++) {
                                JSONObject jo = jorders.getJSONObject(i);
                                Table table = null;
                                Customer customer = null;
                                if(jo.getString("table_id").length() > 0){
                                    for(int x = 0; x < tables.size(); x++){
                                        if(tables.get(x).getTable_id() == jo.getInt("table_id")){
                                            table = tables.get(x);
                                        }
                                    }
                                }
                                int partner_id = -1;
                                if(jo.getString("partner_id").length() > 0){
                                    partner_id = jo.getInt("partner_id");
                                }

                                if(jo.getString("c3_customer_id").length() > 0){
                                    customer = getCustomer(jo.getInt("c3_customer_id"));
                                }else{
                                    customer = guestAcc;
                                }

                                double discount = 0.0;
                                if(jo.getString("discount").length() > 0){
                                    discount = jo.getDouble("discount");
                                }
                                String discount_type = null;
                                if(jo.getString("discount_type").length() > 0){
                                    discount_type = jo.getString("discount_type");
                                }

                                double tip_amount = 0;
                                boolean is_tipped = false;
                                if(jo.getString("tip_amount").length() > 0){
                                    tip_amount = jo.getDouble("tip_amount");
                                }
                                if(jo.getString("is_tipped").length() > 0){
                                    is_tipped = jo.getBoolean("is_tipped");
                                }

                                int order_id = jo.getInt("order_id");
                                int local_order_id = 1;
                                if(orderInRealm.size() > 0){
                                    local_order_id = orderInRealm.get(orderInRealm.size() - 1).getLocal_order_id() + 1;
                                }
                                boolean found = false;
                                for(int y = 0; y < orderInRealm.size(); y++){
                                    if(order_id == orderInRealm.get(y).getOrder_id()){
                                        local_order_id = orderInRealm.get(y).getLocal_order_id();
                                        found = true;
                                    }
                                }
                                int customer_count = 1;
                                if(jo.getString("customer_count").length() > 0){
                                    customer_count = jo.getInt("customer_count");
                                }
                                Order order = new Order(local_order_id, order_id, jo.getString("name"), jo.getString("date_order"),
                                        jo.getString("pos_reference"), jo.getString("state"), jo.getString("state_name"),
                                        jo.getDouble("amount_tax"), jo.getDouble("amount_total"), jo.getDouble("amount_paid"),
                                        jo.getDouble("amount_return"), jo.getDouble("amount_subtotal"), tip_amount,
                                        jo.getString("display_amount_tax"), jo.getString("display_amount_total"),
                                        jo.getString("display_amount_paid"), jo.getString("display_amount_return"),
                                        jo.getString("display_amount_subtotal"), jo.getString("display_tip_amount"),
                                        is_tipped, table, customer, jo.getString("note"), discount, discount_type, customer_count,
                                        pos_session.getId(), pos_session.getUser_id(), pos_config.getCompany_id(), partner_id);

                                if(found == false){ //not found in local db = new order from cloud db
                                    orderInRealm.add(order);
                                }

                                if(customer != null)
                                    customers.add(customer);
                                orders.add(order);
                                orderCounter++;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    page = -1;
                    Log.e("error", "cannot fetch data");
                }
            }
            System.out.println("Order Counter: " + orderCounter);
            long timeAfter = Calendar.getInstance().getTimeInMillis();
            System.out.println("Load order time: " + (timeAfter - timeBefore) + "ms");

            return null;
        }

        private Customer getCustomer(int customer_id){
            Customer customer = null;
            String url = "https://www.c3rewards.com/api/merchant/?module=customers";
            String agent = "c092dc89b7aac085a210824fb57625db";
            String jsonUrl =url + "&agent=" + agent;
            jsonUrl = jsonUrl + "&customer_id=" + customer_id;
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
                        JSONObject jcustomer = jresult.getJSONObject("customer");

                        customer = new Customer(Integer.valueOf(jcustomer.getString("customer_id")),
                                jcustomer.getString("full_name"), jcustomer.getString("email"),
                                jcustomer.getString("tel_no"), jcustomer.getString("ic_no"), jcustomer.getString("date_birth"));

                    }else if (status.equals("ERR")){
                        customer = guestAcc;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Log.e("error", "cannot fetch data");
            }

            return customer;
        }

        @Override
        protected void onPostExecute(String s){
            if(!NetworkUtils.isNetworkAvailable(contextpage)){
                Toast.makeText(contextpage, "No Internet Connection", Toast.LENGTH_SHORT).show();
                if (pd != null)
                    pd.dismiss();
            }else {
                long timeBefore = Calendar.getInstance().getTimeInMillis();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.insertOrUpdate(orderStates);
                        realm.insertOrUpdate(customers);
                        realm.insertOrUpdate(orders);
                    }
                });
                long timeAfter = Calendar.getInstance().getTimeInMillis();
                System.out.println("Update orders to realm time: " + (timeAfter - timeBefore) + "ms");
            }
        }
    }

    public class loadPaymentMethods extends AsyncTask<String, String, String>{
        private ArrayList<Payment_Method> payment_methods;

        @Override
        protected void onPreExecute(){
            if(pd == null) {
                pd = createProgressDialog(contextpage);
                pd.show();
            }
            payment_methods = new ArrayList<>();
        }

        @Override
        protected String doInBackground(String... strings) {
            long timeBefore = Calendar.getInstance().getTimeInMillis();

            String url = "https://www.c3rewards.com/api/merchant/?module=pos&action=payment_methods";
            String agent = "c092dc89b7aac085a210824fb57625db";
            String jsonUrl = url + "&agent=" + agent;
            System.out.println(jsonUrl);


            String jsonUrlPage = jsonUrl;
            URL obj;
            try {
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

                try {
                        JSONObject json = new JSONObject(data);
                        String status = json.getString("status");

                        if (status.equals("OK")) {
                            JSONObject jresult = json.getJSONObject("result");
                            JSONArray jpaymentMethods = jresult.getJSONArray("payment_methods");

                            //Orders
                            for (int i = 0; i < jpaymentMethods.length(); i++) {
                                JSONObject jo = jpaymentMethods.getJSONObject(i);

                                Payment_Method paymentMethod = new Payment_Method(jo.getInt("id"),
                                    jo.getString("name"), jo.getBoolean("is_cash_count"),
                                    jo.getBoolean("split_transactions"), jo.getBoolean("active"),
                                    jo.getInt("payment_method_id"));

                                payment_methods.add(paymentMethod);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
            } catch (IOException e) {
                Log.e("error", "cannot fetch data");
            }

            long timeAfter = Calendar.getInstance().getTimeInMillis();
            System.out.println("Load payment methods time: " + (timeAfter - timeBefore) + "ms");

            return null;
        }

        @Override
        protected void onPostExecute(String s){
            if(!NetworkUtils.isNetworkAvailable(contextpage)){
                Toast.makeText(contextpage, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }else {
                long timeBefore = Calendar.getInstance().getTimeInMillis();
                RealmResults<Payment_Method> results = realm.where(Payment_Method.class).findAll();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        results.deleteAllFromRealm();
                        realm.insertOrUpdate(payment_methods);
                    }
                });
                long timeAfter = Calendar.getInstance().getTimeInMillis();
                System.out.println("Load payment methods to realm time: " + (timeAfter - timeBefore) + "ms");
            }
            if (pd != null)
                pd.dismiss();
        }
    }

    public static ProgressDialog createProgressDialog(Context mContext) {
        ProgressDialog dialog = new ProgressDialog(mContext);
        try {
            dialog.show();
        } catch (WindowManager.BadTokenException e) {

        }
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.progress_dialog);
        // dialog.setMessage(Message);
        return dialog;
    }
}