package com.example.pos.DataAdapters;

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
                listener.onOrderLineCancelClick(position);
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
    }

    private void closeSetting(OrderLineProductViewHolder holder){
        //case: close the setting
        holder.binding.productOrderGreenLine.setVisibility(View.INVISIBLE);
        holder.binding.productOrderSettingBtn.setImageResource(R.drawable.ic_right);
        holder.binding.textView.setVisibility(View.GONE);
        holder.binding.textView1.setVisibility(View.GONE);
        holder.binding.productOrderQuantityEt.setVisibility(View.GONE);
        holder.binding.productOrderDiscountEt.setVisibility(View.GONE);
        holder.binding.productOrderSettingBtn.setSelected(false);
    }
    private void openSetting(OrderLineProductViewHolder holder){
        //case: open the setting
        holder.binding.productOrderGreenLine.setVisibility(View.VISIBLE);
        holder.binding.productOrderSettingBtn.setImageResource(R.drawable.ic_down);
        holder.binding.textView.setVisibility(View.VISIBLE);
        holder.binding.textView1.setVisibility(View.VISIBLE);
        holder.binding.productOrderQuantityEt.setVisibility(View.VISIBLE);
        holder.binding.productOrderDiscountEt.setVisibility(View.VISIBLE);
        holder.binding.productOrderSettingBtn.setSelected(true);
    }
}
