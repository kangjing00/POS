package com.findbulous.pos.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.findbulous.pos.POS_Category;
import com.findbulous.pos.databinding.ViewProductCategoryListBinding;
import java.util.ArrayList;

public class ProductCategoryAdapter extends RecyclerView.Adapter<ProductCategoryAdapter.ProductCategoryViewHolder>{

    private ArrayList<POS_Category> categories;
    private ProductCategoryClickInterface listener;
    private ArrayList<POS_Category> categories_clicked_wo_child;

    public class ProductCategoryViewHolder extends RecyclerView.ViewHolder{
        private final ViewProductCategoryListBinding binding;

        public ProductCategoryViewHolder(ViewProductCategoryListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public ProductCategoryAdapter(ArrayList<POS_Category> categories, ProductCategoryClickInterface listener, ArrayList<POS_Category> categories_clicked_wo_child){
        this.categories = categories;
        this.listener = listener;
        this.categories_clicked_wo_child = categories_clicked_wo_child;
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
        holder.binding.categoryBtn.setChecked(false);

        for(int i = 0; i < categories_clicked_wo_child.size(); i++){
            if(holder.binding.getCategory().getId() == categories_clicked_wo_child.get(i).getId()){
                holder.binding.categoryBtn.setChecked(true);
            }
        }

        holder.binding.categoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
