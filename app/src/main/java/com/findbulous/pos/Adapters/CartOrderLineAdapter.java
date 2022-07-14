package com.findbulous.pos.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import com.findbulous.pos.Order_Line;
import com.findbulous.pos.R;
import com.findbulous.pos.databinding.ViewCartOrdersBinding;
import java.util.ArrayList;

public class CartOrderLineAdapter extends RecyclerView.Adapter<CartOrderLineAdapter.OrderLineProductViewHolder> {

    private ArrayList<Order_Line> order_lines;
    private OnItemClickListener listener;
    private Context context;
    private int cancelledIndex;

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

    public CartOrderLineAdapter(ArrayList<Order_Line> order_lines, OnItemClickListener listener, Context c){
        this.order_lines = order_lines;
        this.listener = listener;
        this.context = c;
        this.cancelledIndex = -1;
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

        if((holder.getAdapterPosition() % 2) == 0) {
            //even number (recyclerview start from zero) [array]
            holder.binding.itemCard.setCardBackgroundColor(holder.binding.getRoot().getContext().getResources().getColor(R.color.lightGrey));
            holder.binding.productOrderCancelProduct.setBackgroundColor(holder.binding.getRoot().getContext().getResources().getColor(R.color.lightGrey));
        }else{
            holder.binding.itemCard.setCardBackgroundColor(holder.binding.getRoot().getContext().getResources().getColor(R.color.white));
            holder.binding.productOrderCancelProduct.setBackgroundColor(holder.binding.getRoot().getContext().getResources().getColor(R.color.white));
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
                    int qty = 1;

                    if ((!holder.binding.productOrderQuantityEt.getText().toString().equalsIgnoreCase(""))
                            && (Double.valueOf(holder.binding.productOrderQuantityEt.getText().toString()) != 0.0)) {
                        qty = Integer.parseInt(holder.binding.productOrderQuantityEt.getText().toString());
                    }

                    int p = holder.getLayoutPosition();
                    if(cancelledIndex != -1){
                        if(cancelledIndex == p){
                            p = -1;
                        }else if(cancelledIndex < p){
                            p = p - 1;
                        }
                    }
                    cancelledIndex = -1;

                    if(p > -1) {
                        double price_total = qty * order_lines.get(p).getProduct().getList_price();
                        int discount = order_lines.get(p).getDiscount();
                        double subtotal = price_total - ((price_total * discount) / 100);
                        price_total = Double.valueOf(String.format("%.2f", price_total));
                        subtotal = Double.valueOf(String.format("%.2f", subtotal));
                        order_lines.get(p).setPrice_subtotal(subtotal);
                        order_lines.get(p).setQty(qty);
                        order_lines.get(p).setPrice_total(price_total);
                        Order_Line updateOrderLine = order_lines.get(p);
                        holder.binding.setOrderLine(updateOrderLine);

                        listener.quantityUpdateOrderLine(p, updateOrderLine);
                    }
                }
            }
        });

//        holder.binding.productOrderDiscountEt.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//            @Override
//            public void afterTextChanged(Editable editable) {
//                int discount = 0;
//                if((!editable.toString().equalsIgnoreCase(""))
//                    && (!editable.toString().equalsIgnoreCase("0"))){
//                    discount = Integer.parseInt(editable.toString());
//                    holder.binding.productOrderProductTotalPrice.setVisibility(View.VISIBLE);
//                }else{
//                    holder.binding.productOrderProductTotalPrice.setVisibility(View.INVISIBLE);
//                }
//                listener.discountUpdateOrderLine(p, discount);
//            }
//        });
        holder.binding.productOrderDiscountEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    int discount = 0;
                    if ((!holder.binding.productOrderDiscountEt.getText().toString().equalsIgnoreCase(""))
                        && (Double.valueOf(holder.binding.productOrderDiscountEt.getText().toString()) != 0.0)
                        && (Double.valueOf(holder.binding.productOrderDiscountEt.getText().toString()) <= 100.0)) {
                        discount = Integer.parseInt(holder.binding.productOrderDiscountEt.getText().toString());
                        holder.binding.productOrderProductTotalPrice.setVisibility(View.VISIBLE);
                    } else {
                        if((Double.valueOf(holder.binding.productOrderDiscountEt.getText().toString()) > 100.0)) {
                            Toast.makeText(context, "Discount over 100% is impossible", Toast.LENGTH_SHORT).show();
                        }
                        holder.binding.productOrderProductTotalPrice.setVisibility(View.INVISIBLE);
                    }

                    int p = holder.getLayoutPosition();
                    if(cancelledIndex != -1){
                        if(cancelledIndex == p){
                            p = -1;
                        }else if(cancelledIndex < p){
                            p = p - 1;
                        }
                    }
                    cancelledIndex = -1;

                    if(p > -1) {
                        double subtotal;
                        double price_total = order_lines.get(p).getPrice_total();
                        subtotal = price_total - ((price_total * discount) / 100);
                        price_total = Double.valueOf(String.format("%.2f", price_total));
                        subtotal = Double.valueOf(String.format("%.2f", subtotal));
                        order_lines.get(p).setPrice_subtotal(subtotal);
                        order_lines.get(p).setDiscount(discount);
                        Order_Line updateOrderLine = order_lines.get(p);
                        holder.binding.setOrderLine(updateOrderLine);

                        listener.discountUpdateOrderLine(p, updateOrderLine);
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
                cancelledIndex = holder.getAdapterPosition();
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
        void discountUpdateOrderLine(int position, Order_Line updateOrderLine);
        void quantityUpdateOrderLine(int position, Order_Line updateOrderLine);
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
