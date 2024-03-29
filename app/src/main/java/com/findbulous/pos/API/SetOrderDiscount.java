package com.findbulous.pos.API;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.findbulous.pos.Network.NetworkUtils;
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

public class SetOrderDiscount extends AsyncTask<String, String, String> {
    private ProgressDialog pd = null;
    private Context contextpage;
    private Realm realm;
    private int order_id, local_order_id;
    private String discount_type;
    private double discount;

    private Order update_order;

    public SetOrderDiscount(Context contextpage, int order_id, int local_order_id, String discount_type, double discount){
        this.contextpage = contextpage;
        this.order_id = order_id;
        this.local_order_id = local_order_id;
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

        Order result = realm.where(Order.class).equalTo("local_order_id", local_order_id).findFirst();
        if(result != null)
            update_order = realm.copyFromRealm(result);
        else
            update_order = null;
    }

    @Override
    protected String doInBackground(String... strings) {
        long timeBefore = Calendar.getInstance().getTimeInMillis();
        String connection_error = "";

        String urlParameters = "&order_id=" + order_id;
        if(discount_type != null){
            urlParameters += "&discount_type=" + discount_type + "&discount=" + discount;
        }else{
            urlParameters += "&discount_type=&discount=0";
        }

        //Testing (check error)
//        urlParameters += "&dev=1";

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

                    //order
                    if(update_order != null) {
                        update_order.setDiscount_type(jo_order.getString("discount_type"));
                        update_order.setDiscount(jo_order.getDouble("discount"));
                        update_order.setAmount_total(jo_order.getDouble("amount_total"));
                        update_order.setDisplay_amount_total(jo_order.getString("display_amount_total"));
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
        System.out.println("Set draft order's discount time taken: " + (timeAfter - timeBefore) + "ms");
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        if(!NetworkUtils.isNetworkAvailable(contextpage)){
            Toast.makeText(contextpage, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }else{
            if(update_order != null) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.insertOrUpdate(update_order);
                    }
                });
            }
        }

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
