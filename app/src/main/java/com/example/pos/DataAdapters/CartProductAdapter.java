package com.example.pos.DataAdapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pos.Product;
import com.example.pos.databinding.ViewCartOrdersBinding;

import java.util.ArrayList;

public class CartProductAdapter extends RecyclerView.Adapter<CartProductAdapter.CartProductViewHolder> {

    private ArrayList<Product> products;

    public class CartProductViewHolder extends RecyclerView.ViewHolder{
        private final ViewCartOrdersBinding binding;

        public CartProductViewHolder(ViewCartOrdersBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @Override
    public CartProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewCartOrdersBinding binding = ViewCartOrdersBinding.inflate(inflater, parent, false);
        return new CartProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(CartProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.binding.setProduct(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }
}
