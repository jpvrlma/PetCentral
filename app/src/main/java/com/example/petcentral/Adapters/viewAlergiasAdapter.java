package com.example.petcentral.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcentral.Interfaces.DeleteInterface;
import com.example.petcentral.Objetos.Alergias;
import com.example.petcentral.databinding.RecyclerAlergiasBinding;

import java.util.ArrayList;

public class viewAlergiasAdapter extends RecyclerView.Adapter<viewAlergiasAdapter.ViewHolder> {

    Context context;
    private final ArrayList<Alergias> alergiasArrayList;
    private final DeleteInterface deleteInterface;

    public viewAlergiasAdapter(ArrayList<Alergias> alergiasArrayList, DeleteInterface deleteInterface, Context context) {
        this.alergiasArrayList = alergiasArrayList;
        this.deleteInterface = deleteInterface;
        this.context = context;
    }

    @NonNull
    @Override
    public viewAlergiasAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RecyclerAlergiasBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false),deleteInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull viewAlergiasAdapter.ViewHolder holder, int position) {
            Alergias alergias = alergiasArrayList.get(position);

            holder.binding.textnome.setText(alergias.getAlergia());

    }

    @Override
    public int getItemCount() {
        return alergiasArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerAlergiasBinding binding;
        public ViewHolder(@NonNull RecyclerAlergiasBinding binding, DeleteInterface deleteInterface) {
            super(binding.getRoot());
            this.binding = binding;

            binding.deleteButton.setOnClickListener(v -> {
                if (deleteInterface != null){
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION){
                        deleteInterface.onDeleteClick(position);
                    }
                }
            });

            binding.cardView.setOnClickListener(v -> {
                if (deleteInterface != null){
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION){
                        deleteInterface.onSelectClick(position);
                    }
                }
            });
        }
    }
}
