package com.example.mensagensconsumidor.worker;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.mensagensconsumidor.R;
import com.example.mensagensconsumidor.model.MensagemContract;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NotificacaoWorker extends Worker {
    private static final String CHANNEL_ID = "mensagens_favoritas";

    public NotificacaoWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        List<Long> favoritaIds = new ArrayList<>();
        Cursor cursor = getApplicationContext().getContentResolver().query(
                MensagemContract.MensagemEntry.CONTENT_URI, null,
                MensagemContract.MensagemEntry.COLUMN_FAVORITA + " = ?", new String[]{"1"}, null);
        if (cursor != null) {
            int idIndex = cursor.getColumnIndex(MensagemContract.MensagemEntry._ID);
            if (idIndex == -1) {
                cursor.close();
                return Result.failure();
            }
            while (cursor.moveToNext()) {
                favoritaIds.add(cursor.getLong(idIndex));
            }
            cursor.close();
        }

        if (!favoritaIds.isEmpty()) {
            Random random = new Random();
            long id = favoritaIds.get(random.nextInt(favoritaIds.size()));
            cursor = getApplicationContext().getContentResolver().query(
                    MensagemContract.MensagemEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build(),
                    null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int mensagemIndex = cursor.getColumnIndex(MensagemContract.MensagemEntry.COLUMN_MENSAGEM);
                int autorIndex = cursor.getColumnIndex(MensagemContract.MensagemEntry.COLUMN_AUTOR);

                if (mensagemIndex == -1 || autorIndex == -1) {
                    cursor.close();
                    return Result.failure();
                }

                String mensagem = cursor.getString(mensagemIndex);
                String autor = cursor.getString(autorIndex);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Mensagem Favorita")
                        .setContentText(mensagem)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(mensagem + "\nPor: " + autor))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                if (androidx.core.content.ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    notificationManager.notify(random.nextInt(1000), builder.build());
                }

                cursor.close();
            }
        }

        return Result.success();
    }
}