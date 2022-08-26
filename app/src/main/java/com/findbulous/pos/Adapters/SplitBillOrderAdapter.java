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

    public SplitBillOrderAdapter(ArrayList<Order_Line> order_lines, SplitOrderLineInterface listener){
        this.order_lines = order_lines;
        this.listener = listener;
        splitsQty = new ArrayList<>();
        System.out.println("Order Line Sizeeeeeeeeee: " + order_lines.size());
        for (int i = 0; i < order_lines.size(); i++){
            splitsQty.add(0);
            System.out.println("Splits Qty : " + splitsQty.size());
        }

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


                    if (clicked_split_qty > 0) {
//                        if (clickedSplitOrderLine.getLocal_order_line_id() == clickedOrderLine.getLocal_order_line_id()) {
//                            Order_Line temp_clickedOrderLine = realm.copyFromRealm(clickedOrderLine);
//                            Order_Line split_order_line = updateQty(temp_clickedOrderLine, clickedSplitOrderLine.getQty() + 1);
//                            split_order_lines.add(split_order_line);
//                        } else {
//                            Order_Line temp_clickedOrderLine = realm.copyFromRealm(clickedOrderLine);
//
////                        int orderLineQtyMinusOne = clickedOrderLine.getQty() - 1;
////                        order_lines.set(holder.getAdapterPosition(),
////                                updateQty(temp_clickedOrderLine, orderLineQty));
//
//                            Order_Line split_order_line = updateQty(temp_clickedOrderLine, 1);
//                            split_order_lines.add(split_order_line);
//                        }
                    } else {
//                        Order_Line temp_clickedOrderLine = realm.copyFromRealm(clickedOrderLine);
//
////                        int orderLineQtyMinusOne = clickedOrderLine.getQty() - 1;
////                        order_lines.set(holder.getAdapterPosition(),
////                                updateQty(temp_clickedOrderLine, orderLineQty));
//
//                        Order_Line split_order_line = updateQty(temp_clickedOrderLine, 1);
//                        split_order_lines.add(split_order_line);
                    }

                }
                else{ // over qty

                    Toast.makeText(view.getContext(), "Over qty loh", Toast.LENGTH_SHORT).show();
                }

                listener.onSplitOrderLineClick(holder.getAdapterPosition());
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return order_lines.size();
    }

    private Order_Line updateQty(Order_Line order_lineToUpdate, int qty){
        RealmResults<Product_Tax> product_tax_results = realm.where(Product_Tax.class)
                .equalTo("product_tmpl_id", order_lineToUpdate.getProduct().getProduct_tmpl_id()).findAll();
        ArrayList<Product_Tax> product_taxes = new ArrayList<>();
        product_taxes.addAll(realm.copyFromRealm(product_tax_results));

        double price_unit = order_lineToUpdate.getPrice_unit();
        double amount_discount = 0;
        if(order_lineToUpdate.getDiscount_type() != null) {
            if (order_lineToUpdate.getDiscount_type().equalsIgnoreCase("percentage")) {
                amount_discount = (price_unit * order_lineToUpdate.getDiscount()) / 100;
            } else if (order_lineToUpdate.getDiscount_type().equalsIgnoreCase("fixed_amount")) {
                amount_discount = order_lineToUpdate.getDiscount();
            }
        }
        double price_before_discount =
                calculate_price_unit_excl_tax(order_lineToUpdate.getProduct(), price_unit) * qty;
        double price_unit_excl_tax =
                calculate_price_unit_excl_tax(order_lineToUpdate.getProduct(), (price_unit - amount_discount));
        double price_subtotal = price_unit_excl_tax * qty;
        double price_subtotal_incl = calculate_price_subtotal_incl(product_taxes, price_subtotal);
        double total_cost = order_lineToUpdate.getProduct().getStandard_price() * qty;

        order_lineToUpdate.setPrice_subtotal(price_subtotal);
        order_lineToUpdate.setDisplay_price_subtotal(currencyDisplayFormat(price_subtotal));
        order_lineToUpdate.setPrice_subtotal_incl(price_subtotal_incl);
        order_lineToUpdate.setDisplay_price_subtotal_incl(currencyDisplayFormat(price_subtotal_incl));
        order_lineToUpdate.setQty(qty);
        order_lineToUpdate.setPrice_before_discount(price_before_discount);
        order_lineToUpdate.setDisplay_price_before_discount(currencyDisplayFormat(price_before_discount));
        order_lineToUpdate.setTotal_cost(total_cost);
        order_lineToUpdate.setDisplay_total_cost(currencyDisplayFormat(total_cost));

        return order_lineToUpdate;
    }
    private double calculate_price_unit_excl_tax(Product product, double price_unit){
        double fixed = product.getAmount_tax_incl_fixed(),
                percent = product.getAmount_tax_incl_percent(),
                division = product.getAmount_tax_incl_division();

        double price_unit_excl_tax = ((price_unit  - fixed) / (1 + (percent / 100))) * (1 - (division / 100));

        return price_unit_excl_tax;
    }
    private double calculate_price_subtotal_incl(ArrayList<Product_Tax> product_taxes, double price_subtotal){
        double price_subtotal_incl;
        double total_taxes = 0.0, price = price_subtotal;
        double tax = 0.0;

        for(int i = 0; i < product_taxes.size(); i++){
            Product_Tax product_tax = product_taxes.get(i);

            if (product_tax.getAmount_type().equalsIgnoreCase("fixed")) {
                tax = product_tax.getAmount();
            } else if (product_tax.getAmount_type().equalsIgnoreCase("percent")) {
                tax = (price * (product_tax.getAmount() / 100));
            } else if (product_tax.getAmount_type().equalsIgnoreCase("division")) {
                tax = ((price / (1 - (product_tax.getAmount() / 100))) - price);
            }

            if (product_tax.isInclude_base_amount()) {    //TRUE
                price += tax;
            }

            total_taxes += tax;
        }
        price_subtotal_incl = price_subtotal + total_taxes;

        return price_subtotal_incl;
    }
    private String currencyDisplayFormat(double value){
        String valueFormatted = null;
        int decimal_place = currency.getDecimal_places();
        String currencyPosition = currency.getPosition();
        String symbol = currency.getSymbol();

        if(currencyPosition.equalsIgnoreCase("after")){
            valueFormatted = String.format("%." + decimal_place + "f", value) + symbol;
        }else if(currencyPosition.equalsIgnoreCase("before")){
            valueFormatted = symbol + String.format("%." + decimal_place + "f", value);
        }

        return valueFormatted;
    }

    public interface SplitOrderLineInterface{
        void onSplitOrderLineClick(int position);
    }
}
