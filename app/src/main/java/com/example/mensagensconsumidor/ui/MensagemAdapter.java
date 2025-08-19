package com.example.mensagensconsumidor.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mensagensconsumidor.R;
import com.example.mensagensconsumidor.model.Mensagem;

import java.util.List;

public class MensagemAdapter extends RecyclerView.Adapter<MensagemAdapter.MensagemViewHolder> {
    private final List<Mensagem> mensagens;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(long id);
    }

    public MensagemAdapter(List<Mensagem> mensagens, OnItemClickListener listener) {
        this.mensagens = mensagens;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MensagemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensagem, parent, false);
        return new MensagemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MensagemViewHolder holder, int position) {
        Mensagem mensagem = mensagens.get(position);
        holder.tvMensagem.setText(mensagem.getTexto());
        holder.tvAutor.setText(mensagem.getAutor());
        holder.ivFavorita.setVisibility(View.VISIBLE); // Todas as mensagens na lista são favoritas
        holder.itemView.setOnClickListener(v -> listener.onItemClick(mensagem.getId()));
    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }

    static class MensagemViewHolder extends RecyclerView.ViewHolder {
        TextView tvMensagem, tvAutor;
        ImageView ivFavorita;

        MensagemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMensagem = itemView.findViewById(R.id.tv_mensagem);
            tvAutor = itemView.findViewById(R.id.tv_autor);
            ivFavorita = itemView.findViewById(R.id.iv_favorita);
        }
    }
}