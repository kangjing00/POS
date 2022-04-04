package com.example.pos;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PaymentPageViewModel extends ViewModel {

    private MutableLiveData<String> payment_order_detail_credit, cash_amount_et, payment_tip;

    public PaymentPageViewModel() {
       payment_order_detail_credit = new MutableLiveData<String>("0.00");
       cash_amount_et = new MutableLiveData<String>("0.00");
       payment_tip = new MutableLiveData<String>("0.00");
    }





    public void keypadEnter(){
        setPayment_order_detail_credit(cash_amount_et.getValue());
        setAmountEtToZero();
    }

    public void setAmountEtToZero(){
        cash_amount_et.setValue("0.00");
    }

    public void keypadBackSpace() {
        String backSpaceCashAmountEt = cash_amount_et.getValue().substring(0, cash_amount_et.getValue().length() - 1);
        int secondLastIndex = backSpaceCashAmountEt.length() - 2;
        int thirdLastIndex = backSpaceCashAmountEt.length() - 3;
        String cashAmountEtBeforeProcess = swapChar(backSpaceCashAmountEt, secondLastIndex, thirdLastIndex);
        String displayCashAmountEt;
        if(cashAmountEtBeforeProcess.charAt(0) == '.'){
            displayCashAmountEt = "0" + cashAmountEtBeforeProcess;
        }else{
            displayCashAmountEt = cashAmountEtBeforeProcess;
        }
        cash_amount_et.setValue(displayCashAmountEt);
    }

    public void keypadAddNumber(char keyPadNo){
        String addedCashAmountEt = cash_amount_et.getValue() + keyPadNo;
        int thirdLastIndex = addedCashAmountEt.length() - 3;
        int forthLastIndex = addedCashAmountEt.length() - 4;

        String cashAmountEtBeforeProcess = swapChar(addedCashAmountEt, thirdLastIndex, forthLastIndex);
        String displayCashAmountEt;
        if(cashAmountEtBeforeProcess.charAt(0) == '0'){
            displayCashAmountEt = cashAmountEtBeforeProcess.substring(1);
        }else{
            displayCashAmountEt = cashAmountEtBeforeProcess;
        }
        cash_amount_et.setValue(displayCashAmountEt);
    }

    private String swapChar(String string, int position1, int position2){
        char[] result = string.toCharArray();
        char temp = result[position1];
        result[position1] = result[position2];
        result[position2] = temp;
        return String.valueOf(result);
    }


    public MutableLiveData<String> getPayment_order_detail_credit() {
        return payment_order_detail_credit;
    }

    public void setPayment_order_detail_credit(String payment_order_detail_credit) {
        this.payment_order_detail_credit.setValue(payment_order_detail_credit);
    }

    public MutableLiveData<String> getCash_amount_et() {
        return cash_amount_et;
    }

    public void setCash_amount_et(String cash_amount_et) {
        this.cash_amount_et.setValue(cash_amount_et);
    }

    public MutableLiveData<String> getPayment_tip() {
        return payment_tip;
    }

    public void setPayment_tip(String payment_tip) {
        this.payment_tip.setValue(payment_tip);
    }
}
