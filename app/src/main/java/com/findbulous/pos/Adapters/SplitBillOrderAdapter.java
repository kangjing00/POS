package com.findbulous.pos.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.findbulous.pos.Currency;
import com.findbulous.pos.Order;
import com.findbulous.pos.Order_Line;
import com.findbulous.pos.Product;
import com.findbulous.pos.Product_Tax;
import com.findbulous.pos.R;
import com.findbulous.pos.databinding.ViewPaymentProductBinding;
import com.findbulous.pos.databinding.ViewSplitBinding;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class SplitBillOrderAdapter extends RecyclerView.Adapter<SplitBillOrderAdapter.SplitBillOrderViewHolder>{

    private ArrayList<Order_Line> order_lines;
    private ArrayList<Integer> splitsQty;
    private SplitOrderLineInterface listener;

    private Realm realm;
    private Currency currency;

    public class SplitBillOrderViewHolder extends RecyclerView.ViewHolder{
        private ViewSplitBinding binding;
        public SplitBillOrderViewHolder(ViewSplitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public SplitBillOrderAdapter(ArrayList<Order_Line> order_lines, ArrayList<Integer> splitsQty, SplitOrderLineInterface listener){
        this.order_lines = order_lines;
        this.listener = listener;
        this.splitsQty = splitsQty;

        realm = Realm.getDefaultInstance();
        currency = realm.copyFromRealm(realm.where(Currency.class).findFirst());
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
        int splitQty = splitsQty.get(position);
        holder.binding.setSplitQty(splitQty);

        if(splitQty > 0){
            holder.binding.orderLineCl.setBackgroundResource(R.drawable.box_corner_nopadding);
            holder.binding.splitQuantity.setVisibility(View.VISIBLE);
        }else{
            holder.binding.orderLineCl.setBackgroundResource(R.drawable.box_corner_nopadding_white);
            holder.binding.splitQuantity.setVisibility(View.GONE);
        }

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
                int clicked_split_qty = splitsQty.get(holder.getAdapterPosition());

                int orderLineQtyLeft = clickedOrderLine.getQty() - clicked_split_qty;
                if(orderLineQtyLeft > 0) { //qty available
                    splitsQty.set(holder.getAdapterPosition(), clicked_split_qty + 1);
                }
                else{ // over qty
                    splitsQty.set(holder.getAdapterPosition(), 0);
                }

                listener.onSplitOrderLineClick(holder.getAdapterPosition(), splitsQty.get(holder.getAdapterPosition()));
                notifyDataSetChanged();
            }
        });

        holder.binding.orderLineCl.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.onSplitOrderLineLongClick(holder.getAdapterPosition());
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return order_lines.size();
    }

    public interface SplitOrderLineInterface{
        void onSplitOrderLineClick(int position, int qty);
        void onSplitOrderLineLongClick(int position);
    }
}
