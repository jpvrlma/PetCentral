package com.example.petcentral.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcentral.Interfaces.OnSelectInterface;
import com.example.petcentral.Objetos.Vacinas;
import com.example.petcentral.R;
import com.example.petcentral.databinding.RecyclerExibirVacinaItemBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class timelineVacinaAdapter extends RecyclerView.Adapter<timelineVacinaAdapter.ViewHolder> {

    Context context;
    private ArrayList<Vacinas> vacinasArrayList;
    private final OnSelectInterface selectInterface;

    public timelineVacinaAdapter(Context context, ArrayList<Vacinas> vacinasArrayList,OnSelectInterface selectInterface) {
        this.context = context;
        this.vacinasArrayList = vacinasArrayList;
        this.selectInterface = selectInterface;
    }

    @NonNull
    @Override
    public timelineVacinaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RecyclerExibirVacinaItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false),selectInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull timelineVacinaAdapter.ViewHolder holder, int position) {
        Vacinas vacinas = vacinasArrayList.get(position);

        holder.binding.tvNome.setText(vacinas.getNome());

        if (vacinas.getDataAplicacao() != null) {
            holder.binding.tvDataDoseAtual.setText(formatarData(vacinas.getDataAplicacao()));
        } else {
            holder.binding.tvDataDoseAtual.setText("Data não disponível");
        }

        if (vacinas.getProximaDose() != null) {
            holder.binding.tvDataProximaDose.setText(formatarData(vacinas.getProximaDose()));
            carregarStatus(vacinas.getProximaDose().toDate(), holder);
        } else {
            holder.binding.tvDataProximaDose.setText("Data não disponível");
        }
    }

    @Override
    public int getItemCount() {
        return vacinasArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final RecyclerExibirVacinaItemBinding binding;
        public ViewHolder(@NonNull RecyclerExibirVacinaItemBinding binding,OnSelectInterface selectInterface) {
            super(binding.getRoot());
            this.binding = binding;


            binding.cardView.setOnClickListener(v -> {
                if (selectInterface != null){
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION){
                        selectInterface.onSelectClick(position);
                    }
                }
            });
        }
    }

    private static String formatarData(Timestamp data){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dataFormatada = sdf.format(data.toDate());
        return dataFormatada;
    }

    private void carregarStatus(Date proximaDose, ViewHolder holder) {
        Calendar calendarAtual = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Date dataAtual = calendarAtual.getTime();


        if (dataAtual.before(proximaDose)) {
            holder.binding.tvStatus.setText("Status: Em dia");
            holder.binding.tvStatus.setTextColor(context.getColor(R.color.md_theme_primary));
        } else if (dataAtual.after(proximaDose)) {
            holder.binding.tvStatus.setText("Status: Pendente");
            holder.binding.tvStatus.setTextColor(context.getColor(R.color.md_theme_error));
        } else {
            holder.binding.tvStatus.setText("Status: Em dia");
            holder.binding.tvStatus.setTextColor(context.getColor(R.color.md_theme_primary));
        }
    }


}
