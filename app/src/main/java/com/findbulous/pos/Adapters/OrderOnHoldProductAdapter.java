package com.findbulous.pos.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.findbulous.pos.Order_Line;
import com.findbulous.pos.databinding.ViewOrderOnHoldListBinding;
import com.findbulous.pos.databinding.ViewOrderOnHoldProductListBinding;

import java.util.ArrayList;

public class OrderOnHoldProductAdapter extends RecyclerView.Adapter<OrderOnHoldProductAdapter.OrderOnHoldProductViewHolder>{

    private ArrayList<Order_Line> order_lines;
    Context context;

    public class OrderOnHoldProductViewHolder extends RecyclerView.ViewHolder{
        private ViewOrderOnHoldProductListBinding binding;
        public OrderOnHoldProductViewHolder(ViewOrderOnHoldProductListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public OrderOnHoldProductAdapter(ArrayList<Order_Line> order_lines, Context context){
        this.order_lines = order_lines;
        this.context = context;
    }

    @Override
    public OrderOnHoldProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewOrderOnHoldProductListBinding binding = ViewOrderOnHoldProductListBinding.inflate(inflater, parent, false);

        return new OrderOnHoldProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(OrderOnHoldProductViewHolder holder, int position) {
        Order_Line order_line = order_lines.get(position);
        holder.binding.setOrderLine(order_line);
    }

    @Override
    public int getItemCount() {
        return order_lines.size();
    }
}
