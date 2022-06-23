package com.findbulous.pos.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

import com.findbulous.pos.Product;
import com.findbulous.pos.databinding.ViewProductMenuBinding;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private ArrayList<Product> products;
    private OnItemClickListener listener;

    public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final ViewProductMenuBinding binding;

        public ProductViewHolder(ViewProductMenuBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onMenuProductClick(this.getAdapterPosition());
        }
    }

    public ProductAdapter(ArrayList<Product> products, OnItemClickListener listener){
        this.products = products;
        this.listener = listener;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewProductMenuBinding binding = ViewProductMenuBinding.inflate(inflater, parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.binding.setProduct(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public interface OnItemClickListener{
        void onMenuProductClick(int position);
    }
}
