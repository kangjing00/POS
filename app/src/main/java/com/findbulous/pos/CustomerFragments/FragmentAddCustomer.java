package com.findbulous.pos.CustomerFragments;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.findbulous.pos.Customer;
import com.findbulous.pos.R;
import com.findbulous.pos.databinding.FragmentAddCustomerBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;

public class FragmentAddCustomer extends Fragment {

    private FragmentAddCustomerBinding binding;
    private Realm realm;
    final Calendar myCalendar= Calendar.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_customer, container, false);
        View view = binding.getRoot();

        realm = Realm.getDefaultInstance();

        if(getArguments() != null){
            //case: Update Customer detail action
            binding.addOrUpdateCustomerBtn.setText("Update Customer");
            binding.addOrUpdateCustomerBtn.setIcon(getResources().getDrawable(R.drawable.ic_update_user));

            int customer_id = getArguments().getInt("customer_id");
            Customer customer = realm.where(Customer.class).equalTo("customer_id", customer_id).findFirst();
            binding.addCustomerNameEt.setText(customer.getCustomer_name());
            binding.addCustomerPhoneEt.setText(customer.getCustomer_phoneNo());
            binding.addCustomerEmailEt.setText(customer.getCustomer_email());
        }


        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH,day);
                updateDateEt();
            }
        };
        binding.addCustomerBirthdateEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(getContext(), R.style.DatePicker,
                        date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                dialog.getDatePicker().setMaxDate(new Date().getTime());

                dialog.show();
                dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(getContext().getResources().getColor(R.color.black));
                dialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(getContext().getResources().getColor(R.color.black));
            }
        });

        binding.addOrUpdateCustomerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputCheck();
//                if(getArguments() == null){
//                    //Add Customer
//                    addNewCustomer();
//                }else{
//                    //Update Customer detail
//                    updateCustomer();
//                }
            }
        });

        return view;
    }

    private void addNewCustomer(){
        Number id = realm.where(Customer.class).max("customer_id");

        int nextID = -1;
        System.out.println(id);
        if(id == null){
            nextID = 1;
        }else{
            nextID = id.intValue() + 1;
        }

        if(inputCheck()) {
            String customerName = binding.addCustomerNameEt.getText().toString();
            String phoneNo = binding.addCustomerPhoneEt.getText().toString();
            String email = binding.addCustomerEmailEt.getText().toString();
            String identityNo = binding.addCustomerIcEt.getText().toString();
            String birthdate = binding.addCustomerBirthdateEt.getText().toString();
            Customer newCustomer = new Customer(nextID, customerName, email, phoneNo, identityNo, birthdate);

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.insertOrUpdate(newCustomer);
                }
            });
        }
    }
    private void updateCustomer(){
        int customer_id = getArguments().getInt("customer_id");
        Customer customer = realm.where(Customer.class).equalTo("customer_id", customer_id).findFirst();
        Customer updatedCustomer = realm.copyFromRealm(customer);
        if(inputCheck()) {
            updatedCustomer.setCustomer_name(binding.addCustomerNameEt.getText().toString());
            updatedCustomer.setCustomer_phoneNo(binding.addCustomerPhoneEt.getText().toString());
            updatedCustomer.setCustomer_email(binding.addCustomerEmailEt.getText().toString());
            updatedCustomer.setCustomer_identityNo(binding.addCustomerIcEt.getText().toString());
            updatedCustomer.setCustomer_birthdate(binding.addCustomerBirthdateEt.getText().toString());

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.insertOrUpdate(updatedCustomer);
                }
            });
        }
    }

    private void updateDateEt(){
        String myFormat="yyyy-MM-dd";
        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormat, Locale.getDefault());
        binding.addCustomerBirthdateEt.setText(dateFormat.format(myCalendar.getTime()));
    }
    private boolean inputCheck(){
        if(nullOrEmptyCheck(binding.addCustomerNameEt) && nullOrEmptyCheck(binding.addCustomerPhoneEt)
                && nullOrEmptyCheck(binding.addCustomerEmailEt) && nullOrEmptyCheck(binding.addCustomerIcEt)
                && nullOrEmptyCheck(binding.addCustomerBirthdateEt)){
            if(phoneNoCheck(binding.addCustomerPhoneEt)) {
                if (emailCheck(binding.addCustomerEmailEt)) {
                   if(identityNoCheck(binding.addCustomerIcEt)){
                       return true;
                   }else{
                       Toast.makeText(getContext(), "Invalid Identity Number", Toast.LENGTH_SHORT).show();
                       return false;
                   }
                }else{
                    Toast.makeText(getContext(), "Invalid Email Address", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }else{
                Toast.makeText(getContext(), "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else{
            Toast.makeText(getContext(), "Please fill in all the details", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean phoneNoCheck(EditText phone){
        if(Patterns.PHONE.matcher(phone.getText().toString()).matches())
            return true;
        return false;
    }
    private boolean emailCheck(EditText email){
        if(Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches())
            return true;
        return false;
    }
    private boolean identityNoCheck(EditText icNo){
        String regex = "^[0-9]{6}+[-][0-9]{2}+[-][0-9]{4}$";
        if(icNo.getText().toString().matches(regex)){
            return true;
        }
        return false;
    }

    private boolean nullOrEmptyCheck(EditText et){
        if(et.getText().toString().isEmpty())
            return false;
        return true;
    }
}
