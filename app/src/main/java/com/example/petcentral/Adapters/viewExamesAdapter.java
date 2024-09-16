package com.example.petcentral.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcentral.Interfaces.OnSelectInterface;
import com.example.petcentral.Objetos.Exames;
import com.example.petcentral.databinding.RecyclerViewexamesBinding;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

public class viewExamesAdapter extends RecyclerView.Adapter<viewExamesAdapter.ViewHolder> {

    Context context;
    private final ArrayList<Exames> examesArrayList;
    private final OnSelectInterface selectInterface;

    public viewExamesAdapter(OnSelectInterface selectInterface, Context context, ArrayList<Exames> examesArrayList) {
        this.selectInterface = selectInterface;
        this.context = context;
        this.examesArrayList = examesArrayList;
    }

    @NonNull
    @Override
    public viewExamesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RecyclerViewexamesBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false),selectInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull viewExamesAdapter.ViewHolder holder, int position) {
            Exames exames = examesArrayList.get(position);

            holder.binding.tvNome.setText(exames.getNome());
            holder.binding.tvData.setText(formatarData(exames.getData()));

    }

    @Override
    public int getItemCount() {
        return examesArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final RecyclerViewexamesBinding binding;
        public ViewHolder(@NonNull RecyclerViewexamesBinding binding,OnSelectInterface selectInterface) {
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
