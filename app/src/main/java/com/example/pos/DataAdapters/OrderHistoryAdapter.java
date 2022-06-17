package com.example.pos.DataAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pos.Order;
import com.example.pos.R;
import com.example.pos.databinding.ViewOrderHistoryListBinding;
import java.util.ArrayList;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder>{

    private ArrayList<Order> orders;
    private OnItemClickListener listener;
    private View lastOrderHistoryClicked;
    private int lastOrderHistoryClickedPosition;

    public class OrderHistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ViewOrderHistoryListBinding binding;

        public OrderHistoryViewHolder(ViewOrderHistoryListBinding binding){
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onOrderHistoryOrderClick(this.getAdapterPosition());
        }
    }

    public OrderHistoryAdapter(ArrayList<Order> orders, OnItemClickListener listener){
        this.orders = orders;
        this.listener = listener;
    }

    @Override
    public OrderHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewOrderHistoryListBinding binding = ViewOrderHistoryListBinding.inflate(inflater, parent, false);
        lastOrderHistoryClicked = null;
        lastOrderHistoryClickedPosition = -1;

        return new OrderHistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(OrderHistoryViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.binding.setOrderHistory(order);
        if((position % 2) == 0){
            //even number (recyclerview start from zero) [array]
            holder.binding.viewOrderHistoryLl.setBackgroundResource(R.drawable.box_corner_nopadding_white);
        }

        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(lastOrderHistoryClicked != null && lastOrderHistoryClickedPosition > -1){
                    if((lastOrderHistoryClickedPosition % 2) == 0){
                        lastOrderHistoryClicked.setBackgroundResource(R.drawable.box_corner_nopadding_white);
                    }else {
                        lastOrderHistoryClicked.setBackgroundResource(R.drawable.box_corner_nopadding);
                    }
                }
                lastOrderHistoryClicked = holder.binding.getRoot();
                lastOrderHistoryClickedPosition = holder.getAdapterPosition();
                if((holder.getAdapterPosition()) % 2 == 0){
                    holder.binding.viewOrderHistoryLl.setBackgroundResource(R.drawable.box_corner_border_orange_white);
                }else{
                    holder.binding.viewOrderHistoryLl.setBackgroundResource(R.drawable.box_corner_border_orange);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }


    public interface OnItemClickListener{
        void onOrderHistoryOrderClick(int position);
    }
}
