package com.findbulous.pos.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.findbulous.pos.Order;
import com.findbulous.pos.R;
import com.findbulous.pos.databinding.ViewTableOrderSelectionBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TableOrderAdapter extends RecyclerView.Adapter<TableOrderAdapter.TableOrderViewHolder> {

    private ArrayList<Order> orders;
    private TableOrderClickInterface listener;
    private View lastTableOrderClicked;
    private int lastTableOrderClickedPosition;

    public class TableOrderViewHolder extends RecyclerView.ViewHolder{
        private final ViewTableOrderSelectionBinding binding;

        public TableOrderViewHolder(ViewTableOrderSelectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public TableOrderAdapter(ArrayList<Order> orders, TableOrderClickInterface listener){
        this.orders = orders;
        this.listener = listener;
    }

    @Override
    public TableOrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewTableOrderSelectionBinding binding = ViewTableOrderSelectionBinding.inflate(inflater, parent, false);
        lastTableOrderClicked = null;
        lastTableOrderClickedPosition = -1;

        return new TableOrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(TableOrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.binding.setOrder(order);

        if((holder.getAdapterPosition() % 2) == 0){
            //even number (recyclerview start from zero) [array]
            holder.binding.viewTableOrderSelectionLl.setBackgroundResource(R.drawable.box_corner_nopadding_white);
        }else{
            holder.binding.viewTableOrderSelectionLl.setBackgroundResource(R.drawable.box_corner_nopadding);
        }

        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(lastTableOrderClicked != null && lastTableOrderClickedPosition > -1){
                    if((lastTableOrderClickedPosition % 2) == 0){
                        lastTableOrderClicked.setBackgroundResource(R.drawable.box_corner_nopadding_white);
                    }else{
                        lastTableOrderClicked.setBackgroundResource(R.drawable.box_corner_nopadding);
                    }
                }
                lastTableOrderClicked = holder.binding.getRoot();
                lastTableOrderClickedPosition = holder.getAdapterPosition();
                if((holder.getAdapterPosition() % 2) == 0){
                    holder.binding.getRoot().setBackgroundResource(R.drawable.box_corner_border_orange_white);
                }else{
                    holder.binding.getRoot().setBackgroundResource(R.drawable.box_corner_border_orange);
                }

                listener.onTableOrderSelect(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public interface TableOrderClickInterface{
        void onTableOrderSelect(int position);
    }
}
