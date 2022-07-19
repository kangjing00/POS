package com.findbulous.pos.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.findbulous.pos.Floor;
import com.findbulous.pos.Table;
import com.findbulous.pos.databinding.ViewFloorListBinding;
import com.findbulous.pos.databinding.ViewTableListBinding;

import java.util.ArrayList;

public class FloorAdapter extends RecyclerView.Adapter<FloorAdapter.FloorViewHolder>{

    private ArrayList<Floor> floors;
    private FloorClickInterface listener;
    private RadioButton lastClickedRB;
    private int firstFloorCheck = 0;

    public class FloorViewHolder extends RecyclerView.ViewHolder{
        private final ViewFloorListBinding binding;

        public FloorViewHolder(ViewFloorListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public FloorAdapter(ArrayList<Floor> floors, FloorClickInterface listener){
        this.floors = floors;
        this.listener = listener;
        lastClickedRB = null;
    }

    @Override
    public FloorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewFloorListBinding binding = ViewFloorListBinding.inflate(inflater, parent, false);
        return new FloorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(FloorViewHolder holder, int position) {
        Floor floor = floors.get(position);
        holder.binding.setFloor(floor);

        if(!floor.getActive().equalsIgnoreCase("true")){
            holder.binding.floorBtn.setVisibility(View.GONE);
        }
        if(position == firstFloorCheck){
            holder.binding.floorBtn.setChecked(true);
            lastClickedRB = holder.binding.floorBtn;
        }

        holder.binding.floorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(lastClickedRB != null)
                    lastClickedRB.setChecked(false);
                if(holder.binding.floorBtn == lastClickedRB)
                    holder.binding.floorBtn.setChecked(true);
                lastClickedRB = holder.binding.floorBtn;
                listener.onFloorClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return floors.size();
    }

    public interface FloorClickInterface{
        void onFloorClick(int position);
    }
}
