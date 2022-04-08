package com.example.pos.DataAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pos.ItemClickListener;
import com.example.pos.Product;
import com.example.pos.databinding.ViewProductMenuBinding;
import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private ArrayList<Product> products;

    public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final ViewProductMenuBinding binding;
        ItemClickListener itemClickListener;

        public ProductViewHolder(ViewProductMenuBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
        }

        public void setItemClickListener(ItemClickListener itemClickListener){
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            this.itemClickListener.onItemClick(getLayoutPosition());
        }
    }

    public ProductAdapter(ArrayList<Product> products){
        this.products = products;
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
}
