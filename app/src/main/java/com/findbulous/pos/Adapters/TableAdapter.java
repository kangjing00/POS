package com.findbulous.pos.Adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.findbulous.pos.R;
import com.findbulous.pos.Table;
import com.findbulous.pos.databinding.ViewTableListBinding;

import java.util.ArrayList;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.TableViewHolder>{

    private ArrayList<Table> tables;
    private TableClickInterface listener;

    public class TableViewHolder extends RecyclerView.ViewHolder{
        private final ViewTableListBinding binding;

        public TableViewHolder(ViewTableListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public TableAdapter(ArrayList<Table> tables, TableClickInterface listener){
        this.tables = tables;
        this.listener = listener;
    }

    @Override
    public TableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewTableListBinding binding = ViewTableListBinding.inflate(inflater, parent, false);
        return new TableViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(TableViewHolder holder, int position) {
        Drawable tvDrawable;
        tvDrawable = DrawableCompat.wrap(holder.itemView.getResources().getDrawable(R.drawable.ic_square_table_4_modified));
        Table table = tables.get(position);
        holder.binding.setTable(table);

        if(!table.getActive().equalsIgnoreCase("t")){
            holder.binding.tableTv.setVisibility(View.INVISIBLE);
        }else {
            if (table.getState().equalsIgnoreCase("V")) {
                DrawableCompat.setTint(tvDrawable, holder.itemView.getResources().getColor(R.color.green));
            } else if (table.getState().equalsIgnoreCase("H")) {
                DrawableCompat.setTint(tvDrawable, holder.itemView.getResources().getColor(R.color.blue));
            } else {
                DrawableCompat.setTint(tvDrawable, holder.itemView.getResources().getColor(R.color.red));
            }
            holder.binding.tableTv.setBackground(tvDrawable);
        }

        holder.binding.tableTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onTableClick(holder.getAdapterPosition(), view);
            }
        });
        holder.binding.tableTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.onTableLongClick(holder.getAdapterPosition());
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return tables.size();
    }

    public interface TableClickInterface{
        void onTableClick(int position, View view);
        void onTableLongClick(int position);
    }
}
