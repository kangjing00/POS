package com.findbulous.pos.API;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.WindowManager;

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

public class DeleteOneDraftOrder extends AsyncTask<String, String, String>{
    private ProgressDialog pd = null;
    private Context contextpage;
    private int order_id;

    public DeleteOneDraftOrder(Context contextpage, int order_id){
        this.contextpage = contextpage;
        this.order_id = order_id;
    }

    @Override
    protected void onPreExecute() {
        if (pd == null) {
            pd = createProgressDialog(contextpage);
            pd.show();
        }
    }

    @Override
    protected String doInBackground(String... strings) {
            long timeBefore = Calendar.getInstance().getTimeInMillis();
            String connection_error = "";

            String urlParameters = "&order_ids[]=" + order_id;

            //Testing (check error)
//            urlParameters += "&dev=1";

            byte[] postData = urlParameters.getBytes(Charset.forName("UTF-8"));
            int postDataLength = postData.length;

            String url = "https://www.c3rewards.com/api/merchant/?module=pos&action=delete_orders";
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
//                String data = response.toString();
//                try{
//                    JSONObject json = new JSONObject(data);
//                    String status = json.getString("status");
//
//                }catch (JSONException e) {
//                    e.printStackTrace();
//                }
            }catch (IOException e){
                Log.e("error", "cannot fetch data");
                connection_error = e.getMessage() + "";
                System.out.println(connection_error);
            }

            long timeAfter = Calendar.getInstance().getTimeInMillis();
            System.out.println("Delete order time taken: " + (timeAfter - timeBefore) + "ms");
            return null;
        }

    @Override
    protected void onPostExecute(String s) {
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
