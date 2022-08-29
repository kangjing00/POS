package com.findbulous.pos;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PaymentPageSplitBillViewModel extends ViewModel {

    private MutableLiveData<String> totalSplitBill;

    public PaymentPageSplitBillViewModel(){
        totalSplitBill = new MutableLiveData<String>("0.00");
    }

    public MutableLiveData<String> getTotalSplitBill() {
        return totalSplitBill;
    }

    public void setTotalSplitBill(String totalSplitBill) {
        this.totalSplitBill.setValue(totalSplitBill);
    }
}