package com.example.pos.DataAdapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pos.Order_Line;
import com.example.pos.R;
import com.example.pos.databinding.ViewCartOrdersBinding;
import java.util.ArrayList;

public class OrderLineAdapter extends RecyclerView.Adapter<OrderLineAdapter.OrderLineProductViewHolder> {

    private ArrayList<Order_Line> order_lines;
    private OnItemClickListener listener;

    public class OrderLineProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final ViewCartOrdersBinding binding;

        public OrderLineProductViewHolder(ViewCartOrdersBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            listener.onOrderLineClick(this.getAdapterPosition());
        }
    }

    public OrderLineAdapter(ArrayList<Order_Line> order_lines, OnItemClickListener listener){
        this.order_lines = order_lines;
        this.listener = listener;
    }

    @Override
    public OrderLineProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewCartOrdersBinding binding = ViewCartOrdersBinding.inflate(inflater, parent, false);
        return new OrderLineProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(OrderLineProductViewHolder holder, int position) {
        Order_Line order_line = order_lines.get(position);
        holder.binding.setOrderLine(order_line);
        if((position % 2) == 0) {
            //even number (recyclerview start from zero) [array]
            holder.binding.itemCard.setCardBackgroundColor(holder.binding.getRoot().getContext().getResources().getColor(R.color.lightGrey));
            holder.binding.productOrderCancelProduct.setBackgroundColor(holder.binding.getRoot().getContext().getResources().getColor(R.color.lightGrey));
        }

        if(order_line.getDiscount() == 0){
            holder.binding.productOrderProductTotalPrice.setVisibility(View.INVISIBLE);
        }else{
            holder.binding.productOrderProductTotalPrice.setVisibility(View.VISIBLE);
        }

        holder.binding.productOrderQuantityEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus){
                    int discount = order_line.getDiscount();
                    int qty = 1;
                    double product_price = order_line.getProduct().getProduct_price();
                    double price_total = product_price * qty;
                    double subtotal = price_total + ((price_total * discount) / 100);

                    if ((!holder.binding.productOrderQuantityEt.getText().toString().equalsIgnoreCase(""))
                            && (!holder.binding.productOrderQuantityEt.getText().toString().equalsIgnoreCase("0"))) {
                        qty = Integer.parseInt(holder.binding.productOrderQuantityEt.getText().toString());
                        price_total = product_price * qty;
                        subtotal = price_total - ((price_total * discount) / 100);
                        subtotal = Double.valueOf(String.format("%.2f", subtotal));
                        listener.quantityUpdateOrderLine(position, subtotal, price_total, qty);
                    }else{
                        listener.quantityUpdateOrderLine(position, subtotal, price_total, qty);
                    }
                }
            }
        });

        holder.binding.productOrderDiscountEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    double subtotal = order_line.getPrice_subtotal();
                    double price_total = order_line.getPrice_total();
                    int discount = 0;
                    if ((!holder.binding.productOrderDiscountEt.getText().toString().equalsIgnoreCase(""))
                            && (!holder.binding.productOrderDiscountEt.getText().toString().equalsIgnoreCase("0"))) {
                        discount = Integer.parseInt(holder.binding.productOrderDiscountEt.getText().toString());
                        holder.binding.productOrderProductTotalPrice.setVisibility(View.VISIBLE);
                        subtotal = price_total - ((price_total * discount) / 100);
                        subtotal = Double.valueOf(String.format("%.2f", subtotal));
                        listener.discountUpdateOrderLine(position, subtotal, discount);
                    } else {
                        listener.discountUpdateOrderLine(position, subtotal, discount);
                        holder.binding.productOrderProductTotalPrice.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });


        holder.binding.productOrderSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.binding.productOrderSettingBtn.isSelected()){
                    closeSetting(holder);
                }else{
                    openSetting(holder);
                }
            }
        });
        holder.binding.productOrderCancelProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onOrderLineCancelClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return order_lines.size();
    }

    public interface OnItemClickListener{
        void onOrderLineClick(int position);
        void onOrderLineCancelClick(int position);
        void discountUpdateOrderLine(int position, double subtotal, int discount);
        void quantityUpdateOrderLine(int position, double subtotal, double price_total, int quantity);
    }

    private void closeSetting(OrderLineProductViewHolder holder){
        //case: close the setting
        holder.binding.productOrderGreenLine.setVisibility(View.INVISIBLE);
        holder.binding.productOrderSettingBtn.setImageResource(R.drawable.ic_right);
        holder.binding.productOrderQuantityDiscountRl.setVisibility(View.GONE);
        holder.binding.productOrderSettingBtn.setSelected(false);
    }
    private void openSetting(OrderLineProductViewHolder holder){
        //case: open the setting
        holder.binding.productOrderGreenLine.setVisibility(View.VISIBLE);
        holder.binding.productOrderSettingBtn.setImageResource(R.drawable.ic_down);
        holder.binding.productOrderQuantityDiscountRl.setVisibility(View.VISIBLE);
        holder.binding.productOrderSettingBtn.setSelected(true);
    }
}
