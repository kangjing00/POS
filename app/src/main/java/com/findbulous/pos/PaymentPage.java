package com.findbulous.pos;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.findbulous.pos.API.SetOrderCustomer;
import com.findbulous.pos.Adapters.PaymentAdapter;
import com.findbulous.pos.Adapters.PaymentOrderLineAdapter;
import com.findbulous.pos.Adapters.SplitBillOrderAdapter;
import com.findbulous.pos.Network.CheckConnection;
import com.findbulous.pos.Network.NetworkUtils;
import com.findbulous.pos.databinding.CartOrderAddDiscountPopupBinding;
import com.findbulous.pos.databinding.CashInOutPopupBinding;
import com.findbulous.pos.databinding.PaymentAddTipPopupBinding;
import com.findbulous.pos.databinding.PaymentOrderLinePopupBinding;
import com.findbulous.pos.databinding.PaymentPageBinding;
import com.findbulous.pos.databinding.SplitOrderPopupBinding;
import com.findbulous.pos.databinding.SplitOrderQtyPopupBinding;
import com.findbulous.pos.databinding.ToolbarSyncPopupBinding;
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

public class PaymentPage extends CheckConnection implements SplitBillOrderAdapter.SplitOrderLineInterface, PaymentAdapter.PaymentInterface {

    private PaymentPageBinding binding;
//    private PaymentMethodPagerAdapter paymentMethodPagerAdapter;
    private String[] titles = new String[]{"Cash", "Other Modes"};
    private PaymentPageViewModel viewModel;
    //RecyclerView
    private ArrayList<Payment> payments;
    private PaymentAdapter paymentAdapter;
    private ArrayList<Order_Line> payment_order_lines;
//    private PaymentOrderLineAdapter paymentOrderLineAdapter;

    private ArrayList<Order_Line> splitting_order_lines, splitted_order_lines;
    private ArrayList<Integer> splitsQty;
    private  PaymentPageSplitBillViewModel splitBillViewModel;
    private SplitBillOrderAdapter splittingOrderLineAdapter;
    //Current order
    private Order currentOrder;
    //Current customer
    private Customer currentCustomer;

    private Realm realm;
    private SharedPreferences currentOrderSharePreference, currentCustomerSharePreference;
    private SharedPreferences.Editor currentOrderSharePreferenceEdit, currentCustomerSharePreferenceEdit;

    private POS_Config pos_config;
    private Currency currency;
    private ArrayList<Payment_Method> payment_methods;

    //Payment Methods Drop Down List
    private ArrayAdapter<Payment_Method> paymentMethodAdapter;
    private Payment_Method selectedPaymentMethod;

    String statuslogin;
    Context contextpage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextpage = PaymentPage.this;
        binding = DataBindingUtil.setContentView(this, R.layout.payment_page);
        splitBillViewModel = new ViewModelProvider(this).get(PaymentPageSplitBillViewModel.class);
        viewModel = new ViewModelProvider(this).get(PaymentPageViewModel.class);
        binding.setPaymentPageViewModel(viewModel);
        binding.setLifecycleOwner(this);

        realm = Realm.getDefaultInstance();

        currentCustomerSharePreference = getSharedPreferences("CurrentCustomer",MODE_MULTI_PROCESS);
        currentCustomerSharePreferenceEdit = currentCustomerSharePreference.edit();
        int current_customer_id = currentCustomerSharePreference.getInt("customerID", -1);
        String customer_name = currentCustomerSharePreference.getString("customerName", null);
        String customerPhoneNo = currentCustomerSharePreference.getString("customerPhoneNo", null);
        String customerEmail = currentCustomerSharePreference.getString("customerEmail", null);
        String customerIdentityNo = currentCustomerSharePreference.getString("customerIdentityNo", null);
        String customerBirthdate = currentCustomerSharePreference.getString("customerBirthdate", null);
        if(current_customer_id != -1){
            currentCustomer = new Customer(current_customer_id, customer_name, customerEmail, customerPhoneNo, customerIdentityNo, customerBirthdate);
        }else{
            currentCustomer = null;
        }


        currentOrderSharePreference = getSharedPreferences("CurrentOrder",MODE_MULTI_PROCESS);
        currentOrderSharePreferenceEdit = currentOrderSharePreference.edit();
        currentOrder = new Order();
        int currentLocalOrderId = currentOrderSharePreference.getInt("localOrderId", -1);
        Order order = realm.where(Order.class).equalTo("local_order_id", currentLocalOrderId).findFirst();
        if(order != null) {
            currentOrder = realm.copyFromRealm(order);
        }

        //Body Setting
        //Tip Setting
        POS_Config temp_pos_config = realm.where(POS_Config.class).findFirst();
        if(temp_pos_config != null){
            pos_config = realm.copyFromRealm(temp_pos_config);
        }
        if(pos_config.isIface_tipproduct()){
            binding.paymentBarAddTip.setVisibility(View.VISIBLE);
        }else{
            binding.paymentBarAddTip.setVisibility(View.GONE);
        }

//        // Below line for testing purpose
//        binding.paymentBarAddTip.setVisibility(View.VISIBLE);
        //Currency setting
        currency = realm.copyFromRealm(realm.where(Currency.class).findFirst());
        payment_methods = new ArrayList<>();
        payment_methods.addAll(realm.copyFromRealm(realm.where(Payment_Method.class).findAll()));

        //Customer Setting
        if((current_customer_id != -1) && (current_customer_id != 0)) {
            binding.paymentBarCustomerName.setText(customer_name);
            binding.paymentBarCustomerId.setText("#" + current_customer_id);
            binding.paymentBarCustomerRl.setVisibility(View.VISIBLE);
            binding.paymentOrderDetailCustomerName.setText(customer_name);
            binding.paymentOrderDetailCustomerName.setVisibility(View.VISIBLE);
            binding.paymentBarApplyCustomerBtn.setVisibility(View.GONE);
        }else{
            binding.paymentBarCustomerRl.setVisibility(View.GONE);
            binding.paymentOrderDetailCustomerName.setVisibility(View.GONE);
            binding.paymentBarApplyCustomerBtn.setVisibility(View.VISIBLE);
        }
        //Recycler View
        binding.paymentOrderPaymentsRv.setLayoutManager(new LinearLayoutManager(contextpage, LinearLayoutManager.VERTICAL, false));
        binding.paymentOrderPaymentsRv.setHasFixedSize(true);
        payments = new ArrayList<>();
        paymentAdapter = new PaymentAdapter(payments, this);
        binding.paymentOrderPaymentsRv.setAdapter(paymentAdapter);

//        binding.paymentOrderDetailProductRv.setLayoutManager(new LinearLayoutManager(contextpage, LinearLayoutManager.VERTICAL, false));
//        binding.paymentOrderDetailProductRv.setHasFixedSize(true);
        payment_order_lines = new ArrayList<>();
//        paymentOrderLineAdapter = new PaymentOrderLineAdapter(payment_order_lines);
        payment_order_lines.addAll(order.getOrder_lines());
//        binding.paymentOrderDetailProductRv.setAdapter(paymentOrderLineAdapter);



//        paymentMethodPagerAdapter = new PaymentMethodPagerAdapter(this);

