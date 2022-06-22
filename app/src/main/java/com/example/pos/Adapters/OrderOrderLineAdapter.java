package com.example.pos.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pos.Order_Line;
import com.example.pos.databinding.ViewOrderHistoryListBinding;
import com.example.pos.databinding.ViewPaymentProductBinding;
import java.util.ArrayList;

public class OrderOrderLineAdapter extends RecyclerView.Adapter<OrderOrderLineAdapter.OrderOrderLineViewHolder>{

    private ArrayList<Order_Line> order_lines;

    public class OrderOrderLineViewHolder extends RecyclerView.ViewHolder{
        private ViewPaymentProductBinding binding;

        public OrderOrderLineViewHolder(ViewPaymentProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public OrderOrderLineAdapter(ArrayList<Order_Line> order_lines){
        this.order_lines = order_lines;
    }

    @Override
    public OrderOrderLineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewPaymentProductBinding binding = ViewPaymentProductBinding.inflate(inflater, parent, false);
        return new OrderOrderLineAdapter.OrderOrderLineViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(OrderOrderLineViewHolder holder, int position) {
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
