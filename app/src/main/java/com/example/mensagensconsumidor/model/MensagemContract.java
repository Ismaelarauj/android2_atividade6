package com.example.mensagensconsumidor.model;

import android.net.Uri;
import android.provider.BaseColumns;

public class MensagemContract {
    public static final String AUTHORITY = "com.example.mensagensdodia.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_MENSAGENS = "mensagens";

    public static final class MensagemEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MENSAGENS).build();
        public static final String TABLE_NAME = "mensagens";
        public static final String COLUMN_MENSAGEM = "mensagem";
        public static final String COLUMN_AUTOR = "autor";
        public static final String COLUMN_FAVORITA = "favorita"; // 1 para favorita, 0 para não
    }
}