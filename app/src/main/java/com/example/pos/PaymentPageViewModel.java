package com.example.pos;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PaymentPageViewModel extends ViewModel {

    private MutableLiveData<String> payment_order_detail_credit, cash_amount_et;

    public PaymentPageViewModel() {
       payment_order_detail_credit = new MutableLiveData<String>("0.00");
       cash_amount_et = new MutableLiveData<String>("0.00");
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

//    public String displayCashAmountEt(String cashAmountFromEt, char keyPadNo){
//        char[] addedCashAmountEt;
//        addedCashAmountEt = new char[cashAmountFromEt.length() + 1];
//        addedCashAmountEt[addedCashAmountEt.length - 1] = keyPadNo;
//        addedCashAmountEt[addedCashAmountEt.length - 3] = '.';
//        for(int i = (addedCashAmountEt.length - 2), j = (cashAmountFromEt.length() - 1); i >= 0; i--, j--){
//            if(i != addedCashAmountEt.length - 3) {
//                if(cashAmountFromEt.charAt(j) == '.'){
//                    j--;
//                }
//                addedCashAmountEt[i] = cashAmountFromEt.charAt(j);
//            }else{
//                j++;
//            }
//        }
//
//        String cashAmountEtBeforeProcess = String.valueOf(addedCashAmountEt);
//
//        return displayCashAmountEt;
//    }

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
}
