package com.example.mensagensconsumidor.ui;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.mensagensconsumidor.R;
import com.example.mensagensconsumidor.model.Mensagem;
import com.example.mensagensconsumidor.model.MensagemContract;
import com.example.mensagensconsumidor.worker.NotificacaoWorker;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FavoritasFragment extends Fragment {
    private RecyclerView recyclerView;
    private TextInputLayout tilFrequencia;
    private MaterialButton btnConfigurarNotificacoes, btnCancelarNotificacoes;
    private MensagemAdapter adapter;
    private List<Mensagem> mensagens;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favoritas, container, false);

        recyclerView = view.findViewById(R.id.recycler_favoritas);
        tilFrequencia = view.findViewById(R.id.til_frequencia);
        btnConfigurarNotificacoes = view.findViewById(R.id.btn_configurar_notificacoes);
        btnCancelarNotificacoes = view.findViewById(R.id.btn_cancelar_notificacoes);

        // Adicionar animações
        recyclerView.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
        tilFrequencia.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
        btnConfigurarNotificacoes.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scale_button));
        btnCancelarNotificacoes.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scale_button));

        mensagens = new ArrayList<>();
        adapter = new MensagemAdapter(mensagens, id -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Desmarcar Favorita")
                    .setMessage("Deseja desmarcar esta mensagem como favorita?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        ContentValues values = new ContentValues();
                        values.put(MensagemContract.MensagemEntry.COLUMN_FAVORITA, 0);
                        int updated = requireActivity().getContentResolver().update(
                                MensagemContract.MensagemEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build(),
                                values, null, null);
                        if (updated > 0) {
                            Toast.makeText(requireContext(), "Mensagem desmarcada como favorita", Toast.LENGTH_SHORT).show();
                            loadFavoritas();
                        } else {
                            Toast.makeText(requireContext(), "Erro ao desmarcar", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Não", null)
                    .show();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        loadFavoritas();

        btnConfigurarNotificacoes.setOnClickListener(v -> {
            String freqStr = tilFrequencia.getEditText().getText().toString();
            if (freqStr.isEmpty()) {
                tilFrequencia.setError("Informe a frequência");
                return;
            }
            int freq;
            try {
                freq = Integer.parseInt(freqStr);
                if (freq < 1 || freq > 10) {
                    tilFrequencia.setError("Frequência deve ser entre 1 e 10");
                    return;
                }
            } catch (NumberFormatException e) {
                tilFrequencia.setError("Frequência inválida");
                return;
            }
            tilFrequencia.setError(null);

            if (mensagens.isEmpty()) {
                Toast.makeText(requireContext(), "Nenhuma mensagem favorita encontrada", Toast.LENGTH_SHORT).show();
                return;
            }

            long interval = 24 * 60 / freq;
            PeriodicWorkRequest notificacaoRequest = new PeriodicWorkRequest.Builder(
                    NotificacaoWorker.class, interval, TimeUnit.MINUTES, interval / 2, TimeUnit.MINUTES)
                    .build();
            WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                    "notificacao_favoritas", ExistingPeriodicWorkPolicy.REPLACE, notificacaoRequest);
            Toast.makeText(requireContext(), "Notificações configuradas para " + freq + " vezes por dia", Toast.LENGTH_SHORT).show();
        });

        btnCancelarNotificacoes.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Cancelar Notificações")
                    .setMessage("Deseja cancelar todas as notificações agendadas?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        WorkManager.getInstance(requireContext()).cancelUniqueWork("notificacao_favoritas");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
                            notificationManager.cancelAll();
                        }
                        Toast.makeText(requireContext(), "Notificações canceladas", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Não", null)
                    .show();
        });

        return view;
    }

    private void loadFavoritas() {
        Cursor cursor = requireActivity().getContentResolver().query(
                MensagemContract.MensagemEntry.CONTENT_URI, null,
                MensagemContract.MensagemEntry.COLUMN_FAVORITA + " = ?", new String[]{"1"}, null);
        mensagens.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(MensagemContract.MensagemEntry._ID);
                int textoIndex = cursor.getColumnIndex(MensagemContract.MensagemEntry.COLUMN_MENSAGEM);
                int autorIndex = cursor.getColumnIndex(MensagemContract.MensagemEntry.COLUMN_AUTOR);

                if (idIndex == -1 || textoIndex == -1 || autorIndex == -1) {
                    Toast.makeText(requireContext(), "Erro: Colunas não encontradas no banco de dados", Toast.LENGTH_SHORT).show();
                    cursor.close();
                    return;
                }

                long id = cursor.getLong(idIndex);
                String texto = cursor.getString(textoIndex);
                String autor = cursor.getString(autorIndex);
                mensagens.add(new Mensagem(id, texto, autor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
        if (mensagens.isEmpty()) {
            Toast.makeText(requireContext(), "Nenhuma mensagem favorita", Toast.LENGTH_SHORT).show();
        }
    }
}