package com.example.petcentral.Adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcentral.databinding.RecyclerExibirVacinaItemBinding;

public class timelineVacinaAdapter extends RecyclerView.Adapter<timelineVacinaAdapter.ViewHolder> {
    @NonNull
    @Override
    public timelineVacinaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull timelineVacinaAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final RecyclerExibirVacinaItemBinding binding;
        public ViewHolder(@NonNull RecyclerExibirVacinaItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
