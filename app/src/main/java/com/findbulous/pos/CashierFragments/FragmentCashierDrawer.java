package com.findbulous.pos.CashierFragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.findbulous.pos.Attribute;
import com.findbulous.pos.Attribute_Value;
import com.findbulous.pos.ChoosePOSPermissionPage;
import com.findbulous.pos.Currency;
import com.findbulous.pos.Network.NetworkUtils;
import com.findbulous.pos.Order;
import com.findbulous.pos.POS_Category;
import com.findbulous.pos.POS_Session;
import com.findbulous.pos.Product;
import com.findbulous.pos.Product_Tax;
import com.findbulous.pos.R;
import com.findbulous.pos.Table;
import com.findbulous.pos.databinding.FragmentCashierDrawerBinding;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

import io.realm.Realm;
import io.realm.RealmResults;

public class FragmentCashierDrawer extends Fragment {

    private FragmentCashierDrawerBinding binding;
    private Realm realm;
    private POS_Session posSession;
    private Currency currency;
    //viewmodel

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_cashier_drawer, container, false);
        View view = binding.getRoot();

        realm = Realm.getDefaultInstance();

        posSession = realm.copyFromRealm(realm.where(POS_Session.class)
                .equalTo("state", "opened").findFirst());
        currency = realm.copyFromRealm(realm.where(Currency.class).findFirst());

        //Set Amount + currency
        binding.openingDrawerAmount.setText(currencyDisplayFormat(0.00));
        binding.cashPaymentSale.setText(currencyDisplayFormat(0.00));
        binding.otherPaymentSale.setText(currencyDisplayFormat(0.00));
        binding.expectedDrawerAmount.setText(currencyDisplayFormat(0.00));
        binding.differenceAmount.setText(currencyDisplayFormat(0.00));

        binding.closeDrawerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeDrawer();
                Toast.makeText(requireContext(), "The remarks: " + binding.remarksEt.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void closeDrawer(){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
        Date today = new Date();
        //set local db and cloud db, pos session close date and state to closed
        //local db
        posSession.setState("closed");
        posSession.setStop_at(String.valueOf(today));
        //Cloud db
        if(!NetworkUtils.isNetworkAvailable(getContext())){
            Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
        }else{
            new apiEndSession().execute();
        }
    }

    private String currencyDisplayFormat(double value){
        String valueFormatted = null;
        int decimal_place = currency.getDecimal_places();
        String currencyPosition = currency.getPosition();
        String symbol = currency.getSymbol();

        if(currencyPosition.equalsIgnoreCase("after")){
            valueFormatted = String.format("%." + decimal_place + "f", value) + symbol;
        }else if(currencyPosition.equalsIgnoreCase("before")){
            valueFormatted = symbol + String.format("%." + decimal_place + "f", value);
        }

        return valueFormatted;
    }

    public class apiEndSession extends AsyncTask<String, String, String>{
        private ProgressDialog pd;
        private boolean sessionClosed = false;

        @Override
        protected void onPreExecute() {
            if(pd == null) {
                pd = createProgressDialog(getContext());
                pd.show();
            }
            sessionClosed = false;
        }

        @Override
        protected String doInBackground(String... strings) {
            long timeBefore = Calendar.getInstance().getTimeInMillis();

            String connection_error = "";
            String url = "https://www.c3rewards.com/api/merchant/?module=pos&action=end_session";
            String agent = "c092dc89b7aac085a210824fb57625db";
            String jsonUrl =url + "&agent=" + agent;

            //Temporary (bug fixing)
//            jsonUrl += "&dev=1";

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
                            sessionClosed = true;
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
            System.out.println("End Session time taken: " + (timeAfter - timeBefore) + "ms");
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(!NetworkUtils.isNetworkAvailable(getContext())){
                Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
            }else{

            }
            if(sessionClosed){
                RealmResults<POS_Session> all_pos_sessions = realm.where(POS_Session.class).findAll();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        all_pos_sessions.deleteAllFromRealm();
                    }
                });

                Intent intent = new Intent(getContext(), ChoosePOSPermissionPage.class);
                getActivity().startActivity(intent);
                getActivity().finish();
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
