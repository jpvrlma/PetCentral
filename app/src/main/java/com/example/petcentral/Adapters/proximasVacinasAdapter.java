package com.example.petcentral.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcentral.Objetos.Vacinas;
import com.example.petcentral.databinding.RecyclerProximasBinding;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

public class proximasVacinasAdapter extends RecyclerView.Adapter<proximasVacinasAdapter.ViewHolder> {

    Context context;
    private ArrayList<Vacinas> vacinasArrayList;

    public proximasVacinasAdapter(Context context, ArrayList<Vacinas> vacinasArrayList) {
        this.context = context;
        this.vacinasArrayList = vacinasArrayList;
    }

    @NonNull
    @Override
    public proximasVacinasAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RecyclerProximasBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull proximasVacinasAdapter.ViewHolder holder, int position) {
        Vacinas vacinas = vacinasArrayList.get(position);
        holder.binding.tvVacina.setText(vacinas.getNome());
        holder.binding.tvnome.setText(vacinas.getNomePet());

        if (vacinas.getProximaDose() != null){
            holder.binding.tvdata.setText(formatarData(vacinas.getProximaDose()));
        }else{
            holder.binding.tvdata.setText("Data não disponível");
        }
    }

    @Override
    public int getItemCount() {
        return vacinasArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerProximasBinding binding;
        public ViewHolder(@NonNull RecyclerProximasBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static String formatarData(Timestamp data) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dataFormatada = sdf.format(data.toDate());
        return dataFormatada;
    }

}
