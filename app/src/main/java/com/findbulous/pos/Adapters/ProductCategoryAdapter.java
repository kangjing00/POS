package com.findbulous.pos.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.findbulous.pos.Floor;
import com.findbulous.pos.POS_Category;
import com.findbulous.pos.databinding.ViewFloorListBinding;
import com.findbulous.pos.databinding.ViewProductCategoryListBinding;

import java.util.ArrayList;

public class ProductCategoryAdapter extends RecyclerView.Adapter<ProductCategoryAdapter.ProductCategoryViewHolder>{

    private ArrayList<POS_Category> categories;
    private ProductCategoryClickInterface listener;
    private RadioButton lastClickedRB;
    private int firstCategoryCheck = 0;

    public class ProductCategoryViewHolder extends RecyclerView.ViewHolder{
        private final ViewProductCategoryListBinding binding;

        public ProductCategoryViewHolder(ViewProductCategoryListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public ProductCategoryAdapter(ArrayList<POS_Category> categories, ProductCategoryClickInterface listener){
        this.categories = categories;
        this.listener = listener;
        lastClickedRB = null;
    }


    @Override
    public ProductCategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewProductCategoryListBinding binding = ViewProductCategoryListBinding.inflate(inflater, parent, false);
        return new ProductCategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ProductCategoryViewHolder holder, int position) {
        POS_Category category = categories.get(position);
        holder.binding.setCategory(category);


        if(position == firstCategoryCheck){
            holder.binding.categoryBtn.setChecked(true);
            lastClickedRB = holder.binding.categoryBtn;
        }

        holder.binding.categoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(lastClickedRB != null)
                    lastClickedRB.setChecked(false);
                if(holder.binding.categoryBtn == lastClickedRB)
                    holder.binding.categoryBtn.setChecked(true);
                lastClickedRB = holder.binding.categoryBtn;
                listener.onProductCategoryClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public interface ProductCategoryClickInterface{
        void onProductCategoryClick(int position);
    }
}
