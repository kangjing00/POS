package com.findbulous.pos.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.findbulous.pos.Customer;
import com.findbulous.pos.R;
import com.findbulous.pos.databinding.ViewCustomerListBinding;
import java.util.ArrayList;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    private ArrayList<Customer> customers;
    private OnItemClickListener listener;

    public class CustomerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final ViewCustomerListBinding binding;

        public CustomerViewHolder(ViewCustomerListBinding binding){
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onCustomerClick(this.getAdapterPosition());
        }
    }

    public CustomerAdapter(ArrayList<Customer> customers, OnItemClickListener listener){
        this.customers = customers;
        this.listener = listener;
    }

    @Override
    public CustomerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewCustomerListBinding binding = ViewCustomerListBinding.inflate(inflater, parent, false);
        return new CustomerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(CustomerViewHolder holder, int position) {
        Customer customer = customers.get(position);
        holder.binding.setCustomer(customer);
        if((position % 2) == 0){
            //even number (recyclerview start from zero) [array]
            holder.binding.customerListCl.setBackgroundResource(R.drawable.box_corner_nopadding);
        }
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    public interface OnItemClickListener{
        void onCustomerClick(int position);
    }
}
