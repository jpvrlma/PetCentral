package com.example.petcentral.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.petcentral.Interfaces.PetInterface;
import com.example.petcentral.Objetos.Pet;
import com.example.petcentral.R;
import com.example.petcentral.databinding.RecyclerItemBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class petAdapter extends RecyclerView.Adapter<petAdapter.ViewHolder> {

    Context context;
    ArrayList<Pet> petArrayList;
    private FirebaseFirestore db;
    private final PetInterface petInterface;

    public petAdapter(Context context, ArrayList<Pet> petArrayList, FirebaseFirestore db,PetInterface petInterface) {
        this.context = context;
        this.petArrayList = petArrayList;
        db = FirebaseFirestore.getInstance();
        this.petInterface = petInterface;
    }

    @NonNull
    @Override
    public petAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RecyclerItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false),petInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull petAdapter.ViewHolder holder, int position) {
        Pet pet = petArrayList.get(position);

        holder.binding.textNome.setText(pet.getNome());
        holder.binding.textEspecie.setText(pet.getEspecie() + " - " + pet.getSexo());
        holder.binding.textRaca.setText(pet.getRaca());

        if (pet.getDataNascimento() != null){
            Date dataNascimento = pet.getDataNascimento().toDate();
            String idade = calcularIdadeFormatada(dataNascimento);
            holder.binding.textIdade.setText(idade);
        }

    }

    @Override
    public int getItemCount() {
        return petArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final RecyclerItemBinding binding;
        public ViewHolder(@NonNull RecyclerItemBinding binding,PetInterface petInterface) {
            super(binding.getRoot());
            this.binding = binding;

            binding.cardView.setOnClickListener(v ->{
                if (petInterface != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        petInterface.onClick(position);
                    }
                }
            });

            binding.btnEditar.setOnClickListener(v -> {
                if (petInterface != null){
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION){
                        petInterface.onEditClick(position);
                    }
                }
            });

        }
    }
    public static String calcularIdadeFormatada(Date dataNascimento) {
        Calendar dataDeNascimentoCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        dataDeNascimentoCalendar.setTime(dataNascimento);
        Calendar dataAtualCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        int anos = dataAtualCalendar.get(Calendar.YEAR) - dataDeNascimentoCalendar.get(Calendar.YEAR);
        int meses = dataAtualCalendar.get(Calendar.MONTH) - dataDeNascimentoCalendar.get(Calendar.MONTH);

        if (meses < 0) {
            anos--;
            meses += 12;
        }
        StringBuilder idadeFormatada = new StringBuilder();
        if (anos > 0) {
            idadeFormatada.append(anos).append(anos == 1 ? " ano" : " anos");
        }
        if (meses > 0) {
            if (idadeFormatada.length() > 0) {
                idadeFormatada.append(" e ");
            }
            idadeFormatada.append(meses).append(meses == 1 ? " mês" : " meses");
        }
        return idadeFormatada.toString();
    }


}
