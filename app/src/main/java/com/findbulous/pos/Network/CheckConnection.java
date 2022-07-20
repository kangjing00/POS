package com.findbulous.pos.Network;

import android.content.Context;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.findbulous.pos.Floor;
import com.findbulous.pos.State;
import com.findbulous.pos.Table;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import io.realm.Realm;
import io.realm.RealmResults;

public class CheckConnection extends AppCompatActivity implements ConnectivityChangeReceiver.OnConnectivityChangedListener {

    private ConnectivityChangeReceiver connectivityChangeReceiver;


    private Context context;
    private Realm realm;
    private boolean updated = false;
    private ArrayList<Floor> floors;
    private ArrayList<Table> tables;
    private RealmResults<Table> table_list;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        connectivityChangeReceiver = new ConnectivityChangeReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(connectivityChangeReceiver, filter);

        realm = Realm.getDefaultInstance();
        tables = new ArrayList<Table>();
    }

    @Override
    public void onConnectivityChanged(boolean isConnected){
        if(isConnected){
            System.out.println("Internet Connected");
            //if the account is main account, update the table and floor
            //else if the account is no main account, load the table and floor
            //new loadFloorAndTable().execute();
//            table_list = realm.where(Table.class).findAll();
//            tables.addAll(realm.copyFromRealm(table_list));
//            new updateTable().execute();
        }else{
            System.out.println("Internet Disconnected");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectivityChangeReceiver);
    }

    public class updateTable extends AsyncTask<String, String, String> {
        boolean no_connection = false;
        String connection_error = "";

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... strings) {
            long timeBefore = Calendar.getInstance().getTimeInMillis();

            String urlParameters = "";
            for(int i =0; i < tables.size(); i ++){
                urlParameters += "&states[" + tables.get(i).getTable_id() + "]=" + tables.get(i).getState();
            }
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
                        tables.clear();
//                        JSONObject jresult = json.getJSONObject("result");
//                        JSONArray jfloors = jresult.getJSONArray("floors");
//
//                        for (int a = 0; a < jfloors.length(); a++) {
//                            JSONObject jf = jfloors.getJSONObject(a);
//                            Floor floor = new Floor(jf.getInt("floor_id"), jf.getString("name"),
//                                    jf.getInt("sequence"), jf.getString("active"));
//
//                            JSONArray jtables = jf.getJSONArray("tables");
//                            for(int x = 0; x < jtables.length(); x++){
//                                JSONObject jt = jtables.getJSONObject(x);
//                                Table table = new Table(jt.getInt("table_id"), jt.getString("name"),
//                                        jt.getDouble("position_h"), jt.getDouble("position_v"),
//                                        jt.getDouble("width"), jt.getDouble("height"), jt.getInt("seats"),
//                                        jt.getString("active"), jt.getString("state"), floor);
//                                System.out.println("Floor: " + floor.getFloor_id() + "    Table: " + table.getTable_id() + " State: " + table.getState());
//                            }
//                        }
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

            }
        }
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
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.insertOrUpdate(floors);
                    realm.insertOrUpdate(tables);
                }
            });
            floors.clear();
            tables.clear();
        }
    }
}
