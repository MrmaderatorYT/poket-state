package com.ccs.poketstate.ui;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.ccs.poketstate.i18n.Strings;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** Exports monthly macroeconomic series directly to Downloads/PoketState for thesis analysis. */
public final class CsvExporter {
    public static String saveToDownloads(Context context, String fileName, String csvContent) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/PoketCity");
            Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) return Strings.t("Не вдалося створити CSV-файл.");
            try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                if (out == null) return Strings.t("Не вдалося записати CSV-файл.");
                out.write(csvContent.getBytes(StandardCharsets.UTF_8));
            }
            return Strings.f("Збережено в Downloads/PoketState/%s", fileName);
        } catch (Exception e) {
            return Strings.f("Помилка експорту: %s", e.getMessage());
        }
    }
}
