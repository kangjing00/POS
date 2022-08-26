package com.findbulous.pos.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;
import com.findbulous.pos.Order;
import com.findbulous.pos.R;
import com.findbulous.pos.databinding.ViewOrderHistoryListBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder>{

    private ArrayList<Order> orders;
    private OnItemClickListener listener;
    private View lastOrderHistoryClicked;
    private int lastOrderHistoryClickedPosition;

    public class OrderHistoryViewHolder extends RecyclerView.ViewHolder{
        private ViewOrderHistoryListBinding binding;

        public OrderHistoryViewHolder(ViewOrderHistoryListBinding binding){
            super(binding.getRoot());
            this.binding = binding;
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
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date_format = null;
        try {
            date_format = df.parse(order.getDate_order());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(date_format != null){
            order.setDate_order(df.format(date_format));
        }

        holder.binding.setOrderHistory(order);
        if((position % 2) == 0){
            //even number (recyclerview start from zero) [array]
            holder.binding.viewOrderHistoryLl.setBackgroundResource(R.drawable.box_corner_nopadding_white);
        }else{
            holder.binding.viewOrderHistoryLl.setBackgroundResource(R.drawable.box_corner_nopadding);
        }

        holder.binding.viewOrderHistoryLl.setOnClickListener(new View.OnClickListener() {
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
                listener.onOrderHistoryOrderSelect(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }


    public interface OnItemClickListener{
        void onOrderHistoryOrderSelect(int position);
    }
}
