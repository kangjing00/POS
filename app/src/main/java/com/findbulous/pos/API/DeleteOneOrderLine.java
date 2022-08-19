package com.findbulous.pos.API;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.WindowManager;

import com.findbulous.pos.Order;
import com.findbulous.pos.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;

import io.realm.Realm;

public class DeleteOneOrderLine extends AsyncTask<String, String, String> {
    private ProgressDialog pd = null;
    private Context contextpage;
    private int local_order_id, order_id, order_line_id;
    private Order updatedOrder, order;
    private Realm realm;

    public DeleteOneOrderLine(Context contextpage, int local_order_id, int order_id, int order_line_id){
       this.contextpage = contextpage;
       this.local_order_id = local_order_id;
       this.order_id = order_id;
       this.order_line_id = order_line_id;
    }

    @Override
    protected void onPreExecute() {
        if (pd == null) {
            pd = createProgressDialog(contextpage);
            pd.show();
        }
        realm = Realm.getDefaultInstance();
        Order result = realm.where(Order.class).equalTo("local_order_id", local_order_id).findFirst();
        order = realm.copyFromRealm(result);
        updatedOrder = null;
    }

    @Override
    protected String doInBackground(String... strings) {
        long timeBefore = Calendar.getInstance().getTimeInMillis();
        String connection_error = "";

        String urlParameters = "&order_id=" + order_id + "&remove_order_line_ids[]=" + order_line_id;

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

                if(status.equalsIgnoreCase("OK")){
                    JSONObject jresult = json.getJSONObject("result");
                    JSONObject jo_order = jresult.getJSONObject("order");

                    //order
                    double tip_amount = 0;
                    boolean is_tipped = false;
                    int partner_id = 0;
                    if(jo_order.getString("tip_amount").length() > 0){
                        tip_amount = jo_order.getDouble("tip_amount");
                    }
                    if(jo_order.getString("is_tipped").length() > 0){
                        is_tipped = jo_order.getBoolean("is_tipped");
                    }
                    if(jo_order.getString("partner_id").length() > 0){
                        partner_id = jo_order.getInt("partner_id");
                    }
                    updatedOrder = new Order(local_order_id, order_id, jo_order.getString("name"),
                            jo_order.getString("date_order"), jo_order.getString("pos_reference"),
                            jo_order.getString("state"), jo_order.getString("state_name"),
                            jo_order.getDouble("amount_tax"), jo_order.getDouble("amount_total"),
                            jo_order.getDouble("amount_paid"), jo_order.getDouble("amount_return"),
                            jo_order.getDouble("amount_subtotal"), tip_amount,
                            jo_order.getString("display_amount_tax"), jo_order.getString("display_amount_total"),
                            jo_order.getString("display_amount_paid"), jo_order.getString("display_amount_return"),
                            jo_order.getString("display_amount_subtotal"), jo_order.getString("display_tip_amount"),
                            is_tipped, order.getTable(), order.getCustomer(), jo_order.getString("note"),
                            jo_order.getDouble("discount"), jo_order.getString("discount_type"),
                            jo_order.getInt("customer_count"), jo_order.getInt("session_id"),
                            jo_order.getInt("user_id"), jo_order.getInt("company_id"),
                            partner_id);

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
        System.out.println("Delete order line time taken: " + (timeAfter - timeBefore) + "ms");
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if(updatedOrder != null)
                    realm.insertOrUpdate(updatedOrder);
            }
        });

        if (pd != null)
            pd.dismiss();
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
