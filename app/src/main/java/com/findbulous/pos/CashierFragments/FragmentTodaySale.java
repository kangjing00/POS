package com.findbulous.pos.CashierFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.findbulous.pos.Currency;
import com.findbulous.pos.R;
import com.findbulous.pos.databinding.FragmentTodaySaleBinding;

import io.realm.Realm;

public class FragmentTodaySale extends Fragment {

    private FragmentTodaySaleBinding binding;
    private Currency currency;
    private Realm realm;
    //viewmodel

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_today_sale, container, false);
        View view = binding.getRoot();

        realm = Realm.getDefaultInstance();
        currency = realm.copyFromRealm(realm.where(Currency.class).findFirst());

        //Set Amount + currency
        binding.todaySaleOpeningDrawerAmount.setText(currencyDisplayFormat(0.00));
        binding.cashPaymentSale.setText(currencyDisplayFormat(0.00));
        binding.otherPaymentSale.setText(currencyDisplayFormat(0.00));

        return view;
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
}
