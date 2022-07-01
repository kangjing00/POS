package com.findbulous.pos.CustomerFragments;

import static android.content.Context.MODE_MULTI_PROCESS;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.findbulous.pos.Customer;
import com.findbulous.pos.Adapters.CustomerAdapter;
import com.findbulous.pos.CustomerPage;
import com.findbulous.pos.Network.NetworkUtils;
import com.findbulous.pos.R;
import com.findbulous.pos.databinding.FragmentCustomerBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import io.realm.Realm;

public class FragmentCustomer extends Fragment implements CustomerAdapter.OnItemClickListener{

    private FragmentCustomerBinding binding;
    private CustomerAdapter customerAdapter;
    private ArrayList<Customer> customers;
    private Realm realm;
    private String searchValue;
    // Storing data into SharedPreferences
    private SharedPreferences customerSharedPreference;
    // Creating an Editor object to edit(write to the file)
    private SharedPreferences.Editor customerSharedPreferenceEdit;
    //Popup
    private TextView customer_name, customer_email, customer_phone_no;
    private ImageButton exit_btn;
    private RelativeLayout view_detail_btn, add_current_customer_btn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_customer, container, false);
        View view = binding.getRoot();

        realm = Realm.getDefaultInstance();

        //Customer search value
        searchValue = null;
        //Share Preference
        customerSharedPreference = getActivity().getSharedPreferences("CurrentCustomer", MODE_MULTI_PROCESS);
        customerSharedPreferenceEdit = customerSharedPreference.edit();
        //Customer Recyclerview
        binding.searchCustomerRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.searchCustomerRv.setHasFixedSize(true);
        customers = new ArrayList<>();
        customerAdapter = new CustomerAdapter(customers, this);
        binding.searchCustomerRv.setAdapter(customerAdapter);

        int currentCustomerId = customerSharedPreference.getInt("customerID", -1);
        if(currentCustomerId != -1){
            binding.customerCurrentCustomerName.setText(customerSharedPreference.getString("customerName", null));
            binding.customerCurrentCustomerEmail.setText(customerSharedPreference.getString("customerEmail", null));
            binding.customerCurrentCustomerId.setText("#" + customerSharedPreference.getInt("customerID", -1));
            binding.customerCurrentCustomerPhone.setText(customerSharedPreference.getString("customerPhoneNo", null));
            binding.customerCurrentCustomerRl.setVisibility(View.VISIBLE);
        }else{
            binding.customerCurrentCustomerRl.setVisibility(View.GONE);
        }

        binding.customerCurrentRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //REMOVE CURRENT CUSTOMER /or/ CLEAR CURRENT CUSTOMER /or/ MAKE CURRENT CUSTOMER TO GENERAL CUSTOMER
                customerSharedPreferenceEdit.putInt("customerID", -1);
                customerSharedPreferenceEdit.putString("customerName", null);
                customerSharedPreferenceEdit.putString("customerEmail", null);
                customerSharedPreferenceEdit.putString("customerPhoneNo", null);
                customerSharedPreferenceEdit.putString("customerIdentityNo", null);
                customerSharedPreferenceEdit.putString("customerBirthdate", null);
                customerSharedPreferenceEdit.commit();
                ((CustomerPage)getActivity()).refreshCartCurrentCustomer();
                if(customers.isEmpty())
                    binding.emptyCustomerImg.setVisibility(View.VISIBLE);
                binding.customerCurrentCustomerRl.setVisibility(View.GONE);
            }
        });
        binding.customerCurrentEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((CustomerPage)getActivity()).editCustomer(customerSharedPreference.getInt("customerID", -1));
            }
        });
        binding.customerSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchEt = binding.customerEtSearch.getText().toString();
                customers.clear();
                if(searchEt.isEmpty()){
                    Toast.makeText(getContext(), "Please provide some hints for me", Toast.LENGTH_SHORT).show();
                    binding.emptyCustomerImg.setVisibility(View.VISIBLE);
                }else if(searchEt.length() < 4){
                    Toast.makeText(getContext(), "Please enter at least 4 characters", Toast.LENGTH_SHORT).show();
                    binding.emptyCustomerImg.setVisibility(View.VISIBLE);
                }else{
                    searchValue = searchEt;
                    new getList().execute();
//                    RealmResults<Customer> results = realm.where(Customer.class).contains("customer_name", searchEt, Case.INSENSITIVE)
//                            .or().contains("customer_email", searchEt, Case.INSENSITIVE).or().contains("customer_phoneNo", searchEt, Case.INSENSITIVE)
//                            .findAll();
//                    if(results.isEmpty()){
//                        binding.emptyCustomerImg.setVisibility(View.VISIBLE);
//                    }else {
//                        binding.emptyCustomerImg.setVisibility(View.GONE);
//                        customers.addAll(realm.copyFromRealm(results));
//                    }
                }
                System.out.println("notifyDataSetChanged()");
                customerAdapter.notifyDataSetChanged();
                binding.customerEtSearch.onEditorAction(EditorInfo.IME_ACTION_DONE);
                binding.customerEtSearch.clearFocus();
            }
        });

        return view;
    }

    public class getList extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            String url = "https://www.c3rewards.com/api/merchant/?module=customers&action=search";
            String agent = "c092dc89b7aac085a210824fb57625db";
            String jsonUrl =url + "&agent=" + agent;
            jsonUrl = jsonUrl + "&search=" + searchValue;
            System.out.println(jsonUrl);

            int page = 1, counter = 0;
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
                            JSONArray jcustomers = jresult.getJSONArray("customers");

                            if(jcustomers.length() != 0){
                                page++;
                            }else{
                                page = -1;
                            }

                            for (int a = 0; a < jcustomers.length(); a++) {
                                JSONObject ja = jcustomers.getJSONObject(a);
                                Customer cust = new Customer(Integer.valueOf(ja.getString("customer_id")),
                                        ja.getString("full_name"), ja.getString("email"),
                                        ja.getString("tel_no"), ja.getString("ic_no"), ja.getString("date_birth"));
                                customers.add(cust);
                                System.out.println(customers.get(a).getCustomer_name());
                                counter++;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    Log.e("error", "cannot fetch data");
                    page = -1;
                }
            }
            System.out.println("Counter: " + counter);

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(!NetworkUtils.isNetworkAvailable(getContext())){
                Toast.makeText(getContext(), "Internet Connection Lost", Toast.LENGTH_SHORT).show();
            }
            if(customers.isEmpty()){
                binding.emptyCustomerImg.setVisibility(View.VISIBLE);
            }else {
                binding.emptyCustomerImg.setVisibility(View.GONE);
            }
            customerAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onCustomerClick(int position) {
        Customer clickedCustomer = customers.get(position);
        showOptionsPopup(clickedCustomer);
        binding.emptyCustomerImg.setVisibility(View.GONE);
    }

    private void showOptionsPopup(Customer customer){
        PopupWindow popup = new PopupWindow(getContext());
        View layout = getLayoutInflater().inflate(R.layout.customer_list_clicked_popup, null);
        popup.setContentView(layout);
        // Set content width and height
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // Closes the popup window when touch outside of it - when looses focus
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        // Show anchored to button
        popup.setElevation(8);
        popup.setBackgroundDrawable(null);
        popup.showAtLocation(binding.getRoot(), Gravity.CENTER, 0, 0);

        View container = (View) popup.getContentView().getParent();
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);

        customer_name = layout.findViewById(R.id.customer_name);
        customer_email = layout.findViewById(R.id.customer_email);
        customer_phone_no = layout.findViewById(R.id.customer_phone_no);
        exit_btn = layout.findViewById(R.id.exit_btn);
        view_detail_btn = layout.findViewById(R.id.view_detail_btn);
        add_current_customer_btn = layout.findViewById(R.id.add_current_customer_btn);

        customer_name.setText(customer.getCustomer_name());
        customer_email.setText(customer.getCustomer_email());
        customer_phone_no.setText(customer.getCustomer_phoneNo());

        exit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.dismiss();
            }
        });

        view_detail_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((CustomerPage)getActivity()).viewCustomerDetail(customer.getCustomer_id());
                popup.dismiss();
            }
        });

        add_current_customer_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customerSharedPreferenceEdit.putInt("customerID", customer.getCustomer_id());
                customerSharedPreferenceEdit.putString("customerName", customer.getCustomer_name());
                customerSharedPreferenceEdit.putString("customerEmail", customer.getCustomer_email());
                customerSharedPreferenceEdit.putString("customerPhoneNo", customer.getCustomer_phoneNo());
                customerSharedPreferenceEdit.putString("customerIdentityNo", customer.getCustomer_identityNo());
                customerSharedPreferenceEdit.putString("customerBirthdate", customer.getCustomer_birthdate());
                customerSharedPreferenceEdit.commit();

                ((CustomerPage)getActivity()).refreshCartCurrentCustomer();
                binding.customerCurrentCustomerName.setText(customer.getCustomer_name());
                binding.customerCurrentCustomerEmail.setText(customer.getCustomer_email());
                binding.customerCurrentCustomerId.setText("#" + customer.getCustomer_id());
                binding.customerCurrentCustomerPhone.setText(customer.getCustomer_phoneNo());
                binding.customerCurrentCustomerRl.setVisibility(View.VISIBLE);

                Toast.makeText(getContext(), "Add to Current Customer Btn", Toast.LENGTH_SHORT).show();
                popup.dismiss();
            }
        });
    }

    public void updateCurrentCustomer(){
        if(customerSharedPreference.getInt("customerID", -1) == -1){
            if(customers.isEmpty())
                binding.emptyCustomerImg.setVisibility(View.VISIBLE);
            binding.customerCurrentCustomerRl.setVisibility(View.GONE);
        }
    }
}