        double order_subtotal = 0.0, total_price_subtotal_incl = 0.0, amount_order_discount = 0.0;
        for(int i = 0; i < payment_order_lines.size(); i++){
            order_subtotal += payment_order_lines.get(i).getPrice_subtotal();
            total_price_subtotal_incl += payment_order_lines.get(i).getPrice_subtotal_incl();
        }
        binding.paymentSubtotal.setText(String.format("%.2f", order_subtotal));
        binding.paymentTax.setText(String.format("%.2f", currentOrder.getAmount_tax()));
        if(currentOrder.getDiscount_type() != null) {
            if (currentOrder.getDiscount_type().equalsIgnoreCase("percentage")) {
                amount_order_discount = (total_price_subtotal_incl * currentOrder.getDiscount()) / 100;
            } else if (currentOrder.getDiscount_type().equalsIgnoreCase("fixed_amount")) {
                amount_order_discount = currentOrder.getDiscount();
            }
        }
        binding.paymentDiscount.setText(String.format("- %.2f", amount_order_discount));

        binding.paymentGrandTotal.setText(String.format("%.2f", currentOrder.getAmount_total()));
        binding.paymentBarPayableAmount.setText(currencyDisplayFormat(currentOrder.getAmount_total()));
        viewModel.setAmount_total(currentOrder.getAmount_total());

        binding.paymentOrderDetailOrderId.setText("#" + currentOrder.getOrder_id());
        if(currentOrder.getTable() == null) { //Takeaway
            binding.paymentOrderDetailType.setText("Takeaway");
        }else{ //Dine-in
            binding.paymentOrderDetailType.setText("Dine-in - " + currentOrder.getTable().getFloor().getName() + " / " + currentOrder.getTable().getName());
        }

        binding.customerCount.setText("" + currentOrder.getCustomer_count());



