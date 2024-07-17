package com.example.petcentral;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

public class petAdapter extends RecyclerView.Adapter<petAdapter.myViewHolder> {

    Context context;
    ArrayList<Pet> petArrayList;

    public petAdapter(Context context, ArrayList<Pet> petArrayList) {
        this.context = context;
        this.petArrayList = petArrayList;
    }

    @NonNull
    @Override
    public petAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);

        return new myViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull petAdapter.myViewHolder holder, int position) {
        Pet pet = petArrayList.get(position);

        holder.textNome.setText(pet.getNome());
        holder.textEspecie.setText(pet.getEspecie());
        holder.textRaca.setText(pet.getRaca());
        holder.textIdade.setText(String.valueOf(pet.getDataNascimento()));
    }

    @Override
    public int getItemCount() {
        return petArrayList.size();
    }
    public static class myViewHolder extends RecyclerView.ViewHolder{

       TextView textNome, textEspecie, textRaca, textIdade;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            textNome = itemView.findViewById(R.id.textNome);
            textEspecie = itemView.findViewById(R.id.textEspecie);
            textRaca = itemView.findViewById(R.id.textRaca);
            textIdade = itemView.findViewById(R.id.textIdade);


        }
    }
}
