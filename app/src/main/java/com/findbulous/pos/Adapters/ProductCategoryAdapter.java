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
import java.util.Stack;

public class ProductCategoryAdapter extends RecyclerView.Adapter<ProductCategoryAdapter.ProductCategoryViewHolder>{

    private ArrayList<POS_Category> categories;
    private ProductCategoryClickInterface listener;
//    private ArrayList<RadioButton> allButtons;
//    private POS_Category clickedCategory;

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
//        this.allButtons = new ArrayList<>();
//        this.clickedCategory = null;
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
//        allButtons.add(holder.binding.categoryBtn);

        holder.binding.categoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                for(int i = 0; i < categories.size(); i++){
//                    if(categories.get(i).getPos_categ_id() == holder.binding.getCategory().getPos_categ_id()){
//                        clickedCategory = categories.get(i);
//                    }
//                    allButtons.get(i).setChecked(false);
//                }
//
//                for(int i = 0; i < allButtons.size(); i++){
//                    allButtons.get(i).setChecked(true);
//                    if(categories.get(i).getPos_categ_id() == clickedCategory.getPos_categ_id()){
//                        break;
//                    }
//                }
//                allButtons.clear();
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
