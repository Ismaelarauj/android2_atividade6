package com.example.mensagensconsumidor.ui;

import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mensagensconsumidor.R;
import com.example.mensagensconsumidor.model.MensagemContract;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Random;

public class MensagemAleatoriaFragment extends Fragment {
    private TextView tvMensagem, tvAutor;
    private ImageView ivFavorita;
    private MaterialButton btnListaFavoritas, btnAtualizar;
    private CardView cardMensagem;
    private long currentId = -1;
    private boolean isFavorita = false;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(requireContext(), "Permissão de notificação necessária", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mensagem_aleatoria, container, false);

        tvMensagem = view.findViewById(R.id.tv_mensagem);
        tvAutor = view.findViewById(R.id.tv_autor);
        ivFavorita = view.findViewById(R.id.iv_favorita);
        btnListaFavoritas = view.findViewById(R.id.btn_lista_favoritas);
        btnAtualizar = view.findViewById(R.id.btn_atualizar);
        cardMensagem = view.findViewById(R.id.card_mensagem);

        // Adicionar animação de escala aos botões
        btnListaFavoritas.setAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scale_button));
        btnAtualizar.setAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scale_button));

        // Solicitar permissão de notificação
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Configurar clique no CardView para marcar/desmarcar como favorita
        cardMensagem.setOnClickListener(v -> {
            if (currentId == -1) {
                Toast.makeText(requireContext(), "Nenhuma mensagem selecionada", Toast.LENGTH_SHORT).show();
                return;
            }
            String title = isFavorita ? "Desmarcar Favorita" : "Marcar como Favorita";
            String message = isFavorita ? "Deseja desmarcar esta mensagem como favorita?" : "Deseja marcar esta mensagem como favorita?";
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Sim", (dialog, which) -> {
                        ContentValues values = new ContentValues();
                        values.put(MensagemContract.MensagemEntry.COLUMN_FAVORITA, isFavorita ? 0 : 1);
                        int updated = requireActivity().getContentResolver().update(
                                MensagemContract.MensagemEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(currentId)).build(),
                                values, null, null);
                        if (updated > 0) {
                            isFavorita = !isFavorita;
                            ivFavorita.setVisibility(isFavorita ? View.VISIBLE : View.GONE);
                            Toast.makeText(requireContext(), isFavorita ? "Marcada como favorita" : "Desmarcada como favorita", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Erro ao atualizar favorita", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Não", null)
                    .show();
        });

        btnListaFavoritas.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_mensagemAleatoria_to_favoritas);
        });

        btnAtualizar.setOnClickListener(v -> showRandomMensagem());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        showRandomMensagem();
    }

    private void showRandomMensagem() {
        Cursor cursor = requireActivity().getContentResolver().query(MensagemContract.MensagemEntry.CONTENT_URI, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            Random random = new Random();
            int position = random.nextInt(cursor.getCount());
            cursor.moveToPosition(position);

            int idIndex = cursor.getColumnIndex(MensagemContract.MensagemEntry._ID);
            int mensagemIndex = cursor.getColumnIndex(MensagemContract.MensagemEntry.COLUMN_MENSAGEM);
            int autorIndex = cursor.getColumnIndex(MensagemContract.MensagemEntry.COLUMN_AUTOR);
            int favoritaIndex = cursor.getColumnIndex(MensagemContract.MensagemEntry.COLUMN_FAVORITA);

            if (idIndex == -1 || mensagemIndex == -1 || autorIndex == -1 || favoritaIndex == -1) {
                Toast.makeText(requireContext(), "Erro: Colunas não encontradas no banco de dados", Toast.LENGTH_SHORT).show();
                cursor.close();
                return;
            }

            currentId = cursor.getLong(idIndex);
            tvMensagem.setText(cursor.getString(mensagemIndex));
            tvAutor.setText(cursor.getString(autorIndex));
            isFavorita = cursor.getInt(favoritaIndex) == 1;
            ivFavorita.setVisibility(isFavorita ? View.VISIBLE : View.GONE);
            cardMensagem.setEnabled(true);
            cursor.close();
        } else {
            tvMensagem.setText("Nenhuma mensagem cadastrada");
            tvAutor.setText("");
            ivFavorita.setVisibility(View.GONE);
            cardMensagem.setEnabled(false);
            if (cursor != null) cursor.close();
        }
    }
}