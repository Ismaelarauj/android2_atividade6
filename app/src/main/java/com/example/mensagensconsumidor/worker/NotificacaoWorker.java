package com.example.mensagensconsumidor.worker;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.mensagensconsumidor.MyApplication;
import com.example.mensagensconsumidor.R;
import com.example.mensagensconsumidor.model.MensagemContract;
import com.example.mensagensconsumidor.ui.MainActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NotificacaoWorker extends Worker {
    public NotificacaoWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Consultar todas as mensagens favoritas
            List<Long> favoritaIds = new ArrayList<>();
            Cursor cursor = getApplicationContext().getContentResolver().query(
                    MensagemContract.MensagemEntry.CONTENT_URI,
                    new String[]{MensagemContract.MensagemEntry._ID},
                    MensagemContract.MensagemEntry.COLUMN_FAVORITA + " = ?",
                    new String[]{"1"},
                    null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) cursor.close();
                return Result.retry(); // Tentar novamente se não houver mensagens favoritas
            }

            int idIndex = cursor.getColumnIndex(MensagemContract.MensagemEntry._ID);
            if (idIndex == -1) {
                cursor.close();
                return Result.failure(); // Falha se a coluna _ID não for encontrada
            }
            do {
                favoritaIds.add(cursor.getLong(idIndex));
            } while (cursor.moveToNext());
            cursor.close();

            if (favoritaIds.isEmpty()) {
                return Result.retry(); // Tentar novamente se não houver mensagens favoritas
            }

            // Selecionar uma mensagem favorita aleatoriamente
            Random random = new Random();
            long selectedId = favoritaIds.get(random.nextInt(favoritaIds.size()));
            cursor = getApplicationContext().getContentResolver().query(
                    MensagemContract.MensagemEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(selectedId)).build(),
                    new String[]{MensagemContract.MensagemEntry.COLUMN_MENSAGEM, MensagemContract.MensagemEntry.COLUMN_AUTOR},
                    null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) cursor.close();
                return Result.retry(); // Tentar novamente se a mensagem não for encontrada
            }

            int mensagemIndex = cursor.getColumnIndex(MensagemContract.MensagemEntry.COLUMN_MENSAGEM);
            int autorIndex = cursor.getColumnIndex(MensagemContract.MensagemEntry.COLUMN_AUTOR);
            if (mensagemIndex == -1 || autorIndex == -1) {
                cursor.close();
                return Result.failure(); // Falha se as colunas não forem encontradas
            }

            String mensagem = cursor.getString(mensagemIndex);
            String autor = cursor.getString(autorIndex);
            cursor.close();

            // Criar PendingIntent para abrir o MainActivity
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // Construir a notificação
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), MyApplication.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_star)
                    .setContentTitle("Mensagem Favorita")
                    .setContentText(mensagem)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(mensagem + "\nPor: " + autor))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            // Enviar a notificação
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            if (androidx.core.content.ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify((int) System.currentTimeMillis(), builder.build());
                return Result.success();
            } else {
                return Result.retry(); // Tentar novamente se a permissão não foi concedida
            }
        } catch (Exception e) {
            return Result.retry(); // Tentar novamente em caso de erro inesperado
        }
    }
}