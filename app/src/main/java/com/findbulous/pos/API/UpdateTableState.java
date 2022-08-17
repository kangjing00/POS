package com.findbulous.pos.API;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.WindowManager;

import com.findbulous.pos.R;
import com.findbulous.pos.Table;

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

public class UpdateTableState extends AsyncTask<String, String, String> {
    private boolean no_connection = false;
    private String connection_error = "";
    private ProgressDialog pd = null;
    private Context contextpage;
    private Table table;

    public UpdateTableState(Context contextpage, Table table){
        this.contextpage = contextpage;
        this.table = table;
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
        long timeBefore = Calendar.getInstance().getTimeInMillis();

        String urlParameters = "&states[" + table.getTable_id() + "]=" + table.getState();

        byte[] postData = urlParameters.getBytes(Charset.forName("UTF-8"));
        int postDataLength = postData.length;

        String agent = "c092dc89b7aac085a210824fb57625db";
        String url = "https://www.c3rewards.com/api/merchant/?module=pos&action=restaurant_update_table_state";
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
//                        JSONObject jresult = json.getJSONObject("result");
//                        JSONArray jfloors = jresult.getJSONArray("floors");
                    long timeAfter = Calendar.getInstance().getTimeInMillis();
                    System.out.println("Update Time: " + (timeAfter - timeBefore) + "ms");
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
            System.out.println("Internet Reconnected!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
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