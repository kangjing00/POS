package com.findbulous.pos.Adapters;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

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

        if(order_line.getDiscount_type() != null) {
            if ((order_line.getDiscount_type().equalsIgnoreCase("percentage")) ||
                    (order_line.getDiscount_type().equalsIgnoreCase("fixed_amount"))) {
                holder.binding.productOrderProductPriceBeforeDiscount.setVisibility(View.VISIBLE);
            } else {
                holder.binding.productOrderProductPriceBeforeDiscount.setVisibility(View.INVISIBLE);
            }
        }else{
            holder.binding.productOrderProductPriceBeforeDiscount.setVisibility(View.INVISIBLE);
        }

        if(order_line.getDiscount_type() != null) {
            if (order_line.getDiscount_type().equalsIgnoreCase("percentage")) {
                holder.binding.discountRadioBtnPercentage.setChecked(true);
                holder.binding.discountRadioBtnAmount.setChecked(false);
                holder.binding.productOrderDiscountEt.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else if (order_line.getDiscount_type().equalsIgnoreCase("fixed_amount")) {
                holder.binding.discountRadioBtnPercentage.setChecked(false);
                holder.binding.discountRadioBtnAmount.setChecked(true);
                holder.binding.productOrderDiscountEt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }
        }else{
            holder.binding.discountRadioBtnPercentage.setChecked(true);
            holder.binding.discountRadioBtnAmount.setChecked(false);
            holder.binding.productOrderDiscountEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        holder.binding.discountRadioBtnPercentage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.binding.productOrderDiscountEt.setInputType(InputType.TYPE_CLASS_NUMBER);
                holder.binding.productOrderProductPriceBeforeDiscount.setVisibility(View.INVISIBLE);
                //Reset with no discount
                int p = holder.getAdapterPosition();
                double subtotal;
                double price_before_discount = order_lines.get(p).getPrice_before_discount();

                subtotal = price_before_discount;
                subtotal = Double.valueOf(String.format("%.2f", subtotal));

                order_lines.get(p).setPrice_subtotal(subtotal);

                order_lines.get(p).setDiscount(0.0);
                order_lines.get(p).setDiscount_type(null);

                Order_Line updateOrderLine = order_lines.get(p);
                holder.binding.setOrderLine(updateOrderLine);

                listener.discountUpdateOrderLine(p, updateOrderLine);
            }
        });

        holder.binding.discountRadioBtnAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.binding.productOrderDiscountEt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                holder.binding.productOrderProductPriceBeforeDiscount.setVisibility(View.INVISIBLE);
                //Reset with no discount
                int p = holder.getAdapterPosition();
                double subtotal;
                double price_before_discount = order_lines.get(p).getPrice_before_discount();

                subtotal = price_before_discount;
                subtotal = Double.valueOf(String.format("%.2f", subtotal));

                order_lines.get(p).setPrice_subtotal(subtotal);

                order_lines.get(p).setDiscount(0.0);
                order_lines.get(p).setDiscount_type(null);

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
                        double price_before_discount = qty * order_lines.get(p).getPrice_unit();
                        double amount_discount = 0;
                        if(order_lines.get(p).getDiscount_type() != null) {
                            if (order_lines.get(p).getDiscount_type().equalsIgnoreCase("percentage")) {
                                amount_discount = (price_before_discount * order_lines.get(p).getDiscount()) / 100;
                            } else if (order_lines.get(p).getDiscount_type().equalsIgnoreCase("fixed_amount")) {
                                amount_discount = order_lines.get(p).getDiscount();
                            }
                        }
                        double subtotal = price_before_discount - amount_discount;

                        order_lines.get(p).setPrice_subtotal(subtotal);
                        order_lines.get(p).setQty(qty);
                        order_lines.get(p).setPrice_before_discount(price_before_discount);
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
                    double discount = 0.0;
                    String discount_type = null;

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
                            discount = Integer.parseInt(holder.binding.productOrderDiscountEt.getText().toString());
                            discount_type = "percentage";
                            holder.binding.productOrderProductPriceBeforeDiscount.setVisibility(View.VISIBLE);
                        } else {
                            if(holder.binding.productOrderDiscountEt.getText().toString().equalsIgnoreCase("")){
                                discount = 0;
                            }else if((Double.valueOf(holder.binding.productOrderDiscountEt.getText().toString()) > 100.0)) {
                                Toast.makeText(context, "Discount over 100% is impossible", Toast.LENGTH_SHORT).show();
                            }
                            discount_type = null;
                            discount = 0.0;
                            holder.binding.productOrderProductPriceBeforeDiscount.setVisibility(View.INVISIBLE);
                        }
                    }else{
                        if ((!holder.binding.productOrderDiscountEt.getText().toString().equalsIgnoreCase(""))
                                && (Double.valueOf(holder.binding.productOrderDiscountEt.getText().toString()) > 0.0)
                                && (Double.valueOf(holder.binding.productOrderDiscountEt.getText().toString()) <=
                                order_lines.get(p).getPrice_before_discount())) {
                            discount = Double.valueOf(holder.binding.productOrderDiscountEt.getText().toString());
                            discount_type = "fixed_amount";
                            holder.binding.productOrderProductPriceBeforeDiscount.setVisibility(View.VISIBLE);
                        } else {
                            if((Double.valueOf(holder.binding.productOrderDiscountEt.getText().toString()) >
                                    order_lines.get(p).getPrice_before_discount())) {
                                Toast.makeText(context, "Discount over the total product price is impossible", Toast.LENGTH_SHORT).show();
                            }
                            discount_type = null;
                            discount = 0.0;
                            holder.binding.productOrderProductPriceBeforeDiscount.setVisibility(View.INVISIBLE);
                        }
                    }


                    if(p > -1) {
                        double subtotal;
                        double amount_discount;
                        double price_before_discount = order_lines.get(p).getPrice_before_discount();

                        amount_discount = discount;//fixed_amount
                        if(discount_type != null) {
                            if (discount_type.equalsIgnoreCase("percentage")) { //percentage
                                amount_discount = (price_before_discount * discount) / 100;
                            }
                        }
                        subtotal = price_before_discount - amount_discount;

                        order_lines.get(p).setPrice_subtotal(subtotal);
                        order_lines.get(p).setDiscount(discount);
                        order_lines.get(p).setDiscount_type(discount_type);

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
