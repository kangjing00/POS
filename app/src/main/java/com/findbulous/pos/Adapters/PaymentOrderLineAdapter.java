package com.findbulous.pos.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;
import com.findbulous.pos.Order_Line;
import com.findbulous.pos.databinding.ViewCartOrdersBinding;
import com.findbulous.pos.databinding.ViewPaymentProductBinding;

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

        if(!order_line.isHas_discount()){
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
