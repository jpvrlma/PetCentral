package com.example.petcentral.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcentral.Interfaces.OnSelectInterface;
import com.example.petcentral.Objetos.DoseVacina;
import com.example.petcentral.R;
import com.example.petcentral.databinding.RecyclerDosesBinding;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

public class dosesVacinaAdapter extends RecyclerView.Adapter<dosesVacinaAdapter.ViewHolder> {

    Context context;
    ArrayList<DoseVacina> doseVacinaArrayList;
    private final OnSelectInterface selectInterface;

    public dosesVacinaAdapter(Context context, ArrayList<DoseVacina> doseVacinaArrayList, OnSelectInterface selectInterface) {
        this.context = context;
        this.doseVacinaArrayList = doseVacinaArrayList;
        this.selectInterface = selectInterface;
    }

    @NonNull
    @Override
    public dosesVacinaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RecyclerDosesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), selectInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull dosesVacinaAdapter.ViewHolder holder, int position) {
        DoseVacina doseVacina = doseVacinaArrayList.get(position);

        //Tirar Linha
        if (position == doseVacinaArrayList.size() - 1) {
            holder.binding.linhaDivisora.setVisibility(View.GONE);
        } else {
            holder.binding.linhaDivisora.setVisibility(View.VISIBLE);
        }
        //Altera a cor do marcador e da linha de acordo com o boolean retornado
        if (doseVacina.isAplicada() == false) {
            holder.binding.tvProxima.setVisibility(View.VISIBLE);
            holder.binding.tvDia.setText(formatarData(doseVacina.getProximaDose()));
            holder.binding.marcador.setImageResource(R.drawable.marker_red);
            holder.binding.linhaDivisora.setBackgroundColor(context.getColor(R.color.md_theme_error));
        } else {
            holder.binding.tvProxima.setText("Aplicada");
            holder.binding.tvDia.setText(formatarData(doseVacina.getDataAplicacao()));
            holder.binding.marcador.setImageResource(R.drawable.marker_green);
            holder.binding.linhaDivisora.setBackgroundColor(context.getColor(R.color.verde));
        }

        //Indicar se a vacina foi aplicada ou não
        if (doseVacina.getMarca() == null) {
            holder.binding.tvMarca.setText("Não aplicada");
        } else {
            holder.binding.tvMarca.setText(doseVacina.getMarca());
        }
        if (doseVacina.getLote() == null) {
            holder.binding.tvLote.setVisibility(View.GONE);
        } else {
            holder.binding.tvLote.setText(doseVacina.getLote());
        }
        holder.binding.tvDose.setText(doseVacina.getId());
    }

    @Override
    public int getItemCount() {
        return doseVacinaArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerDosesBinding binding;

        public ViewHolder(@NonNull RecyclerDosesBinding binding, OnSelectInterface selectInterface) {
            super(binding.getRoot());
            this.binding = binding;

            binding.cardView.setOnClickListener(v -> {
                if (selectInterface != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        selectInterface.onSelectClick(position);
                    }
                }
            });
        }
    }

    private static String formatarData(Timestamp data) {
        if (data == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(data.toDate());
    }


}
