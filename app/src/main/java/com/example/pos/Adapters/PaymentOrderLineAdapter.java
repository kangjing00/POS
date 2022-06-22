package com.example.pos.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pos.Order_Line;
import com.example.pos.Product;
import com.example.pos.databinding.ViewCartOrdersBinding;
import com.example.pos.databinding.ViewPaymentProductBinding;

import java.util.ArrayList;

public class PaymentOrderLineAdapter extends RecyclerView.Adapter<PaymentOrderLineAdapter.PaymentOrderLineViewHolder> {

    private ArrayList<Order_Line> order_lines;

    public class PaymentOrderLineViewHolder extends RecyclerView.ViewHolder{
        private ViewPaymentProductBinding binding;
        public PaymentOrderLineViewHolder(ViewPaymentProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public PaymentOrderLineAdapter(ArrayList<Order_Line> order_lines){
        this.order_lines = order_lines;
    }

    @Override
    public PaymentOrderLineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewPaymentProductBinding binding = ViewPaymentProductBinding.inflate(inflater, parent, false);
        return new PaymentOrderLineViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(PaymentOrderLineViewHolder holder, int position) {
        Order_Line order_line = order_lines.get(position);
        holder.binding.setOrderLine(order_line);

        int disc = order_line.getDiscount();
        if(disc <= 0){
            holder.binding.paymentProductProductDiscount.setVisibility(View.INVISIBLE);
            holder.binding.paymentProductProductPrice.setVisibility(View.INVISIBLE);
        }else{
            holder.binding.paymentProductProductDiscount.setVisibility(View.VISIBLE);
            holder.binding.paymentProductProductPrice.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return order_lines.size();
    }

}
