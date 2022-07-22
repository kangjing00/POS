package com.findbulous.pos.Adapters;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.findbulous.pos.Attribute;
import com.findbulous.pos.Attribute_Value;
import com.findbulous.pos.Order_Line;
import com.findbulous.pos.POS_Config;
import com.findbulous.pos.R;
import com.findbulous.pos.databinding.ViewCartOrdersBinding;
import java.util.ArrayList;

import io.realm.Realm;

public class CartOrderLineAdapter extends RecyclerView.Adapter<CartOrderLineAdapter.OrderLineProductViewHolder> {

    private ArrayList<Order_Line> order_lines;
    private OnItemClickListener listener;
    private Context context;
    private POS_Config pos_config;
//    private ArrayList<Attribute> attributes;
//    private ArrayList<Attribute_Value> attribute_values;
    private Realm realm;
    private int cancelledIndex;

    public class OrderLineProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final ViewCartOrdersBinding binding;

        public OrderLineProductViewHolder(ViewCartOrdersBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);

            if(pos_config.isManual_discount()){
                binding.cartOrderLineDiscountLl.setVisibility(View.VISIBLE);
            }else{
                binding.cartOrderLineDiscountLl.setVisibility(View.GONE);
            }
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
        this.pos_config = null;
//        this.attributes = new ArrayList<>();
//        this.attribute_values = new ArrayList<>();
        realm = Realm.getDefaultInstance();
        setPOSConfig();
    }

    private void setPOSConfig(){
        POS_Config temp = realm.where(POS_Config.class).findFirst();
        this.pos_config = realm.copyFromRealm(temp);
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

        if(order_line.isHas_discount()){
            holder.binding.productOrderProductTotalPrice.setVisibility(View.VISIBLE);
        }else{
            holder.binding.productOrderProductTotalPrice.setVisibility(View.INVISIBLE);
        }

        if(order_line.isIs_percentage()) {
            holder.binding.discountRadioBtnPercentage.setChecked(true);
            holder.binding.discountRadioBtnAmount.setChecked(false);
            holder.binding.productOrderDiscountEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        }else{
            holder.binding.discountRadioBtnPercentage.setChecked(false);
            holder.binding.discountRadioBtnAmount.setChecked(true);
            holder.binding.productOrderDiscountEt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }

        holder.binding.discountRadioBtnPercentage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.binding.productOrderDiscountEt.setInputType(InputType.TYPE_CLASS_NUMBER);
                holder.binding.productOrderProductTotalPrice.setVisibility(View.INVISIBLE);
                //Reset with no discount
                int p = holder.getAdapterPosition();
                double subtotal;
                double price_total = order_lines.get(p).getPrice_total();

                subtotal = price_total;
                subtotal = Double.valueOf(String.format("%.2f", subtotal));

                order_lines.get(p).setPrice_subtotal(subtotal);

                order_lines.get(p).setAmount_discount(0.0);
                order_lines.get(p).setDiscount_percent(0);
                order_lines.get(p).setIs_percentage(true);
                order_lines.get(p).setHas_discount(false);

                Order_Line updateOrderLine = order_lines.get(p);
                holder.binding.setOrderLine(updateOrderLine);

                listener.discountUpdateOrderLine(p, updateOrderLine);
            }
        });

        holder.binding.discountRadioBtnAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.binding.productOrderDiscountEt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                holder.binding.productOrderProductTotalPrice.setVisibility(View.INVISIBLE);
                //Reset with no discount
                int p = holder.getAdapterPosition();
                double subtotal;
                double price_total = order_lines.get(p).getPrice_total();

                subtotal = price_total;
                subtotal = Double.valueOf(String.format("%.2f", subtotal));

                order_lines.get(p).setPrice_subtotal(subtotal);

                order_lines.get(p).setAmount_discount(0.0);
                order_lines.get(p).setDiscount_percent(0);
                order_lines.get(p).setIs_percentage(true);
                order_lines.get(p).setHas_discount(false);

                Order_Line updateOrderLine = order_lines.get(p);
                holder.binding.setOrderLine(updateOrderLine);

                listener.discountUpdateOrderLine(p, updateOrderLine);
            }
        });

        holder.binding.productOrderQuantityEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus){
                    int qty = 1;

                    if ((!holder.binding.productOrderQuantityEt.getText().toString().equalsIgnoreCase(""))
                            && (Double.valueOf(holder.binding.productOrderQuantityEt.getText().toString()) > 0.0)) {
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
                        double price_total = qty * order_lines.get(p).getProduct_price();
                        double discount_amount = order_lines.get(p).getAmount_discount();
                        double subtotal = price_total - discount_amount;
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

        holder.binding.productOrderDiscountEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    int discount_percentage = 0;
                    double amount_discount = 0.0;
                    boolean has_discount = false, is_percentage = true;

                    int p = holder.getLayoutPosition();
                    if(cancelledIndex != -1){
                        if(cancelledIndex == p){
                            p = -1;
                        }else if(cancelledIndex < p){
                            p = p - 1;
                        }
                    }
                    cancelledIndex = -1;

                    if(holder.binding.discountRadioBtnPercentage.isChecked()){
                        if ((!holder.binding.productOrderDiscountEt.getText().toString().equalsIgnoreCase(""))
                                && (Double.valueOf(holder.binding.productOrderDiscountEt.getText().toString()) > 0.0)
                                && (Double.valueOf(holder.binding.productOrderDiscountEt.getText().toString()) <= 100.0)) {
                            discount_percentage = Integer.parseInt(holder.binding.productOrderDiscountEt.getText().toString());
                            amount_discount = 0.0;
                            is_percentage = true;
                            has_discount = true;
                            holder.binding.productOrderProductTotalPrice.setVisibility(View.VISIBLE);
                        } else {
                            if((Double.valueOf(holder.binding.productOrderDiscountEt.getText().toString()) > 100.0)) {
                                Toast.makeText(context, "Discount over 100% is impossible", Toast.LENGTH_SHORT).show();
                            }
                            has_discount = false;
                            holder.binding.productOrderProductTotalPrice.setVisibility(View.INVISIBLE);
                        }
                    }else{
                        if ((!holder.binding.productOrderDiscountEt.getText().toString().equalsIgnoreCase(""))
                                && (Double.valueOf(holder.binding.productOrderDiscountEt.getText().toString()) > 0.0)
                                && (Double.valueOf(holder.binding.productOrderDiscountEt.getText().toString()) <=
                                order_lines.get(p).getPrice_total())) {
                            discount_percentage = 0;
                            amount_discount = Double.valueOf(holder.binding.productOrderDiscountEt.getText().toString());
                            is_percentage = false;
                            has_discount = true;
                            holder.binding.productOrderProductTotalPrice.setVisibility(View.VISIBLE);
                        } else {
                            if((Double.valueOf(holder.binding.productOrderDiscountEt.getText().toString()) >
                                    order_lines.get(p).getPrice_total())) {
                                Toast.makeText(context, "Discount over the total product price is impossible", Toast.LENGTH_SHORT).show();
                            }
                            has_discount = false;
                            holder.binding.productOrderProductTotalPrice.setVisibility(View.INVISIBLE);
                        }
                    }



                    if(p > -1) {
                        double subtotal;
                        double price_total = order_lines.get(p).getPrice_total();

                        if(is_percentage){
                            amount_discount = (price_total * discount_percentage) / 100;
                        }
                        subtotal = price_total - amount_discount;
                        amount_discount = Double.valueOf(String.format("%.2f", amount_discount));
                        subtotal = Double.valueOf(String.format("%.2f", subtotal));

                        order_lines.get(p).setPrice_subtotal(subtotal);

                        order_lines.get(p).setAmount_discount(amount_discount);
                        order_lines.get(p).setDiscount_percent(discount_percentage);
                        order_lines.get(p).setIs_percentage(is_percentage);
                        order_lines.get(p).setHas_discount(has_discount);

                        Order_Line updateOrderLine = order_lines.get(p);
                        holder.binding.setOrderLine(updateOrderLine);

                        listener.discountUpdateOrderLine(p, updateOrderLine);
                    }
//                    if(p > -1) {
//                        double subtotal;
//                        double price_total = order_lines.get(p).getPrice_total();
//                        subtotal = price_total - ((price_total * discount) / 100);
//                        price_total = Double.valueOf(String.format("%.2f", price_total));
//                        subtotal = Double.valueOf(String.format("%.2f", subtotal));
//                        order_lines.get(p).setPrice_subtotal(subtotal);
//                        order_lines.get(p).setDiscount(discount);
//                        Order_Line updateOrderLine = order_lines.get(p);
//                        holder.binding.setOrderLine(updateOrderLine);
//
//                        listener.discountUpdateOrderLine(p, updateOrderLine);
//                    }
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
        holder.binding.productOrderQuantityDiscountCl.setVisibility(View.GONE);
        holder.binding.productOrderSettingBtn.setSelected(false);
    }
    private void openSetting(OrderLineProductViewHolder holder){
        //case: open the setting
        holder.binding.productOrderGreenLine.setVisibility(View.VISIBLE);
        holder.binding.productOrderSettingBtn.setImageResource(R.drawable.ic_down);
        holder.binding.productOrderQuantityDiscountCl.setVisibility(View.VISIBLE);
        holder.binding.productOrderSettingBtn.setSelected(true);
    }
}