        //Payment Methods Spinner / Drop Down List
        selectedPaymentMethod = payment_methods.get(0);
        paymentMethodAdapter = new ArrayAdapter<Payment_Method>(contextpage, R.layout.textview_spinner_item_larger, payment_methods){
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent){
                View v = null;
                v = super.getDropDownView(position, null, parent);

                // If this is the selected item position
                if (position == binding.paymentMethodSpinner.getSelectedItemPosition()) {
                    if(position == (binding.paymentMethodSpinner.getCount() - 1)){ //last one
                        v.setBackground(getResources().getDrawable(R.drawable.box_btm_corner_dark_orange));
                    }else { //not the last one
                        v.setBackgroundColor(getResources().getColor(R.color.darkOrange));
                    }
                }
                else {
                    // for other views
                    if(position != (binding.paymentMethodSpinner.getCount() - 1)) { // not the last one
                        v.setBackgroundColor(getResources().getColor(R.color.white));
                    }else{  //last one
                        v.setBackground(getResources().getDrawable(R.drawable.box_btm_corner));
                    }
                }
                return v;
            }
        };
        paymentMethodAdapter.setDropDownViewResource(R.layout.textview_spinner_item_larger);
        binding.paymentMethodSpinner.setAdapter(paymentMethodAdapter);
        binding.paymentMethodSpinner.setDropDownVerticalOffset(90);
        binding.paymentMethodSpinner.setSelection(0);

        binding.paymentMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                Payment_Method payment_method = (Payment_Method) adapterView.getItemAtPosition(pos);
                selectedPaymentMethod = payment_method;

                //binding.cartInclude.cartBtnPosType.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //OnClickListener
        //Body
        {
        binding.paymentSplitBillBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSplitBill();
            }
        });
        binding.paymentBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contextpage, HomePage.class);
                startActivity(intent);
                finish();
                Toast.makeText(contextpage, "Back Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        binding.paymentBarAddTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTipPopup(view);
                Toast.makeText(contextpage, "Add Tip Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        binding.paymentOrderDetailViewAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrderProducts();

                Toast.makeText(contextpage, "View All Button Clicked", Toast.LENGTH_SHORT).show();
            }
        });
        binding.paymentTipCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.textView9.setVisibility(View.GONE);
                binding.paymentTip.setVisibility(View.GONE);
                binding.getPaymentPageViewModel().setPayment_tip("0.00");
                double amount_total = binding.getPaymentPageViewModel().getAmount_total();
                binding.paymentGrandTotal.setText(String.format("%.2f", amount_total));
                binding.paymentBarPayableAmount.setText(currencyDisplayFormat(amount_total));
                currentOrder.setAmount_total(amount_total);
                currentOrder.setDisplay_amount_total(currencyDisplayFormat(amount_total));
                binding.paymentTipCancelBtn.setVisibility(View.GONE);
            }
        });
        binding.paymentBarApplyCustomerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contextpage, CustomerPage.class);
                startActivity(intent);
                finish();
            }
        });
        binding.paymentBarRemoveCustomerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.paymentBarCustomerRl.setVisibility(View.GONE);
                binding.paymentBarApplyCustomerBtn.setVisibility(View.VISIBLE);

                if(currentOrder.getLocal_order_id() != -1) {
                    currentCustomer = null;
                    setCurrentCustomer(null);

                    binding.paymentOrderDetailCustomerName.setText("[Customer Name]");
                    Toast.makeText(contextpage, "Current Customer Removed", Toast.LENGTH_SHORT).show();
                    binding.paymentOrderDetailCustomerName.setVisibility(View.GONE);
                }
            }
        });
        //wait for api to make / confirm payment
        binding.paymentOrderDetailConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(is_balanceLargerOrEqualToZero() && (currentOrder.getLocal_order_id() != -1)) {
                    int current_customer_id = currentCustomerSharePreference.getInt("customerID", -1);
                    currentCustomerSharePreferenceEdit.putInt("customerID", -1);
                    currentCustomerSharePreferenceEdit.putString("customerName", null);
                    currentCustomerSharePreferenceEdit.putString("customerEmail", null);
                    currentCustomerSharePreferenceEdit.putString("customerPhoneNo", null);
                    currentCustomerSharePreferenceEdit.putString("customerIdentityNo", null);
                    currentCustomerSharePreferenceEdit.putString("customerBirthdate", null);
                    currentCustomerSharePreferenceEdit.commit();
                    int current_local_order_id = currentOrderSharePreference.getInt("localOrderId", -1);
                    currentOrderSharePreferenceEdit.putInt("orderingState", 0);
                    currentOrderSharePreferenceEdit.putInt("localOrderId", -1);
                    currentOrderSharePreferenceEdit.commit();

                    Order updated_current_order = currentOrder;
                    double amount_paid = updated_current_order.getAmount_total();
//                    for (int i = 0; i < payments.size(); i++){
//                        amount_paid += payments.get(i).getAmount();
//                    }

                    double tip_amount = Double.valueOf(binding.getPaymentPageViewModel().getPayment_tip().getValue());
                    if (tip_amount > 0) {
                        if (updated_current_order.isIs_tipped() == false) {
                            updated_current_order.setIs_tipped(true);
                        }
                        updated_current_order.setTip_amount(tip_amount);
                    }else{
                        updated_current_order.setIs_tipped(false);
                    }
                    updated_current_order.setState("paid");
                    updated_current_order.setState_name("Paid");
                    updated_current_order.setAmount_paid(amount_paid);
                    updated_current_order.setDisplay_amount_paid(currencyDisplayFormat(amount_paid));
                    updated_current_order.setAmount_return(Double.valueOf(viewModel.getPayment_order_detail_balance().getValue()));
                    updated_current_order.setDisplay_amount_return(currencyDisplayFormat(Double.valueOf(viewModel.getPayment_order_detail_balance().getValue())));
                    if(currentCustomer == null){
                        currentCustomer = realm.where(Customer.class).equalTo("customer_id", 0).findFirst();
                    }
                    updated_current_order.setCustomer(currentCustomer);

                    boolean hasReturn = false;
                    if(updated_current_order.getAmount_return() > 0){
                        Number id = realm.where(Payment.class).max("local_id");
                        int nextID = -1;
                        if (id == null) {
                            nextID = 1;
                        } else {
                            nextID = id.intValue() + 1;
                        }
                        if (payments.size() > 0) {
                            nextID = payments.get(payments.size() - 1).getLocal_id() + 1;
                        }
                        double return_amount = -updated_current_order.getAmount_return();
                        Payment return_payment = new Payment(nextID, -1, return_amount,
                                currencyDisplayFormat(return_amount), selectedPaymentMethod, currentOrder);
                        payments.add(return_payment);
                        hasReturn = true;
                    }

                    if(currentOrder.getTable() != null){
                        RealmResults<Order> results = realm.where(Order.class)
                                .equalTo("table.table_id", updated_current_order.getTable().getTable_id())
                                .and().notEqualTo("state", "paid").and()
                                .notEqualTo("local_order_id", updated_current_order.getLocal_order_id()).findAll();
                        if(results.size() == 0)
                            tableOccupiedToVacant(updated_current_order.getTable());
                    }
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.insertOrUpdate(updated_current_order);
                            realm.insertOrUpdate(payments);
                        }
                    });
                    Toast.makeText(contextpage, "Payment Success", Toast.LENGTH_SHORT).show();


                    Intent intent = new Intent(contextpage, HomePage.class);
                    if(!NetworkUtils.isNetworkAvailable(contextpage)){
                        Toast.makeText(PaymentPage.this, "No Internet Connection, payment record store in local", Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        finish();
                    }else{
                        new apiCheckoutOrder(updated_current_order.getOrder_id(), updated_current_order.getLocal_order_id(),
                                payments, hasReturn, payment_methods, updated_current_order.getTip_amount(), intent).execute();
                    }

                }else{
                    Toast.makeText(contextpage, "Transaction is not completed", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //Key pad number
        binding.cashKeypad1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.keypadAddNumber('1');
            }
        });
        binding.cashKeypad2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.keypadAddNumber('2');
            }
        });
        binding.cashKeypad3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.keypadAddNumber('3');
            }
        });
        binding.cashKeypad4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.keypadAddNumber('4');
            }
        });
        binding.cashKeypad5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.keypadAddNumber('5');
            }
        });
        binding.cashKeypad6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.keypadAddNumber('6');
            }
        });
        binding.cashKeypad7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.keypadAddNumber('7');
            }
        });
        binding.cashKeypad8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.keypadAddNumber('8');
            }
        });
        binding.cashKeypad9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.keypadAddNumber('9');
            }
        });
        binding.cashKeypad0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.keypadAddNumber('0');
            }
        });
        binding.cashKeypad00.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.keypadAddNumber('0');
                viewModel.keypadAddNumber('0');
            }
        });
        //Key pad btn
        binding.cashKeypadEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Double.valueOf(viewModel.getCash_amount_et().getValue()) > 0) {
                    if (Double.valueOf(viewModel.getPayment_order_detail_balance().getValue()) < 0) {
                        Number id = realm.where(Payment.class).max("local_id");
                        int nextID = -1;
                        if (id == null) {
                            nextID = 1;
                        } else {
                            nextID = id.intValue() + 1;
                        }
                        if (payments.size() > 0) {
                            nextID = payments.get(payments.size() - 1).getLocal_id() + 1;
                        }
                        double payment_amount = Double.valueOf(viewModel.getCash_amount_et().getValue());
                        Payment payment = new Payment(nextID, -1, payment_amount,
                                currencyDisplayFormat(payment_amount), selectedPaymentMethod, currentOrder);
                        payments.add(payment);
                        paymentAdapter.notifyDataSetChanged();

                        viewModel.keypadEnter();
                    } else {
                        Toast.makeText(contextpage, "The payable amount is completed.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(contextpage, "Please provide an amount at least more than zero", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.cashKeypadCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.setAmountEtToZero();
            }
        });
        binding.cashKeypadBackspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.keypadBackSpace();
            }
        });
        binding.cashKeypadExact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double balance = Double.valueOf(viewModel.getPayment_order_detail_balance().getValue());
                if(balance >= 0){
                    balance = 0.0;
                }else{
                    balance = -balance;
                }
                viewModel.setCash_amount_et(String.format("%.2f", balance));
            }
        });
        }
        //Toolbar buttons
        {
        binding.toolbarLayoutIncl.toolbarRefresh.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View view) {
                   showRefreshPopup(view);
               }
           }
        );

        binding.toolbarLayoutIncl.toolbarWifi.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Toast.makeText(contextpage, "Wifi Button Clicked", Toast.LENGTH_SHORT).show();
                }
            }
        );

        binding.toolbarLayoutIncl.cashInOutBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    showCashInOut();
                }
            }
        );
        }
    }

    public void setCurrentCustomer(Customer customer){
        int customer_id = -1;
        String name = null, email = null, phoneNo = null, identityNo = null, birthdate = null;

        if(customer != null){
            customer_id = customer.getCustomer_id();
            name = customer.getCustomer_name();
            email = customer.getCustomer_email();
            phoneNo = customer.getCustomer_phoneNo();
            identityNo = customer.getCustomer_identityNo();
            birthdate = customer.getCustomer_birthdate();
        }else{
            if(currentOrder.getLocal_order_id() != -1) {
                RealmResults results = realm.where(Order.class)
                        .equalTo("customer.customer_id", currentOrder.getCustomer().getCustomer_id())
                        .and().notEqualTo("local_order_id", currentOrder.getLocal_order_id()).findAll();
                if (results.size() < 1) {
                    Customer remove_Customer = realm.where(Customer.class)
                            .equalTo("customer_id", currentOrder.getCustomer().getCustomer_id())
                            .findFirst();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            remove_Customer.deleteFromRealm();
                        }
                    });
                }
                currentOrder.setPartner_id(-1);
            }
        }
        currentCustomerSharePreferenceEdit.putInt("customerID", customer_id);
        currentCustomerSharePreferenceEdit.putString("customerName", name);
        currentCustomerSharePreferenceEdit.putString("customerEmail", email);
        currentCustomerSharePreferenceEdit.putString("customerPhoneNo", phoneNo);
        currentCustomerSharePreferenceEdit.putString("customerIdentityNo", identityNo);
        currentCustomerSharePreferenceEdit.putString("customerBirthdate", birthdate);
        currentCustomerSharePreferenceEdit.commit();

        if(currentOrder.getLocal_order_id() != -1) {
            currentOrder.setCustomer(customer);

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    if (customer != null) {
                        realm.insertOrUpdate(customer);
                    }
                    realm.insertOrUpdate(currentOrder);
                }
            });


            if(customer == null){
                customer_id = 0;
            }
            if(!NetworkUtils.isNetworkAvailable(contextpage)){
                Toast.makeText(contextpage, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }else {
                new SetOrderCustomer(contextpage, currentOrder.getOrder_id(),
                        currentOrder.getLocal_order_id(), customer_id).execute();
            }
        }
    }

    public class apiCheckoutOrder extends AsyncTask<String, String, String> {
        private ProgressDialog pd = null;
        private int order_id, local_order_id;
        private ArrayList<Payment> payments;
        private boolean hasReturn;
        private ArrayList<Payment_Method> payment_methods;
        private double tip_amount;
        private Intent intent;

        private Order update_order;

        public apiCheckoutOrder(int order_id, int local_order_id, ArrayList<Payment> payments, boolean hasReturn,
                                ArrayList<Payment_Method> payment_methods, double tip_amount, Intent intent){
            this.order_id = order_id;
            this.local_order_id = local_order_id;
            this.payments = payments;
            this.hasReturn = hasReturn;
            this.payment_methods = payment_methods;
            this.tip_amount = tip_amount;
            this.intent = intent;
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
            int noOfPayment = payments.size();
            if(hasReturn){
                noOfPayment--;
            }
            for(int i = 0; i < noOfPayment; i++){
                urlParameters += "&payments[" + i + "][payment_method_id]=" + payments.get(i).getPayment_method().getPayment_method_id()
                        + "&payments[" + i + "][amount]=" + payments.get(i).getAmount();
            }
            if((pos_config.isIface_tipproduct()) && (pos_config.getTip_product_id() > 0) && (tip_amount > 0)){
                urlParameters += "&tip_amount=" + tip_amount;
            }

            //Testing (check error)
            urlParameters += "&dev=1";

            byte[] postData = urlParameters.getBytes(Charset.forName("UTF-8"));
            int postDataLength = postData.length;

            String url = "https://www.c3rewards.com/api/merchant/?module=pos&action=checkout_order";
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
                        JSONArray jo_payment_array = jo_order.getJSONArray("payments");

                        //order
                        if(update_order != null) {
                            update_order.setAmount_paid(jo_order.getDouble("amount_paid"));
                            update_order.setDisplay_amount_paid(jo_order.getString("display_amount_paid"));
                            update_order.setAmount_return(jo_order.getDouble("amount_return"));
                            update_order.setDisplay_amount_return(jo_order.getString("display_amount_return"));
                            update_order.setState(jo_order.getString("state"));
                            update_order.setState_name(jo_order.getString("state_name"));
                            if(jo_order.getString("is_tipped").length() > 0)
                                update_order.setIs_tipped(jo_order.getBoolean("is_tipped"));
                            if(jo_order.getString("tip_amount").length() > 0)
                                update_order.setTip_amount(jo_order.getDouble("tip_amount"));
                            if(jo_order.getString("display_tip_amount").length() > 0)
                                update_order.setDisplay_tip_amount(jo_order.getString("display_tip_amount"));
                        }

                        for(int i = 0; i < payments.size(); i++){
                            JSONObject jo_payment = jo_payment_array.getJSONObject(i);
                            payments.get(i).setId(jo_payment.getInt("id"));
                            payments.get(i).setAmount(jo_payment.getDouble("amount"));
                            payments.get(i).setDisplay_amount(jo_payment.getString("display_amount"));
                            int payment_method_id = jo_payment.getInt("payment_method_id");
                            for(int x = 0; x < payment_methods.size(); x++){
                                if(payment_methods.get(x).getPayment_method_id() == payment_method_id){
                                    payments.get(i).setPayment_method(payment_methods.get(x));
                                }
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
            System.out.println("Checkout order api time taken: " + (timeAfter - timeBefore) + "ms");
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
                            realm.insertOrUpdate(payments);
                        }
                    });
                }
                startActivity(intent);
                finish();
            }

            if (pd != null)
                pd.dismiss();
        }
    }


    private void showSplitBill(){
        PopupWindow popup = new PopupWindow(contextpage);
        SplitOrderPopupBinding popupBinding = SplitOrderPopupBinding.inflate(getLayoutInflater());
        popup.setContentView(popupBinding.getRoot());
        // Set content width and height
        popup.setHeight(1350);
        popup.setWidth(2000);
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        popup.setElevation(8);
        popup.setBackgroundDrawable(null);

        //RecyclerView
        int currentLocalOrderId = currentOrderSharePreference.getInt("localOrderId", -1);
        Order order = realm.where(Order.class).equalTo("local_order_id", currentLocalOrderId).findFirst();

        popupBinding.productsRv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        popupBinding.productsRv.setHasFixedSize(true);

        popupBinding.setSplitBillViewModel(splitBillViewModel);
        splitBillViewModel.getTotalSplitBill().observe(this, item -> {
            // Update the UI..
            popupBinding.setSplitBillViewModel(splitBillViewModel);
        });

        splitting_order_lines = new ArrayList<>();
        splitted_order_lines = new ArrayList<>();
        splitsQty = new ArrayList<>();
        splittingOrderLineAdapter = new SplitBillOrderAdapter(splitting_order_lines, splitsQty, this);
        splitting_order_lines.addAll(realm.copyFromRealm(order.getOrder_lines()));
        for (int i = 0; i < splitting_order_lines.size(); i++){
            splitsQty.add(0);
        }
        popupBinding.productsRv.setAdapter(splittingOrderLineAdapter);


        popupBinding.proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(splitted_order_lines.size() > 0) {
                    //calculation splitting order line (Order A)
                    ArrayList<Order_Line> removedOrderLine = new ArrayList<>();
                    for (int i = 0; i < splitsQty.size(); i++) {
                        if (splitsQty.get(i) == splitting_order_lines.get(i).getQty()) {
                            removedOrderLine.add(splitting_order_lines.get(i));
                        } else if ((splitsQty.get(i) < splitting_order_lines.get(i).getQty()) && (splitsQty.get(i) > 0)) {
                            splitting_order_lines.set(
                                    i,
                                    updateQty(splitting_order_lines.get(i), splitting_order_lines.get(i).getQty() - splitsQty.get(i))
                            );
                        }
                    }

                    for (int i = 0; i < removedOrderLine.size(); i++) {
                        for (int x = 0; x < splitting_order_lines.size(); x++) {
                            if (removedOrderLine.get(i).getLocal_order_line_id() == splitting_order_lines.get(x).getLocal_order_line_id()) {
                                splitting_order_lines.remove(x);
                            }
                        }
                    }

                    //create splitted order (Order B)
                    Order splitted_order = new Order(currentOrder);
                    Number id1 = realm.where(Order.class).max("local_order_id");
                    int nextOrderID = -1;
                    if (id1 == null) {
                        nextOrderID = 1;
                    } else {
                        nextOrderID = id1.intValue() + 1;
                    }
                    splitted_order.setLocal_order_id(nextOrderID);
                    splitted_order.setOrder_id(-1);
                    splitted_order.setCustomer(null);
                    splitted_order.setPos_reference(null);
                    splitted_order.setNote(null);
                    splitted_order.setCustomer_count(1);
                    splitted_order.setName(null);
                    splitted_order.setTip_amount(0);
                    splitted_order.setIs_tipped(false);
                    splitted_order.setPartner_id(-1);
                    splitted_order.setDisplay_tip_amount(currencyDisplayFormat(0.00));

                    //update splitted order line id (Order Line B)
                    Number id = realm.where(Order_Line.class).max("local_order_line_id");
                    int nextID = -1;
                    if (id == null) {
                        nextID = 1;
                    } else {
                        nextID = id.intValue() + 1;
                    }

                    ArrayList<Integer> splitted_order_lines_id = new ArrayList<>();
                    for (int i = 0; i < splitted_order_lines.size(); i++) {
                        splitted_order_lines_id.add(splitted_order_lines.get(i).getOrder_line_id());
                    }

                    splitted_order_lines.get(0).setLocal_order_line_id(nextID);
                    splitted_order_lines.get(0).setOrder_line_id(-1);
                    splitted_order_lines.get(0).setOrder(splitted_order);
                    for (int i = 1; i < splitted_order_lines.size(); i++) {
                        splitted_order_lines.get(i).setLocal_order_line_id(
                                (splitted_order_lines.get(i - 1).getLocal_order_line_id() + 1)
                        );
                        splitted_order_lines.get(i).setOrder_line_id(-1);
                        splitted_order_lines.get(i).setOrder(splitted_order);
                    }

                    //calculation + insert splitted order (Order B)
                    updateOrderTotalAmount(splitted_order_lines, splitted_order);

                    //update order line   (Order Line A) and (Order Line B)
                    for (int i = 0; i < removedOrderLine.size(); i++) {
                        Order_Line removedOrderLine_result = realm.where(Order_Line.class)
                                .equalTo("local_order_line_id", removedOrderLine.get(i).getLocal_order_line_id())
                                .findFirst();
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                removedOrderLine_result.deleteFromRealm();
                            }
                        });
                    }
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.insertOrUpdate(splitting_order_lines);
                            realm.insertOrUpdate(splitted_order_lines);
                        }
                    });

                    //calculation + update splitting order (Order A)
                    updateOrderTotalAmount(splitting_order_lines, currentOrder);

                    //update share preference
                    currentCustomerSharePreferenceEdit.putInt("customerID", -1);
                    currentCustomerSharePreferenceEdit.putString("customerName", null);
                    currentCustomerSharePreferenceEdit.putString("customerEmail", null);
                    currentCustomerSharePreferenceEdit.putString("customerPhoneNo", null);
                    currentCustomerSharePreferenceEdit.putString("customerIdentityNo", null);
                    currentCustomerSharePreferenceEdit.putString("customerBirthdate", null);
                    currentCustomerSharePreferenceEdit.commit();

                    int orderTypePosition = 0;
                    if (splitted_order.getTable() != null) {
                        orderTypePosition = 1;
                    }
                    currentOrderSharePreferenceEdit.putInt("orderTypePosition", orderTypePosition);
                    currentOrderSharePreferenceEdit.putInt("orderingState", 1);
                    currentOrderSharePreferenceEdit.putInt("localOrderId", splitted_order.getLocal_order_id());
                    currentOrderSharePreferenceEdit.commit();

                    //call api
                    if (!NetworkUtils.isNetworkAvailable(contextpage)) {
                        Toast.makeText(contextpage, "No Internet Connection.", Toast.LENGTH_SHORT).show();
                        startActivity(getIntent());
                        finish();
                    } else {
                        new apiSplitOrder(currentOrder.getOrder_id(), currentOrder.getLocal_order_id(),
                                splitted_order.getLocal_order_id(), splitted_order_lines_id).execute();
                    }

                    popup.dismiss();
                }else{
                    Toast.makeText(contextpage, "Please select product for splitting", Toast.LENGTH_SHORT).show();
                }
            }
        });
        popupBinding.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.dismiss();
            }
        });

        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                splitBillViewModel.setTotalSplitBill(currencyDisplayFormat(0.00));
                splitting_order_lines.clear();
            }
        });


        popup.showAtLocation(binding.getRoot(), Gravity.CENTER, 0, 0);
        //blur background
        View container = (View) popup.getContentView().getParent();
        WindowManager wm = (WindowManager) PaymentPage.this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);
    }
    @Override
    public void onSplitOrderLineClick(int position, int qty) {
        splitsQty.set(position, qty);
        Order_Line splitting_order_line = new Order_Line(splitting_order_lines.get(position));

        updateSplitOrderLine(splitting_order_line, qty);
    }
    private void showSplitBillInsertQty(Order_Line splitting_order_line, int qty, int splitsQtyPosition){
        PopupWindow popup = new PopupWindow(contextpage);
        SplitOrderQtyPopupBinding popupBinding = SplitOrderQtyPopupBinding.inflate(getLayoutInflater());
//        View layout = getLayoutInflater().inflate(R.layout.cart_order_add_discount_popup, null);
        popup.setContentView(popupBinding.getRoot());
        // Set content width and height
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // Closes the popup window when touch outside of it - when looses focus
        popup.setOutsideTouchable(false);
        popup.setFocusable(true);
        // Show anchored to button
        popup.setElevation(8);
        popup.setBackgroundDrawable(null);

        popupBinding.productNameTv.setText(splitting_order_line.getProduct().getName());
        popupBinding.qtyEt.setText(String.valueOf(qty));

        popupBinding.positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int latestQty = 0;
                if(popupBinding.qtyEt.getText().length() > 0)
                    latestQty = Integer.valueOf(popupBinding.qtyEt.getText().toString());

                if(latestQty <= splitting_order_line.getQty()) {
                    splitsQty.set(splitsQtyPosition, latestQty);
                    updateSplitOrderLine(splitting_order_line, latestQty);
                    splittingOrderLineAdapter.notifyDataSetChanged();
                    popup.dismiss();
                }else{
                    Toast.makeText(contextpage, "The quantity has exceeded", Toast.LENGTH_SHORT).show();
                }
            }
        });
        popupBinding.negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.dismiss();
            }
        });


        popup.showAtLocation(binding.getRoot(), Gravity.CENTER, 0, 0);
        //blur background
        View container = (View) popup.getContentView().getParent();
        WindowManager wm = (WindowManager) PaymentPage.this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);
    }
    @Override
    public void onSplitOrderLineLongClick(int position) {
        int qty = splitsQty.get(position);
        Order_Line splitting_order_line = new Order_Line(splitting_order_lines.get(position));

        showSplitBillInsertQty(splitting_order_line, qty, position);
    }

    private void updateSplitOrderLine(Order_Line splitting_order_line, int latestQty){
        splitting_order_line = updateQty(splitting_order_line, latestQty);
        if(splitted_order_lines.size() > 0) {
            boolean splitted = false;
            for(int i = 0; i < splitted_order_lines.size(); i++) {
                if(splitted_order_lines.get(i).getLocal_order_line_id() == splitting_order_line.getLocal_order_line_id()){
                    if(latestQty > 0)
                        splitted_order_lines.set(i, splitting_order_line);
                    else
                        splitted_order_lines.remove(i);
                    splitted = true;
                }
            }
            if(splitted == false){
                splitted_order_lines.add(splitting_order_line);
            }
        }else{
            splitted_order_lines.add(splitting_order_line);
        }


        double totalSplitBill = 0.0;
        for(int i = 0; i < splitted_order_lines.size(); i++){
            totalSplitBill += splitted_order_lines.get(i).getPrice_subtotal_incl();
        }

        splitBillViewModel.setTotalSplitBill(currencyDisplayFormat(totalSplitBill));
    }
    private Order_Line updateQty(Order_Line order_lineToUpdate, int qty){
        RealmResults<Product_Tax> product_tax_results = realm.where(Product_Tax.class)
                .equalTo("product_tmpl_id", order_lineToUpdate.getProduct().getProduct_tmpl_id()).findAll();
        ArrayList<Product_Tax> product_taxes = new ArrayList<>();
        product_taxes.addAll(realm.copyFromRealm(product_tax_results));

        double price_unit = order_lineToUpdate.getPrice_unit();
        double amount_discount = 0;
        if(order_lineToUpdate.getDiscount_type() != null) {
            if (order_lineToUpdate.getDiscount_type().equalsIgnoreCase("percentage")) {
                amount_discount = (price_unit * order_lineToUpdate.getDiscount()) / 100;
            } else if (order_lineToUpdate.getDiscount_type().equalsIgnoreCase("fixed_amount")) {
                amount_discount = order_lineToUpdate.getDiscount();
            }
        }
        double price_before_discount =
                calculate_price_unit_excl_tax(order_lineToUpdate.getProduct(), price_unit) * qty;
        double price_unit_excl_tax =
                calculate_price_unit_excl_tax(order_lineToUpdate.getProduct(), (price_unit - amount_discount));
        double price_subtotal = price_unit_excl_tax * qty;
        double price_subtotal_incl = calculate_price_subtotal_incl(product_taxes, price_subtotal);
        double total_cost = order_lineToUpdate.getProduct().getStandard_price() * qty;

        order_lineToUpdate.setPrice_subtotal(price_subtotal);
        order_lineToUpdate.setDisplay_price_subtotal(currencyDisplayFormat(price_subtotal));
        order_lineToUpdate.setPrice_subtotal_incl(price_subtotal_incl);
        order_lineToUpdate.setDisplay_price_subtotal_incl(currencyDisplayFormat(price_subtotal_incl));
        order_lineToUpdate.setQty(qty);
        order_lineToUpdate.setPrice_before_discount(price_before_discount);
        order_lineToUpdate.setDisplay_price_before_discount(currencyDisplayFormat(price_before_discount));
        order_lineToUpdate.setTotal_cost(total_cost);
        order_lineToUpdate.setDisplay_total_cost(currencyDisplayFormat(total_cost));

        return order_lineToUpdate;
    }

    public class apiSplitOrder extends AsyncTask<String, String, String> {
        private ProgressDialog pd = null;
        private int order_id, local_order_id, split_local_order_id;

        private ArrayList<Order_Line> update_order_lines, update_split_order_lines;
        private Order update_splitting_order, update_splitted_order;
        private ArrayList<Integer> splitted_order_lines_id;

        public apiSplitOrder(int order_id, int local_order_id, int split_local_order_id,
                             ArrayList<Integer> splitted_order_lines_id){
            this.order_id = order_id;
            this.local_order_id = local_order_id;
            this.split_local_order_id = split_local_order_id;
            this.splitted_order_lines_id = splitted_order_lines_id;
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
                update_splitting_order = realm.copyFromRealm(result);
            else
                update_splitting_order = null;

            Order split_result = realm.where(Order.class).equalTo("local_order_id", split_local_order_id).findFirst();
            if(split_result != null)
                update_splitted_order = realm.copyFromRealm(split_result);
            else
                update_splitted_order = null;

            RealmResults<Order_Line> results_order_lines = realm.where(Order_Line.class)
                    .equalTo("order.local_order_id", local_order_id).findAll();
            update_order_lines = new ArrayList<>();
            update_order_lines.addAll(realm.copyFromRealm(results_order_lines));

            RealmResults<Order_Line> results_split_order_lines = realm.where(Order_Line.class)
                    .equalTo("order.local_order_id", split_local_order_id).findAll();
            update_split_order_lines = new ArrayList<>();
            update_split_order_lines.addAll(realm.copyFromRealm(results_split_order_lines));
        }

        @Override
        protected String doInBackground(String... strings) {
            long timeBefore = Calendar.getInstance().getTimeInMillis();
            String connection_error = "";

            String urlParameters = "&order_id=" + order_id;
            for(int i = 0; i < update_split_order_lines.size(); i++) {
                urlParameters += "&order_lines[" + splitted_order_lines_id.get(i) + "]="
                            + update_split_order_lines.get(i).getQty();
            }

            //Testing (check error)
            urlParameters += "&dev=1";

            byte[] postData = urlParameters.getBytes(Charset.forName("UTF-8"));
            int postDataLength = postData.length;

            String url = "https://www.c3rewards.com/api/merchant/?module=pos&action=split_order";
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
                        JSONArray jo_order_arr = jresult.getJSONArray("orders");

                        for(int i = 0; i < jo_order_arr.length(); i++){
                            JSONObject jo_order = jo_order_arr.getJSONObject(i);
                            if(jo_order.getInt("order_id") == order_id){
                                if(update_splitting_order != null){
                                    double tip_amount = 0;
                                    boolean is_tipped = false;
                                    int partner_id = -1, customer_count = 1;
                                    String discount_type = null;
                                    if(jo_order.getString("is_tipped").length() > 0)
                                        is_tipped = jo_order.getBoolean("is_tipped");
                                    if(jo_order.getString("tip_amount").length() > 0)
                                        tip_amount = jo_order.getDouble("tip_amount");
                                    if(jo_order.getString("partner_id").length() > 0)
                                        partner_id = jo_order.getInt("partner_id");
                                    if(jo_order.getString("discount_type").length() > 0)
                                        discount_type = jo_order.getString("discount_type");
                                    if(jo_order.getString("customer_count").length() > 0){
                                        customer_count = jo_order.getInt("customer_count");
                                    }

                                    update_splitting_order = new Order(local_order_id, jo_order.getInt("order_id"), jo_order.getString("name"),
                                        jo_order.getString("date_order"), jo_order.getString("pos_reference"),
                                        jo_order.getString("state"), jo_order.getString("state_name"),
                                        jo_order.getDouble("amount_tax"), jo_order.getDouble("amount_total"),
                                        jo_order.getDouble("amount_paid"), jo_order.getDouble("amount_return"),
                                        jo_order.getDouble("amount_subtotal"), tip_amount,
                                        jo_order.getString("display_amount_tax"), jo_order.getString("display_amount_total"),
                                        jo_order.getString("display_amount_paid"), jo_order.getString("display_amount_return"),
                                        jo_order.getString("display_amount_subtotal"), jo_order.getString("display_tip_amount"),
                                        is_tipped, update_splitting_order.getTable(), update_splitting_order.getCustomer(), jo_order.getString("note"),
                                        jo_order.getDouble("discount"), discount_type, customer_count,
                                        jo_order.getInt("session_id"), jo_order.getInt("user_id"),
                                        jo_order.getInt("company_id"), partner_id);
                                }
                                JSONArray jo_order_line_arr = jo_order.getJSONArray("order_lines");
                                for(int x = 0; x < jo_order_line_arr.length(); x++){
                                    JSONObject jo_order_line = jo_order_line_arr.getJSONObject(x);
                                    update_order_lines.get(x).setOrder(update_splitting_order);
                                    update_order_lines.get(x).setOrder_line_id(jo_order_line.getInt("order_line_id"));
                                    update_order_lines.get(x).setName(jo_order_line.getString("name"));
                                    update_order_lines.get(x).setPrice_unit(jo_order_line.getDouble("price_unit"));
                                    update_order_lines.get(x).setPrice_subtotal(jo_order_line.getDouble("price_subtotal"));
                                    update_order_lines.get(x).setPrice_subtotal_incl(jo_order_line.getDouble("price_subtotal_incl"));
                                    update_order_lines.get(x).setDisplay_price_unit(jo_order_line.getString("display_price_unit"));
                                    update_order_lines.get(x).setDisplay_price_subtotal(jo_order_line.getString("display_price_subtotal"));
                                    update_order_lines.get(x).setDisplay_price_subtotal_incl(jo_order_line.getString("display_price_subtotal_incl"));
                                    update_order_lines.get(x).setFull_product_name(jo_order_line.getString("full_product_name"));
                                    update_order_lines.get(x).setCustomer_note(jo_order_line.getString("customer_note"));
                                    double total_cost = 0;
                                    if(jo_order_line.getString("total_cost").length() > 0){
                                        total_cost = jo_order_line.getDouble("total_cost");
                                    }
                                    update_order_lines.get(x).setTotal_cost(total_cost);
                                    update_order_lines.get(x).setDisplay_total_cost(jo_order_line.getString("display_total_cost"));
                                    double price_extra = 0.0;
                                    if(jo_order_line.getString("price_extra").length() > 0){
                                        price_extra = jo_order_line.getDouble("price_extra");
                                    }
                                    update_order_lines.get(x).setPrice_extra(price_extra);
                                    update_order_lines.get(x).setDisplay_price_extra(jo_order_line.getString("display_price_extra"));
                                    double discount_order_line = 0.0;
                                    if(jo_order_line.getString("discount").length() > 0){
                                        discount_order_line = jo_order_line.getDouble("discount");
                                    }
                                    String discount_type_order_line = null;
                                    if(jo_order_line.getString("discount_type").length() > 0){
                                        discount_type_order_line = jo_order_line.getString("discount_type");
                                    }
                                    update_order_lines.get(x).setDiscount(discount_order_line);
                                    update_order_lines.get(x).setDiscount_type(discount_type_order_line);
                                    update_order_lines.get(x).setDisplay_discount(jo_order_line.getString("display_discount"));
                                }
                            }else{
                                if(update_splitted_order != null){
                                    double tip_amount = 0;
                                    boolean is_tipped = false;
                                    int partner_id = -1, customer_count = 1;
                                    String discount_type = null;
                                    if(jo_order.getString("is_tipped").length() > 0)
                                        is_tipped = jo_order.getBoolean("is_tipped");
                                    if(jo_order.getString("tip_amount").length() > 0)
                                        tip_amount = jo_order.getDouble("tip_amount");
                                    if(jo_order.getString("partner_id").length() > 0)
                                        partner_id = jo_order.getInt("partner_id");
                                    if(jo_order.getString("discount_type").length() > 0)
                                        discount_type = jo_order.getString("discount_type");
                                    if(jo_order.getString("customer_count").length() > 0){
                                        customer_count = jo_order.getInt("customer_count");
                                    }

                                    update_splitted_order = new Order(split_local_order_id, jo_order.getInt("order_id"), jo_order.getString("name"),
                                        jo_order.getString("date_order"), jo_order.getString("pos_reference"),
                                        jo_order.getString("state"), jo_order.getString("state_name"),
                                        jo_order.getDouble("amount_tax"), jo_order.getDouble("amount_total"),
                                        jo_order.getDouble("amount_paid"), jo_order.getDouble("amount_return"),
                                        jo_order.getDouble("amount_subtotal"), tip_amount,
                                        jo_order.getString("display_amount_tax"), jo_order.getString("display_amount_total"),
                                        jo_order.getString("display_amount_paid"), jo_order.getString("display_amount_return"),
                                        jo_order.getString("display_amount_subtotal"), jo_order.getString("display_tip_amount"),
                                        is_tipped, update_splitted_order.getTable(), null, jo_order.getString("note"),
                                        jo_order.getDouble("discount"), discount_type, customer_count,
                                        jo_order.getInt("session_id"), jo_order.getInt("user_id"),
                                        jo_order.getInt("company_id"), partner_id);

                                    JSONArray jo_order_line_arr = jo_order.getJSONArray("order_lines");
                                    for(int x = 0; x < jo_order_line_arr.length(); x++){
                                        JSONObject jo_order_line = jo_order_line_arr.getJSONObject(x);
                                        update_split_order_lines.get(x).setOrder(update_splitted_order);
                                        update_split_order_lines.get(x).setOrder_line_id(jo_order_line.getInt("order_line_id"));
                                        update_split_order_lines.get(x).setName(jo_order_line.getString("name"));
                                        update_split_order_lines.get(x).setPrice_unit(jo_order_line.getDouble("price_unit"));
                                        update_split_order_lines.get(x).setPrice_subtotal(jo_order_line.getDouble("price_subtotal"));
                                        update_split_order_lines.get(x).setPrice_subtotal_incl(jo_order_line.getDouble("price_subtotal_incl"));
                                        update_split_order_lines.get(x).setDisplay_price_unit(jo_order_line.getString("display_price_unit"));
                                        update_split_order_lines.get(x).setDisplay_price_subtotal(jo_order_line.getString("display_price_subtotal"));
                                        update_split_order_lines.get(x).setDisplay_price_subtotal_incl(jo_order_line.getString("display_price_subtotal_incl"));
                                        update_split_order_lines.get(x).setFull_product_name(jo_order_line.getString("full_product_name"));
                                        update_split_order_lines.get(x).setCustomer_note(jo_order_line.getString("customer_note"));
                                        double total_cost = 0;
                                        if(jo_order_line.getString("total_cost").length() > 0){
                                            total_cost = jo_order_line.getDouble("total_cost");
                                        }
                                        update_split_order_lines.get(x).setTotal_cost(total_cost);
                                        update_split_order_lines.get(x).setDisplay_total_cost(jo_order_line.getString("display_total_cost"));
                                        double price_extra = 0.0;
                                        if(jo_order_line.getString("price_extra").length() > 0){
                                            price_extra = jo_order_line.getDouble("price_extra");
                                        }
                                        update_split_order_lines.get(x).setPrice_extra(price_extra);
                                        update_split_order_lines.get(x).setDisplay_price_extra(jo_order_line.getString("display_price_extra"));
                                        double discount_order_line = 0.0;
                                        if(jo_order_line.getString("discount").length() > 0){
                                            discount_order_line = jo_order_line.getDouble("discount");
                                        }
                                        String discount_type_order_line = null;
                                        if(jo_order_line.getString("discount_type").length() > 0){
                                            discount_type_order_line = jo_order_line.getString("discount_type");
                                        }

                                        update_split_order_lines.get(x).setDiscount(discount_order_line);
                                        update_split_order_lines.get(x).setDiscount_type(discount_type_order_line);
                                        update_split_order_lines.get(x).setDisplay_discount(jo_order_line.getString("display_discount"));
                                    }
                                }
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
            System.out.println("Split api time taken: " + (timeAfter - timeBefore) + "ms");
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(!NetworkUtils.isNetworkAvailable(contextpage)){
                Toast.makeText(contextpage, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }else{
                if((update_splitted_order != null) && (update_splitting_order != null)){
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.insertOrUpdate(update_splitted_order);
                            realm.insertOrUpdate(update_splitting_order);

                            realm.insertOrUpdate(update_split_order_lines);
                            realm.insertOrUpdate(update_order_lines);
                        }
                    });
                }
            }

            if (pd != null)
                pd.dismiss();

            startActivity(getIntent());
            finish();
        }
    }



    private void showOrderProducts() {
        PopupWindow popup = new PopupWindow(contextpage);
        PaymentOrderLinePopupBinding popupBinding = PaymentOrderLinePopupBinding.inflate(getLayoutInflater());
//        View layout = getLayoutInflater().inflate(R.layout.payment_order_line_popup, null);
        popup.setContentView(popupBinding.getRoot());
        // Set content width and height
        popup.setHeight(1350);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // Closes the popup window when touch outside of it - when looses focus
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        // Show anchored to button
        popup.setElevation(8);
        popup.setBackgroundDrawable(null);
        //RecyclerView
        PaymentOrderLineAdapter paymentOrderLineAdapter = new PaymentOrderLineAdapter(payment_order_lines);
        popupBinding.paymentOrderProductsRv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        popupBinding.paymentOrderProductsRv.setHasFixedSize(true);
        popupBinding.paymentOrderProductsRv.setAdapter(paymentOrderLineAdapter);

        //other setting
        popupBinding.paymentOrderPopupOrderId.setText("#" + currentOrder.getOrder_id());
        if(currentCustomer != null) {
            popupBinding.paymentOrderPopupCustomerName.setText(currentCustomer.getCustomer_name());
            popupBinding.paymentOrderPopupCustomerName.setVisibility(View.VISIBLE);
        }else{
            popupBinding.paymentOrderPopupCustomerName.setVisibility(View.GONE);
        }
        if(currentOrder.getTable() != null){
            popupBinding.paymentOrderPopupType.setText("Dine-in - " + currentOrder.getTable().getFloor().getName() +
                                                    " / " + currentOrder.getTable().getName());
        }else{
            popupBinding.paymentOrderPopupType.setText("Takeaway");
        }
        popupBinding.customerCount.setText("" + currentOrder.getCustomer_count());

        popup.showAtLocation(binding.getRoot(), Gravity.CENTER, 0, 0);
        //blur background
        View container = (View) popup.getContentView().getParent();
        WindowManager wm = (WindowManager) PaymentPage.this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);
    }

    private void tableOccupiedToVacant(Table table){
        table.setState("V");
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(table);
            }
        });
    }

    private void showCashInOut() {
        PopupWindow popup = new PopupWindow(contextpage);
        CashInOutPopupBinding popupBinding = CashInOutPopupBinding.inflate(getLayoutInflater());
//        View layout = getLayoutInflater().inflate(R.layout.cash_in_out_popup, null);
        popup.setContentView(popupBinding.getRoot());
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
        //blur background
        View container = (View) popup.getContentView().getParent();
        WindowManager wm = (WindowManager) PaymentPage.this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);

        popupBinding.cashInOutCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.dismiss();
                Toast.makeText(contextpage, "Cancel", Toast.LENGTH_SHORT).show();
            }
        });
        popupBinding.cashInOutConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.dismiss();
                Toast.makeText(contextpage, "Confirm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddTipPopup(View view) {
        PopupWindow popup = new PopupWindow(contextpage);
        PaymentAddTipPopupBinding popupBinding = PaymentAddTipPopupBinding.inflate(getLayoutInflater());
//        View layout = getLayoutInflater().inflate(R.layout.payment_add_tip_popup, null);
        popup.setContentView(popupBinding.getRoot());
        // Set content width and height
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // Closes the popup window when touch outside of it - when looses focus
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        // Show anchored to button
        popup.setElevation(8);
        popup.setBackgroundDrawable(null);
        binding.textView9.setVisibility(View.VISIBLE);
        binding.paymentTip.setVisibility(View.VISIBLE);
        binding.paymentTipCancelBtn.setVisibility(View.VISIBLE);
        //binding.cartInclude.tvDiscount.setTextColor(contextpage.getResources().getColor(R.color.darkOrange));
        popup.showAsDropDown(binding.paymentBarAddTip, -520, -180);

        //Popup Buttons
        popupBinding.addTipPopupEt.setText(binding.getPaymentPageViewModel().getPayment_tip().getValue());

        popupBinding.addTipPopupNegativeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                popup.dismiss();
                Toast.makeText(contextpage, "Cancel", Toast.LENGTH_SHORT).show();
            }
        });

        popupBinding.addTipPopupPositiveBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String tip_amount = String.format("%.2f", Double.valueOf(popupBinding.addTipPopupEt.getText().toString()));
                binding.getPaymentPageViewModel().setPayment_tip(tip_amount);
                double amount_total = binding.getPaymentPageViewModel().getAmount_total();
                binding.paymentGrandTotal.setText(String.format("%.2f", amount_total));
                binding.paymentBarPayableAmount.setText(currencyDisplayFormat(amount_total));
                currentOrder.setAmount_total(amount_total);
                currentOrder.setDisplay_amount_total(currencyDisplayFormat(amount_total));
                popup.dismiss();
                Toast.makeText(contextpage, "Tip Added", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRefreshPopup(View view) {
        PopupWindow popup = new PopupWindow(contextpage);
        ToolbarSyncPopupBinding popupBinding = ToolbarSyncPopupBinding.inflate(getLayoutInflater());
        //View layout = getLayoutInflater().inflate(R.layout.toolbar_sync_popup, null);
        popup.setContentView(popupBinding.getRoot());
        // Set content width and height
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // Closes the popup window when touch outside of it - when looses focus
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        // Show anchored to button
        popup.setElevation(8);
        popup.setBackgroundDrawable(null);
        popup.showAsDropDown(binding.toolbarLayoutIncl.toolbarRefresh, -120, 0);


        popupBinding.syncProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(contextpage, "refresh / sync products", Toast.LENGTH_SHORT).show();
                popup.dismiss();
            }
        });

        popupBinding.syncTransactionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(contextpage, "refresh / sync transactions", Toast.LENGTH_SHORT).show();
                popup.dismiss();
            }
        });
    }

    private boolean is_balanceLargerOrEqualToZero(){
        double balance_amount = Double.valueOf(binding.getPaymentPageViewModel().getPayment_order_detail_balance().getValue());
        if(balance_amount >= 0){
            return true;
        }
        return false;
    }

    @Override
    public void onPaymentRemove(int position) {
        Payment payment = payments.get(position);

        //remove UI payments
        payments.remove(position);
        paymentAdapter.notifyDataSetChanged();

        //update balance & credit
        viewModel.removeOnePayment(payment);

//        Toast.makeText(contextpage, "Name = " + payment.getPayment_method().getName()
//                + "\nAmount = " + payment.getAmount(), Toast.LENGTH_SHORT).show();
    }

    private void updateOrderTotalAmount(ArrayList<Order_Line> order_lines, Order order){
        double order_subtotal = 0.0, total_price_subtotal_incl = 0.0, amount_total = 0.0, amount_order_discount = 0.0;
        double total_tax_amount = 0.0;

        for(int i = 0; i < order_lines.size(); i++){
            order_subtotal += order_lines.get(i).getPrice_subtotal();
            total_price_subtotal_incl += order_lines.get(i).getPrice_subtotal_incl();
        }
        amount_total = total_price_subtotal_incl;
        if(order.getDiscount_type() != null){
            if(order.getDiscount_type().equalsIgnoreCase("percentage")){
                amount_order_discount = total_price_subtotal_incl * (order.getDiscount()/100);
            }else if(order.getDiscount_type().equalsIgnoreCase("fixed_amount")){
                amount_order_discount = order.getDiscount();
            }
            amount_total -= amount_order_discount;
        }

        total_tax_amount = totalAllOrderLineTax(order_lines);

        order.setAmount_total(amount_total);
        order.setDisplay_amount_total(currencyDisplayFormat(amount_total));
        order.setAmount_tax(total_tax_amount);
        order.setDisplay_amount_tax(currencyDisplayFormat(total_tax_amount));
        order.setAmount_subtotal(order_subtotal);
        order.setDisplay_amount_subtotal(currencyDisplayFormat(order_subtotal));

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(order);
            }
        });
    }
    private double totalAllOrderLineTax(ArrayList<Order_Line> order_lines){
        double total_tax_amount = 0.0;

        for(int i = 0; i < order_lines.size(); i++){
            double amount_tax = order_lines.get(i).getPrice_subtotal_incl() - order_lines.get(i).getPrice_subtotal();
            total_tax_amount += amount_tax;
        }

        return total_tax_amount;
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
        price_subtotal_incl = price_subtotal + total_taxes;

        return price_subtotal_incl;
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