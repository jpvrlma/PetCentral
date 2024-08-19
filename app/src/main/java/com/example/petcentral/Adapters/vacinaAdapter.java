package com.example.petcentral.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcentral.Interfaces.selectVacinaInterface;
import com.example.petcentral.Objetos.Vacinas;
import com.example.petcentral.databinding.RecyclerVacinaItemBinding;

import java.util.ArrayList;

public class vacinaAdapter extends RecyclerView.Adapter<vacinaAdapter.ViewHolder> {

    Context context;
    private ArrayList<Vacinas> vacinasArrayList;
    private final selectVacinaInterface selectInterface;

    public vacinaAdapter(Context context, ArrayList<Vacinas> vacinasArrayList,selectVacinaInterface selectInterface) {
        this.context = context;
        this.vacinasArrayList = vacinasArrayList;
        this.selectInterface = selectInterface;
    }

    @NonNull
    @Override
    public vacinaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RecyclerVacinaItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false),selectInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull vacinaAdapter.ViewHolder holder, int position) {
            Vacinas vacinas = vacinasArrayList.get(position);
            holder.binding.tvNome.setText(vacinas.getId());
            holder.binding.tvDesc.setText(vacinas.getDescricao());
    }

    @Override
    public int getItemCount() {
        return vacinasArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final RecyclerVacinaItemBinding binding;
        public ViewHolder(@NonNull RecyclerVacinaItemBinding binding,selectVacinaInterface selectInterface) {
            super(binding.getRoot());
            this.binding = binding;

            binding.cardSelectVacina.setOnClickListener(v ->{
                if (selectInterface != null){
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION){
                        selectInterface.onSelectClick(position);
                    }
                }
            });
        }
    }

}
