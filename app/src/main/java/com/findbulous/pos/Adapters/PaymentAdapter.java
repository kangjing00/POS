package com.findbulous.pos.Adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.findbulous.pos.Order_Line;
import com.findbulous.pos.Payment;
import com.findbulous.pos.databinding.ViewPaymentPaymentsBinding;
import com.findbulous.pos.databinding.ViewPaymentProductBinding;

import java.util.ArrayList;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>{

    private ArrayList<Payment> payments;
    private PaymentInterface listener;

    public class PaymentViewHolder extends RecyclerView.ViewHolder{
        private ViewPaymentPaymentsBinding binding;
        public PaymentViewHolder(ViewPaymentPaymentsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public PaymentAdapter(ArrayList<Payment> payments, PaymentInterface listener){
        this.payments = payments;
        this.listener = listener;
    }


    public PaymentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewPaymentPaymentsBinding binding = ViewPaymentPaymentsBinding.inflate(inflater, parent, false);
        return new PaymentViewHolder(binding);
    }

    public void onBindViewHolder(PaymentViewHolder holder, int position) {
        Payment payment = payments.get(position);
        holder.binding.setPayment(payment);

        holder.binding.paymentCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onPaymentRemove(holder.getAdapterPosition());
            }
        });
    }

    public int getItemCount() {
        return payments.size();
    }

    public interface PaymentInterface{
        void onPaymentRemove(int position);
    }
}
