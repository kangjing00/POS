package com.findbulous.pos.CustomerFragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.findbulous.pos.Customer;
import com.findbulous.pos.CustomerPage;
import com.findbulous.pos.Network.NetworkUtils;
import com.findbulous.pos.Order;
import com.findbulous.pos.R;
import com.findbulous.pos.databinding.FragmentCustomerDetailBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import io.realm.Realm;
import io.realm.RealmResults;

public class FragmentCustomerDetail extends Fragment {

    private FragmentCustomerDetailBinding binding;
    private Realm realm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_customer_detail, container, false);
        View view = binding.getRoot();

        realm = Realm.getDefaultInstance();

        if(getArguments() != null){
            int customer_id = getArguments().getInt("customer_id");
            Customer customer = realm.where(Customer.class).equalTo("customer_id", customer_id).findFirst();
            binding.customerDetailName.setText(customer.getCustomer_name());
            binding.customerDetailEmail.setText(customer.getCustomer_email());
            binding.customerDetailPhoneNo.setText(customer.getCustomer_phoneNo());
            RealmResults<Order> results = realm.where(Order.class).equalTo("customer.customer_id", customer_id)
                    .and().equalTo("state", "paid").sort("date_order").findAll();
            if(!results.isEmpty()){
                Order order = results.last();
                binding.customerDetailLastOrderId.setText("Last Order: #" + order.getOrder_id());
            }else{
                binding.customerDetailLastOrderId.setText("Last Order: No order");
            }
        }

        binding.customerDetailEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((CustomerPage)getActivity()).editCustomer(getArguments().getInt("customer_id"));
            }
        });

        return view;
    }

    public class getCustomer extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            String url = "https://www.c3rewards.com/api/merchant/?module=customers&action=search";
            String agent = "c092dc89b7aac085a210824fb57625db";
            String jsonUrl =url + "&agent=" + agent;
            jsonUrl = jsonUrl + "&search=" + getArguments().getInt("customer_id");
            System.out.println(jsonUrl);

//            int page = 1, counter = 0;
//            while(page > 0) {
//                String jsonUrlPage = jsonUrl + "&page=" + page;
//                URL obj;
//                try {
//                    obj = new URL(jsonUrlPage);
//                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//                    // optional default is GET
//                    con.setRequestMethod("GET");
//                    //add request header
//                    int responseCode = con.getResponseCode();
//                    System.out.println("\nSending 'GET' request to URL : " + jsonUrlPage);
//                    System.out.println("Response Code : " + responseCode);
//
//                    BufferedReader in = new BufferedReader(
//                            new InputStreamReader(con.getInputStream()));
//                    String inputLine;
//
//                    StringBuilder response = new StringBuilder();
//
//                    while ((inputLine = in.readLine()) != null) {
//                        response.append(inputLine);
//                    }
//                    in.close();
//
//                    System.out.println(response);
//                    String data = response.toString();
//                    try {
//                        JSONObject json = new JSONObject(data);
//                        String status = json.getString("status");
//
//                        if (status.equals("OK")) {
//                            JSONObject jresult = json.getJSONObject("result");
//                            JSONArray jcustomers = jresult.getJSONArray("customers");
//
//                            if(jcustomers.length() != 0){
//                                page++;
//                            }else{
//                                page = -1;
//                            }
//
//                            for (int a = 0; a < jcustomers.length(); a++) {
//                                JSONObject ja = jcustomers.getJSONObject(a);
//                                Customer cust = new Customer(Integer.valueOf(ja.getString("customer_id")),
//                                        ja.getString("full_name"), ja.getString("email"),
//                                        ja.getString("tel_no"), ja.getString("ic_no"), ja.getString("date_birth"));
//                                customers.add(cust);
//                                System.out.println(customers.get(a).getCustomer_name());
//                                counter++;
//                            }
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                } catch (IOException e) {
//                    Log.e("error", "cannot fetch data");
//                    page = -1;
//                }
//            }
//            System.out.println("Counter: " + counter);

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(!NetworkUtils.isNetworkAvailable(getContext())){
                Toast.makeText(getContext(), "Internet Connection Lost", Toast.LENGTH_SHORT).show();
            }
//            if(customers.isEmpty()){
//                binding.emptyCustomerImg.setVisibility(View.VISIBLE);
//            }else {
//                binding.emptyCustomerImg.setVisibility(View.GONE);
//            }
//            customerAdapter.notifyDataSetChanged();
        }
    }
}