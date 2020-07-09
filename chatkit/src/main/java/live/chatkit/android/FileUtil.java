package live.chatkit.android;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Pair;

import androidx.documentfile.provider.DocumentFile;

import java.text.DecimalFormat;

public class FileUtil {
    
    private static final String TAG = "FileUtil";

    private static String getFileName(DocumentFile documentFile) {
        return (documentFile != null) ? documentFile.getName() : "";
    }

    private static long getFileSize(DocumentFile documentFile) {
        return (documentFile != null) ? documentFile.length() : 0;
    }

    public static Pair getFileInfo(Uri localUri, Context context) {
        //if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            Cursor c = context.getContentResolver().query(localUri, null, null, null, null);
            if (c != null) {
                int nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = c.getColumnIndex(OpenableColumns.SIZE);
                if (c.moveToFirst())
                    return Pair.create(c.getString(nameIndex), c.getLong(sizeIndex));
            }
        //}

        DocumentFile documentFile = DocumentFile.fromSingleUri(context, localUri);
        return Pair.create(getFileName(documentFile), getFileSize(documentFile));
    }

    public static boolean isImage(String mimeType) {
        return (mimeType != null & mimeType.startsWith("image")) ? true : false;
    }

    public static String getReadableFileSize(long size) {
        final int KILO = 1024;
        final DecimalFormat dec = new DecimalFormat("###.#");
        final String KB = " KB";
        final String MB = " MB";
        final String GB = " GB";
        float fileSize = 0;
        String suffix = KB;

        if (size > KILO) {
            fileSize = size / KILO;
            if (fileSize > KILO) {
                fileSize = fileSize / KILO;
                if (fileSize > KILO) {
                    fileSize = fileSize / KILO;
                    suffix = GB;
                } else {
                    suffix = MB;
                }
            }
        }
        return dec.format(fileSize) + suffix;
    }

}
