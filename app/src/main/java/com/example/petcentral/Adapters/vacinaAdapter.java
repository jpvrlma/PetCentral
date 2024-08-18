package com.example.petcentral.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcentral.Objetos.Vacinas;
import com.example.petcentral.databinding.RecyclerVacinaItemBinding;

import java.util.ArrayList;

public class vacinaAdapter extends RecyclerView.Adapter<vacinaAdapter.ViewHolder> {

    Context context;
    private ArrayList<Vacinas> vacinasArrayList;

    public vacinaAdapter(Context context, ArrayList<Vacinas> vacinasArrayList) {
        this.context = context;
        this.vacinasArrayList = vacinasArrayList;
    }

    @NonNull
    @Override
    public vacinaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RecyclerVacinaItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
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
        public ViewHolder(@NonNull RecyclerVacinaItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
