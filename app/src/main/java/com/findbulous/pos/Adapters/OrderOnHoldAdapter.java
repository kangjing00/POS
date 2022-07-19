package com.findbulous.pos.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.findbulous.pos.Order;
import com.findbulous.pos.Order_Line;
import com.findbulous.pos.databinding.ViewOrderOnHoldListBinding;
import java.util.ArrayList;
import io.realm.Realm;
import io.realm.RealmResults;

public class OrderOnHoldAdapter extends RecyclerView.Adapter<OrderOnHoldAdapter.OrderOnHoldViewHolder> {

    private ArrayList<Order> orders;
    private Context context;
    private OnHoldRemoveResumeInterface listener;

    public class OrderOnHoldViewHolder extends RecyclerView.ViewHolder{
        private ViewOrderOnHoldListBinding binding;

        public OrderOnHoldViewHolder(ViewOrderOnHoldListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public OrderOnHoldAdapter(ArrayList<Order> orders, Context context, OnHoldRemoveResumeInterface listener){
        this.orders = orders;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public OrderOnHoldViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewOrderOnHoldListBinding binding = ViewOrderOnHoldListBinding.inflate(inflater, parent, false);

        return new OrderOnHoldViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(OrderOnHoldViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.binding.setOrder(order);

        if(order.getTable() != null){
            holder.binding.tableName.setText(order.getTable().getFloor().getName() + " / " + order.getTable().getName());
        }else{
            holder.binding.tableName.setText("Takeaway");
        }

        if((order.getNote() == null) || (order.getNote().isEmpty())){
            holder.binding.noteRl.setVisibility(View.GONE);
        }else{
            holder.binding.noteRl.setVisibility(View.VISIBLE);
        }

        holder.binding.removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onOrderOnHoldRemove(holder.getAdapterPosition());
            }
        });
        holder.binding.resumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onOrderOnHoldResume(holder.getAdapterPosition());
            }
        });


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        holder.binding.orderOnHoldProductsRv.setLayoutManager(layoutManager);
        holder.binding.orderOnHoldProductsRv.setHasFixedSize(true);
        Realm realm = Realm.getDefaultInstance();
        RealmResults results = realm.where(Order_Line.class).equalTo("order.order_id", order.getOrder_id()).findAll();
        ArrayList<Order_Line> order_lines = new ArrayList<>();
        order_lines.addAll(realm.copyFromRealm(results));

        OrderOnHoldProductAdapter productAdapter = new OrderOnHoldProductAdapter(order_lines, holder.binding.orderOnHoldProductsRv.getContext());
        holder.binding.orderOnHoldProductsRv.setAdapter(productAdapter);
        productAdapter.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public interface OnHoldRemoveResumeInterface{
        void onOrderOnHoldRemove(int position);
        void onOrderOnHoldResume(int position);
    }
}
