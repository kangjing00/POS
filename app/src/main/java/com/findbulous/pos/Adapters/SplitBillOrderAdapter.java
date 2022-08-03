package com.findbulous.pos.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.findbulous.pos.Order_Line;
import com.findbulous.pos.databinding.ViewPaymentProductBinding;
import com.findbulous.pos.databinding.ViewSplitBinding;

import java.util.ArrayList;

public class SplitBillOrderAdapter extends RecyclerView.Adapter<SplitBillOrderAdapter.SplitBillOrderViewHolder>{

    private ArrayList<Order_Line> order_lines;
    private ArrayList<Order_Line> split_order_lines;
    private SplitOrderLineInterface listener;

    public class SplitBillOrderViewHolder extends RecyclerView.ViewHolder{
        private ViewSplitBinding binding;
        public SplitBillOrderViewHolder(ViewSplitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public SplitBillOrderAdapter(ArrayList<Order_Line> order_lines, SplitOrderLineInterface listener){
        this.order_lines = order_lines;
        this.listener = listener;
        split_order_lines = null;
    }

    @Override
    public SplitBillOrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewSplitBinding binding = ViewSplitBinding.inflate(inflater, parent, false);
        return new SplitBillOrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(SplitBillOrderViewHolder holder, int position) {
        Order_Line order_line = order_lines.get(position);
        holder.binding.setOrderLine(order_line);

        if(order_line.getDiscount_type() != null) {
            if ((order_line.getDiscount_type().equalsIgnoreCase("percentage")) ||
                    (order_line.getDiscount_type().equalsIgnoreCase("fixed_amount"))) {
                holder.binding.productDiscount.setVisibility(View.VISIBLE);
                holder.binding.productPriceBeforeDiscount.setVisibility(View.VISIBLE);
            } else { //no discount
                holder.binding.productDiscount.setVisibility(View.INVISIBLE);
                holder.binding.productPriceBeforeDiscount.setVisibility(View.INVISIBLE);
            }
        }else{
            holder.binding.productDiscount.setVisibility(View.INVISIBLE);
            holder.binding.productPriceBeforeDiscount.setVisibility(View.INVISIBLE);
        }

        holder.binding.orderLineCl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Order_Line clickedOrderLine = order_lines.get(holder.getAdapterPosition());
                if(clickedOrderLine.getQty() > 0) { //qty available
                    if (getSplitItemCount() > 0) {
                        if (split_order_lines.contains(clickedOrderLine)) {

                        } else {

                        }
                    } else {

                        //split_order_lines.add(clickedOrderLine);
                    }
                    //split_order_lines.add(clickedOrderLine);
                }
                else{ // over qty

                }


                listener.onSplitOrderLineClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return order_lines.size();
    }

    public int getSplitItemCount(){
        return split_order_lines.size();
    }

    public interface SplitOrderLineInterface{
        void onSplitOrderLineClick(int position);
    }
}
