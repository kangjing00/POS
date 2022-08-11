package com.findbulous.pos.API;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.findbulous.pos.Attribute_Value;
import com.findbulous.pos.Customer;
import com.findbulous.pos.Network.NetworkUtils;
import com.findbulous.pos.Order;
import com.findbulous.pos.Order_Line;
import com.findbulous.pos.Product;
import com.findbulous.pos.Product_Tax;
import com.findbulous.pos.R;
import com.findbulous.pos.Table;
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
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class UpdateOneOrderLineQtyDisc extends AsyncTask<String, String, String> {
    private ProgressDialog pd = null;
    private Context contextpage;
    private int local_order_id, order_id, local_order_line_id, order_line_id, qty;
    private String discount_type;
    private double discount;

    private Order update_order;
    private Table table;
    private Customer customer;
    private Order_Line update_order_line;
    private Product product;
    private Order order_line_order;
    private RealmList<Attribute_Value> attribute_values;
    private ArrayList<Product_Tax> product_taxes;

    private Realm realm;

    public UpdateOneOrderLineQtyDisc(Context contextpage, int local_order_id, int order_id,
                                     int local_order_line_id, int order_line_id, int qty,
                                     String discount_type, double discount){
        this.contextpage = contextpage;
        this.local_order_id = local_order_id;
        this.order_id = order_id;
        this.local_order_line_id = local_order_line_id;
        this.order_line_id = order_line_id;
        this.qty = qty;
        this.discount_type = discount_type;
        this.discount = discount;
    }

    @Override
    protected void onPreExecute() {
        if (pd == null) {
            pd = createProgressDialog(contextpage);
            pd.show();
        }
        realm = Realm.getDefaultInstance();

        Order order = realm.where(Order.class).equalTo("local_order_id", local_order_id).findFirst();
        if(order.getTable() != null) {
            table = realm.copyFromRealm(order.getTable());
        }else{
            table = null;
        }
        if(order.getCustomer() != null) {
            customer = realm.copyFromRealm(order.getCustomer());
        }else{
            customer = null;
        }
        update_order = null;

        Order_Line order_line = realm.where(Order_Line.class).equalTo("local_order_line_id", local_order_line_id)
                .and().equalTo("order_line_id", order_line_id).findFirst();
        product = realm.copyFromRealm(order_line.getProduct());
        order_line_order = realm.copyFromRealm(order_line.getOrder());
        RealmResults<Product_Tax> product_tax_results = realm.where(Product_Tax.class)
                .equalTo("product_tmpl_id", product.getProduct_tmpl_id()).findAll();
        product_taxes = new ArrayList<>();
        product_taxes.addAll(realm.copyFromRealm(product_tax_results));
        if(attribute_values != null) {
            attribute_values = (RealmList<Attribute_Value>) realm.copyFromRealm(order_line.getAttribute_values());
        }else{
            attribute_values = null;
        }
        update_order_line = null;
    }

    @Override
    protected String doInBackground(String... strings) {
        long timeBefore = Calendar.getInstance().getTimeInMillis();
        String connection_error = "";

        String urlParameters = "&order_id=" + order_id + "&products[0][order_line_id]=" + order_line_id;
        if(qty != -1){
            urlParameters += "&products[0][qty]=" + qty;
        }
        if((discount_type != null) && (discount > 0)){
            urlParameters += "&products[0][discount_type]=" + discount_type
                    + "&products[0][discount]=" + discount;
        }

        //Testing (check error)
//            urlParameters += "&dev=1";

        byte[] postData = urlParameters.getBytes(Charset.forName("UTF-8"));
        int postDataLength = postData.length;

        String url = "https://www.c3rewards.com/api/merchant/?module=pos&action=save_order";
        String agent = "c092dc89b7aac085a210824fb57625db";
        String jsonUrl =url + "&agent=" + agent;
        System.out.println(jsonUrl);

        URL obj;
        try{
            obj = new URL(jsonUrl);
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
            System.out.println("\nSending 'POST' request to URL : " + jsonUrl);
            System.out.println("Post parameters : " + urlParameters);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement je = JsonParser.parseString(String.valueOf(response));
            String prettyJsonString = gson.toJson(je);
            System.out.println(prettyJsonString);
            System.out.println("Post parameters : " + urlParameters);
            System.out.println(response);
            String data = response.toString();
            try{
                JSONObject json = new JSONObject(data);
                String status = json.getString("status");

                if (status.equals("OK")) {
                    JSONObject jresult = json.getJSONObject("result");
                    JSONObject jo_order = jresult.getJSONObject("order");
                    JSONArray ja_order_line = jo_order.getJSONArray("order_lines");

                    //order
                    double tip_amount = 0;
                    boolean is_tipped = false;
                    int partner_id = 0;
                    String discount_type = null;
                    if (jo_order.getString("tip_amount").length() > 0) {
                        tip_amount = jo_order.getDouble("tip_amount");
                    }
                    if (jo_order.getString("is_tipped").length() > 0) {
                        is_tipped = jo_order.getBoolean("is_tipped");
                    }
                    if (jo_order.getString("partner_id").length() > 0) {
                        partner_id = jo_order.getInt("partner_id");
                    }
                    if (jo_order.getString("discount_type").length() > 0) {
                        discount_type = jo_order.getString("discount_type");
                    }
                    update_order = new Order(local_order_id, jo_order.getInt("order_id"), jo_order.getString("name"),
                            jo_order.getString("date_order"), jo_order.getString("pos_reference"),
                            jo_order.getString("state"), jo_order.getString("state_name"),
                            jo_order.getDouble("amount_tax"), jo_order.getDouble("amount_total"),
                            jo_order.getDouble("amount_paid"), jo_order.getDouble("amount_return"),
                            jo_order.getDouble("amount_subtotal"), tip_amount, is_tipped,
                            table, customer, jo_order.getString("note"),
                            jo_order.getDouble("discount"), discount_type,
                            jo_order.getInt("customer_count"), jo_order.getInt("session_id"),
                            jo_order.getInt("user_id"), jo_order.getInt("company_id"),
                            partner_id);

                    //order_line created
                    for (int i = 0; i < ja_order_line.length(); i++) {
                        JSONObject jo_order_line = ja_order_line.getJSONObject(i);

                        if(jo_order_line.getInt("order_line_id") == order_line_id){
                            int qty = jo_order_line.getInt("qty");
                            double price_unit = jo_order_line.getDouble("price_unit");

                            double price_unit_excl_tax = calculate_price_unit_excl_tax(product, price_unit);
                            double price_subtotal_wo_discount = price_unit_excl_tax * qty;
                            double price_subtotal_incl_wo_discount = calculate_price_subtotal_incl(product_taxes, price_subtotal_wo_discount);
                            double price_before_discount = price_subtotal_incl_wo_discount;

                            double total_cost = 0.0;
                            if (jo_order_line.getString("total_cost").length() > 0) {
                                total_cost = jo_order_line.getDouble("total_cost");
                            }
                            double price_extra = 0.0;
                            if (jo_order_line.getString("price_extra").length() > 0) {
                                price_extra = jo_order_line.getDouble("price_extra");
                            }

                            update_order_line = new Order_Line(local_order_line_id, order_line_id, jo_order_line.getString("name"),
                                qty, price_unit, jo_order_line.getDouble("price_subtotal"),
                                jo_order_line.getDouble("price_subtotal_incl"), price_before_discount,
                                jo_order_line.getString("display_price_unit"), jo_order_line.getString("display_price_subtotal"),
                                jo_order_line.getString("display_price_subtotal_incl"), String.format("$ .2f", price_before_discount),
                                jo_order_line.getString("full_product_name"), jo_order_line.getString("customer_note"),
                                jo_order_line.getString("discount_type"), jo_order_line.getDouble("discount"),
                                jo_order_line.getString("display_discount"), total_cost,
                                jo_order_line.getString("display_total_cost"), price_extra,
                                jo_order_line.getString("display_price_extra"), order_line_order, product, attribute_values);
                        }
                    }
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }catch (IOException e){
            Log.e("error", "cannot fetch data");
            connection_error = e.getMessage() + "";
            System.out.println(connection_error);
        }

        long timeAfter = Calendar.getInstance().getTimeInMillis();
        System.out.println("Update order line time taken: " + (timeAfter - timeBefore) + "ms");
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
                    realm.insertOrUpdate(update_order_line);
                    realm.insertOrUpdate(update_order);
                }
            });
        }

        if (pd != null)
            pd.dismiss();
    }

    private double calculate_price_unit_excl_tax(Product product, double price_unit){
        double fixed = product.getAmount_tax_incl_fixed(),
                percent = product.getAmount_tax_incl_percent(),
                division = product.getAmount_tax_incl_division();

        double price_unit_excl_tax = ((price_unit  - fixed) / (1 + (percent / 100))) * (1 - (division / 100));

        return price_unit_excl_tax;
    }
    private double calculate_price_subtotal_incl(ArrayList<Product_Tax> product_taxes, double price_subtotal){
        double price_subtotal_incl;
        double total_taxes = 0.0, price = price_subtotal;
        double tax = 0.0;

        for(int i = 0; i < product_taxes.size(); i++){
            Product_Tax product_tax = product_taxes.get(i);
            if(!product_tax.isPrice_included()) {
                if (product_tax.getAmount_type().equalsIgnoreCase("fixed")) {
                    tax = product_tax.getAmount();
                } else if (product_tax.getAmount_type().equalsIgnoreCase("percent")) {
                    tax = (price * (product_tax.getAmount() / 100));
                } else if (product_tax.getAmount_type().equalsIgnoreCase("division")) {
                    tax = ((price / (1 - (product_tax.getAmount() / 100))) - price);
                }

                if (product_tax.isInclude_base_amount()) {    //TRUE
                    price += tax;
                }

                total_taxes += tax;
            }
        }
        price_subtotal_incl = price_subtotal + total_taxes;

        return price_subtotal_incl;
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
